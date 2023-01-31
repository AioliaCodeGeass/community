package com.nowcoder.community;

import com.nowcoder.community.util.SensitiveFilter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/1/29
 */
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class SensitiveTests
{
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testSensitiveFilter()
    {
        String text="这里不可以赌博，不可以嫖娼，不可以吸毒，不可以开票。任何邪恶，终将绳之以法！";
        text=sensitiveFilter.filter(text);
        System.out.println(text);
    }
}
