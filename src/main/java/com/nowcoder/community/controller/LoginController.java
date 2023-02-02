package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/1/27
 */
@Slf4j
@Controller
public class LoginController implements CommunityConstant
{
    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(value="/register",method = RequestMethod.GET)
    public String getRegisterPage()
    {
        return "/site/register";
    }

    @RequestMapping(value="/login",method = RequestMethod.GET)
    public String getLoginPage()
    {
        return "/site/login";
    }

    @RequestMapping(value="/forget",method = RequestMethod.GET)
    public String getForgetPage()
    {
        return "/site/forget";
    }

    @RequestMapping(value="/register",method=RequestMethod.POST)
    public String register(Model model, User user)
    {
        Map<String,Object> map=userService.register(user);
        if(map==null||map.isEmpty())
        {
            model.addAttribute("msg","注册成功,我们已经向您的邮箱发送了一封激活邮件,请尽快激活!");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }
        else
        {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }

    @RequestMapping(value="/activation/{userId}/{code}",method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code)
    {
        int result=userService.activation(userId,code);
        if(result==ACTIVATION_SUCCESS)
        {
            model.addAttribute("msg","激活成功，您的账号已经可以正常使用了!");
            model.addAttribute("target","/login");
        }
        else if(result==ACTIVATION_REPEAT)
        {
            model.addAttribute("msg","无效操作，该账号已经激活过了！");
            model.addAttribute("target","/index");
        }
        else
        {
            model.addAttribute("msg","激活失败，您提供的激活码不正确!");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }

    @RequestMapping(value="/kaptcha",method=RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response)
    {
        //生成验证码
        String text=kaptchaProducer.createText();
        BufferedImage image=kaptchaProducer.createImage(text);
        //验证码的归属
        String kaptchaOwner= CommunityUtil.generateUUID();
        Cookie cookie=new Cookie("kaptchaOwner",kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        //将验证码存入Redis
        String redisKey= RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey,text,60, TimeUnit.SECONDS);
        //将图片输出给浏览器
        response.setContentType("image/png");
        try{
            OutputStream os=response.getOutputStream();
            ImageIO.write(image,"png",os);
        }catch(IOException e){
            log.error("响应验证码失败:"+e.getMessage());
        }
    }

    @RequestMapping(value="login",method = RequestMethod.POST)
    public String login(String username,String password,String code,boolean rememberme,Model model,
                        HttpServletResponse response, @CookieValue("kaptchaOwner")String kaptchaOwner)
    {
        //检查验证码
        String kaptcha=null;
        if(StringUtils.isNotBlank(kaptchaOwner))
        {
            String redisKey=RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha=(String)redisTemplate.opsForValue().get(redisKey);
        }
        if(StringUtils.isBlank(kaptcha)||StringUtils.isBlank(code)||!kaptcha.equalsIgnoreCase(code))
        {
            model.addAttribute("codeMsg","验证码不正确！");
            return "/site/login";
        }
        //检查账号，密码
        int expiredSeconds=rememberme?REMEMBER_EXPIRED_SECONDS:DEFAULT_EXPIRED_SECONDS;
        Map<String,Object> map=userService.login(username,password,expiredSeconds);
        if(map.containsKey("ticket"))
        {
            Cookie cookie=new Cookie("ticket",map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        }
        else
        {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    @RequestMapping(value="/logout",method=RequestMethod.GET)
    public String logout(@CookieValue("ticket")String ticket)
    {
        userService.logout(ticket);
        return "redirect:/login";
    }

    @RequestMapping(value="getKaptcha",method = RequestMethod.GET)
    @ResponseBody
    public Map getKaptcha(Model model,String email, HttpSession session)
    {
        Map<String,Object> map=userService.getKaptcha(email,session);
        return map;
    }

    @RequestMapping(value="resetPassword",method = RequestMethod.POST)
    public String resetPassword(Model model,String email,String code,String password,HttpSession session)
    {
        //检查验证码
        String kaptcha=(String)session.getAttribute("kaptcha");
        if(StringUtils.isBlank(kaptcha)||StringUtils.isBlank(code)||!kaptcha.equalsIgnoreCase(code))
        {
            model.addAttribute("codeMsg","验证码不正确！");
            return "/site/forget";
        }
        //检查邮箱
        String codeEmail=(String)session.getAttribute("email");
        if(StringUtils.isBlank(codeEmail)||StringUtils.isBlank(email)||!codeEmail.equalsIgnoreCase(email))
        {
            model.addAttribute("emailMsg","邮箱不正确！");
            return "/site/forget";
        }
        //重置密码
        Map<String,Object> map=userService.resetPassword(password,email);
        if(map.containsKey("emailMsg")||map.containsKey("passwordMsg"))
        {
            model.addAttribute("emailMsg", map.get("emailMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/forget";
        }
        else
        {
            return "redirect:/login";
        }
    }
}
