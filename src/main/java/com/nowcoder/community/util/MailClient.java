package com.nowcoder.community.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;

/**
 * 邮件客户端
 * @author aiolia
 * @version 1.0
 * @create 2023/1/27
 */
@Slf4j
@Component
public class MailClient
{
    @Autowired
    private JavaMailSender mailSender;


    private String from="aiolia_geass@sina.com";

    public void sendMail(String to,String subject,String content)
    {
        try
        {
            MimeMessage message=mailSender.createMimeMessage();
            MimeMessageHelper helper=new MimeMessageHelper(message);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content,true);
            mailSender.send(helper.getMimeMessage());
        } catch (Exception e)
        {
            log.error("发送邮件失败:"+e.getMessage());
        }
    }
}
