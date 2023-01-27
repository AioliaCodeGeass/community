package com.nowcoder.community;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/1/25
 */
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class MapperTests
{
    @Autowired
    private UserService userService;

    @Test
    public void testDatabase()
    {
        List<User> list=userService.list();
        System.out.println(list);
        System.out.println(list.size());
    }

    @Test
    public void testLogger()
    {
        log.info("hhh");
        log.debug("hhh");
    }
}
