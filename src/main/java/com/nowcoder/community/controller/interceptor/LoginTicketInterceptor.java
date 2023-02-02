package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CookieUtil;
import com.nowcoder.community.util.HostUtil;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/1/28
 */
@Component
public class LoginTicketInterceptor implements HandlerInterceptor
{
    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
    {
        String ticket= CookieUtil.getValue(request,"ticket");
        if(ticket!=null)
        {
            //查询凭证
            String redisKey= RedisKeyUtil.getTicketKey(ticket);
            LoginTicket loginTicket=(LoginTicket) redisTemplate.opsForValue().get(redisKey);
            //检查凭证是否有效
            if(loginTicket!=null&&loginTicket.getStatus()==0&&loginTicket.getExpired().after(new Date()))
            {
                //根据凭证查询用户
                User user=userService.findUserById(loginTicket.getUserId());
                //在本次请求中持有用户
                HostUtil.setUser(user);
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception
    {
        User user=HostUtil.getUser();
        if(user!=null&&modelAndView!=null)
        {
            modelAndView.addObject("loginUser",user);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception
    {
        HostUtil.clear();
    }
}
