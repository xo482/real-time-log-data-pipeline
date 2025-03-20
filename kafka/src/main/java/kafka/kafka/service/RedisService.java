package kafka.kafka.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;


    public void setValue(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    // set 값과 ttl을 같이 설정
    public void setValueWithTTL(String key, String value, long timeout) {
        // 데이터를 Redis에 저장하고, ttl 설정 (timeout은 초 단위)
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }


    public String getValue(String key) {
        return (String) redisTemplate.opsForValue().get(key);
    }

    public void deleteValue(String key) {
        redisTemplate.delete(key);
    }

    public Long getTTL(String key) {
        return redisTemplate.getExpire(key);
    }

    public void setTTL(String key, long timeout) {
        redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
    }
}
