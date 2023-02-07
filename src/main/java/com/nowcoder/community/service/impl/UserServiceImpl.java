package com.nowcoder.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.code.kaptcha.Producer;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.LoginTicketService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/1/25
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService, CommunityConstant
{
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private Producer kaptchaProducer;

    @Override
    public User findUserById(int id)
    {
        User user = getCache(id);
        if (user == null)
        {
            user = initCache(id);
        }
        return user;
    }

    @Override
    public Map<String, Object> register(User user)
    {
        Map<String, Object> map = new HashMap<>();
        // 空值处理
        if (user == null)
        {
            throw new IllegalArgumentException("参数不能为空！");
        }
        if (StringUtils.isBlank(user.getUsername()))
        {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword()))
        {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail()))
        {
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }
        //验证账号
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        User u = this.getOne(wrapper);
        if (u != null)
        {
            map.put("usernameMsg", "该账号已存在！");
            return map;
        }
        //验证邮箱
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, user.getEmail());
        u = this.getOne(wrapper);
        if (u != null)
        {
            map.put("emailMsg", "该邮箱已被注册!");
            return map;
        }
        //注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        save(user);
        //发送激活账号邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map;
    }

    @Override
    public int activation(int userId, String code)
    {
        User user = this.getById(userId);
        if (user.getStatus() == 1)
        {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code))
        {
//            user.setStatus(1);
//            updateById(user);
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        } else
        {
            return ACTIVATION_FAILURE;
        }
    }

    @Override
    public Map<String, Object> login(String username, String password, long expiredSeconds)
    {
        Map<String, Object> map = new HashMap<>();
        // 空值处理
        if (StringUtils.isBlank(username))
        {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(password))
        {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }
        // 验证账号
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = this.getOne(wrapper);
        if (user == null)
        {
            map.put("usernameMsg", "该账号不存在!");
            return map;
        }
        // 验证状态
        if (user.getStatus() == 0)
        {
            map.put("usernameMsg", "该账号未激活!");
            return map;
        }
        // 验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password))
        {
            map.put("passwordMsg", "密码不正确!");
            return map;
        }
        //生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));

        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey, loginTicket);

        map.put("ticket", loginTicket.getTicket());

        return map;
    }

    @Override
    public void logout(String ticket)
    {
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey, loginTicket);
    }

    @Override
    public Map<String, Object> getKaptcha(String email, HttpSession session)
    {
        Map<String, Object> map = new HashMap<>();
        //空值处理
        if (StringUtils.isBlank(email))
        {
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }
        //验证邮箱
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        User user = this.getOne(wrapper);
        if (user == null)
        {
            map.put("emailMsg", "该邮箱未被注册！");
            return map;
        }
        //生成验证码
        String text = kaptchaProducer.createText();
        //将验证码存入session
        session.setAttribute("kaptcha", text);
        //将当前邮箱存入session
        session.setAttribute("email", email);
        //发送重置密码邮件
        Context context = new Context();
        context.setVariable("email", email);
        String code = text;
        context.setVariable("code", code);
        String content = templateEngine.process("/mail/forget", context);
        mailClient.sendMail(email, "重置密码", content);
        map.put("emailMsg", "验证码发送成功，请查看邮箱!");

        return map;
    }

    @Override
    public Map<String, Object> resetPassword(String password, String email)
    {
        Map<String, Object> map = new HashMap<>();
        //空值处理
        if (StringUtils.isBlank(email))
        {
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }
        if (StringUtils.isBlank(password))
        {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }
        //重置密码
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        User user = this.getOne(wrapper);
        user.setPassword(CommunityUtil.md5(password + user.getSalt()));
        userMapper.updatePassword(user.getId(), user.getPassword());
//        this.update(user,wrapper);

        return map;
    }

    @Override
    public LoginTicket findLoginTicket(String ticket)
    {
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    @Override
    public Map<String, Object> updatePassword(String oldPassword, String newPassword)
    {
        Map<String, Object> map = new HashMap<>();
        // 空值处理
        if (StringUtils.isBlank(oldPassword))
        {
            map.put("oldPasswordMsg", "原密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(newPassword))
        {
            map.put("newPasswordMsg", "新密码不能为空!");
            return map;
        }
        //验证原密码
        User user = HostUtil.getUser();
        String md5OldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        if (!md5OldPassword.equals(user.getPassword()))
        {
            map.put("oldPasswordMsg", "原密码错误！");
            return map;
        }
        //修改密码
        user.setPassword(CommunityUtil.md5(newPassword + user.getSalt()));
        userMapper.updatePassword(user.getId(), user.getPassword());
//        this.updateById(user);
        return map;
    }

    public User getCache(int userId)
    {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    public User initCache(int userId)
    {
        User user = this.getById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    public void clearCache(int userId)
    {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

    public Collection<? extends GrantedAuthority> getAuthorities(int userId)
    {
        User user=this.getById(userId);
        List<GrantedAuthority> list=new ArrayList<>();
        list.add(new GrantedAuthority()
        {
            @Override
            public String getAuthority()
            {
                switch (user.getType())
                {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }
}
