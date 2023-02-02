package com.nowcoder.community.aspect;

import com.nowcoder.community.service.UserService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/2/2
 */
@Component
@Aspect
public class UserAspect
{
    @Autowired
    private UserService userService;

    //更新用户信息时，清除缓存
    @Pointcut("execution(* com.nowcoder.community.dao.UserMapper.update*(..))")
    public void pointcut()
    {

    }

    @AfterReturning("pointcut()")
    public void clearUserCache(JoinPoint joinPoint)
    {
        Object[]args=joinPoint.getArgs();
        int id=(int)args[0];
        userService.clearCache(id);
    }
}
