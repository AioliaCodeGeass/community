package com.nowcoder.community.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/1/31
 */
@Slf4j
@Component
@Aspect
public class ServiceLogAspect
{
    @Pointcut("execution(* com.nowcoder.community.service.*.*(..))")
    public void pointcut()
    {

    }

    @Before("pointcut()")
    public void before(JoinPoint joinPoint)
    {
        ServletRequestAttributes attributes=(ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request=attributes.getRequest();
        String ip=request.getRemoteHost();
        String now=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String target=joinPoint.getSignature().getDeclaringTypeName()+"."+joinPoint.getSignature().getName();
        log.info(String.format("用户[%s],在[%s],访问了[%s].",ip,now ,target));
    }
}
