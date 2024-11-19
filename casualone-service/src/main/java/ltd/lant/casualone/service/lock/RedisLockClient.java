package ltd.lant.casualone.service.lock;

import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.concurrent.locks.Lock;

/**
 * @author zhanglei
 * @description redis可重入分布式锁客户端
 * @date 2024/11/19  15:31
 */
public class RedisLockClient {

    @Resource
    private StringRedisTemplate redisTemplate;

    /**
     * 获取可重入分布式锁
     *
     * @param name
     * @return
     */
    public TheLock getReentrantLock(String name) {
        return new ReentrantDistributedLock(name, redisTemplate);
    }
}
