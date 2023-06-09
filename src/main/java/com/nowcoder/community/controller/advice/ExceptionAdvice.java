package com.nowcoder.community.controller.advice;

import com.nowcoder.community.util.CommunityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/1/31
 */
@Slf4j
@ControllerAdvice
public class ExceptionAdvice
{
    @ExceptionHandler(Exception.class)
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        log.error("服务器发生异常："+e.getMessage());
        for(StackTraceElement element:e.getStackTrace())
        {
            log.error(element.toString());
        }
        String xRequestWith=request.getHeader("x-request-with");
        if("XMLHttpRequest".equals(xRequestWith))
        {
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer=response.getWriter();
            writer.write(CommunityUtil.getJsonString(1,"服务器异常"));
        }
        else
        {
            response.sendRedirect(request.getContextPath()+"/error/500");
        }
    }
}
