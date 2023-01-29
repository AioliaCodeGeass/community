package com.nowcoder.community;

import com.nowcoder.community.util.MailClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/1/27
 */
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class MailTests
{
    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testTextMail()
    {
        mailClient.sendMail("aiolia_geass@outlook.com","TEST","Welcome.");
    }

    @Test
    public void testHtmlMail()
    {
        Context context = new Context();
        context.setVariable("username", "aiolia");

        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);

        mailClient.sendMail("aiolia_geass@outlook.com", "HTML", content);
    }
}
