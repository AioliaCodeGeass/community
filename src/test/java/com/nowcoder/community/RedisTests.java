package com.nowcoder.community;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
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

    //统计20万个重复数据的独立总数
    @Test
    public void testHyperLogLog()
    {
        String key="test:h11::01";

        System.out.println(redisTemplate.opsForHyperLogLog().size(key));
    }

    //统计一组数据的布尔值
    @Test
    public void testBitMap()
    {
        String redisKey="test:bm:01";
        redisTemplate.opsForValue().setBit(redisKey,1,true);
        redisTemplate.opsForValue().setBit(redisKey,4,true);
        redisTemplate.opsForValue().setBit(redisKey,7,true);
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,2));
        Object obj =redisTemplate.execute(new RedisCallback()
        {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException
            {
                return redisConnection.bitCount(redisKey.getBytes());
            }
        });

        System.out.println(obj);
    }
}
