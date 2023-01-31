package com.nowcoder.community.controller.advice;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/1/31
 */
@Controller
public class NotFoundException implements ErrorController
{

    @Override
    public String getErrorPath()
    {
        return "/error";
    }

    @RequestMapping(value = {"/error"})
    public void error(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        response.sendRedirect(request.getContextPath() + "/error/404");
    }
}
