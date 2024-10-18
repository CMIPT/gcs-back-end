package edu.cmipt.gcs.util;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisUtil {
    private static RedisTemplate<String, Object> redisTemplate;

    public RedisUtil(RedisTemplate<String, Object> redisTemplate) {
        RedisUtil.redisTemplate = redisTemplate;
    }

    public static Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * @param key key
     * @param value object to be stored
     * @param expireTime time to live in milliseconds
     */
    public static void set(String key, Object value, Long expireTime) {
        redisTemplate.opsForValue().set(key, value, expireTime, TimeUnit.MILLISECONDS);
    }

    public static void del(String... keys) {
        for (String key : keys) {
            redisTemplate.delete(key);
        }
    }

    public static boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }
}
