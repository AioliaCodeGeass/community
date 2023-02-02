package com.nowcoder.community.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import javax.servlet.http.HttpSession;
import java.util.Map;


/**
 * @author aiolia
 * @version 1.0
 * @create 2023/1/25
 */
public interface UserService extends IService<User>
{
    public User findUserById(int id);

    public Map<String,Object> register(User user);

    public int activation(int userId,String code);

    public Map<String,Object> login(String username,String password,long expiredSeconds);

    public void logout(String ticket);

    public Map<String,Object> getKaptcha(String email, HttpSession session);

    public Map<String,Object> resetPassword(String password,String email);

    public LoginTicket findLoginTicket(String ticket);

    public Map<String, Object> updatePassword(String oldPassword, String newPassword);

    public User getCache(int userId);

    public User initCache(int userId);

    public void clearCache(int userId);
}
