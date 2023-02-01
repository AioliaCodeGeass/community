package com.nowcoder.community;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/2/1
 */
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisTests
{
    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testStrings()
    {
        String redisKey="test:count";
        ValueOperations ops = redisTemplate.opsForValue();
        ops.set(redisKey,1);
        System.out.println(ops.get(redisKey));
        System.out.println(ops.increment(redisKey));
        System.out.println(ops.decrement(redisKey));
    }
}
