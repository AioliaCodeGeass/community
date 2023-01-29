package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/1/29
 */
@Slf4j
@Controller
@RequestMapping("/user")
public class UserController
{
    @Value("${community.path.upload}")
    private String uploadPath;

    @Value(("${community.path.domain}"))
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @LoginRequired
    @RequestMapping(value="setting",method = RequestMethod.GET)
    public String getSettingPage()
    {
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(value="/upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model)
    {
        if(headerImage==null)
        {
            model.addAttribute("error","您还没有选择图片！");
            return "/site/setting";
        }
        //获取图片后缀
        String fileName=headerImage.getOriginalFilename();
        String suffix=fileName.substring(fileName.lastIndexOf("."));
        if(StringUtils.isBlank(suffix))
        {
            model.addAttribute("error","文件的格式不正确！");
            return "/site/setting";
        }
        //生成随机文件名
        fileName= CommunityUtil.generateUUID()+suffix;
        //确定文件存放的路径
        File dest=new File(uploadPath+"/"+fileName);
        try{
            //存储文件
            headerImage.transferTo(dest);
        }catch(IOException e)
        {
            log.error("上传文件失败："+e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常!"+e);
        }
        //更新当前用户的头像的路径
        User user= HostUtil.getUser();
        String headerUrl=domain+contextPath+"/user/header/"+fileName;
        user.setHeaderUrl(headerUrl);
        userService.updateById(user);

        return "redirect:/index";
    }

    @RequestMapping(value="/header/{fileName}",method=RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response)
    {
        //服务器存放路径
        fileName=uploadPath+"/"+fileName;
        String suffix=fileName.substring(fileName.lastIndexOf("."));
        //响应图片
        response.setContentType("image/"+suffix);
        try (
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            log.error("读取头像失败: " + e.getMessage());
        }
    }

    @RequestMapping(value="updatePassword",method=RequestMethod.POST)
    public String updatePassword(Model model,String oldPassword,String newPassword)
    {
        Map<String,Object> map=userService.updatePassword(oldPassword,newPassword);
        if(map.isEmpty())
        {
            return "redirect:/logout";
        }
        else
        {
            model.addAttribute("newPasswordMsg",map.get("newPasswordMsg"));
            model.addAttribute("oldPasswordMsg",map.get("oldPasswordMsg"));
            return "/site/setting";
        }
    }
}
