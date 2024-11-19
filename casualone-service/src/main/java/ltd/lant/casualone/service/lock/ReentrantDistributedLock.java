package ltd.lant.casualone.service.lock;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author zhanglei
 * @description 可重入分布式锁   https://mp.weixin.qq.com/s/foXfebF5blLqLbIVjGy72w
 * @date 2024/11/19  14:20
 */
public class ReentrantDistributedLock implements TheLock {

    /**
     * 锁超时时间，默认 30 秒
     */
    protected long internalLockLeaseTime = 30000;

    /**
     * 标识 id
     */
    private final String id = UUID.randomUUID().toString();

    /**
     * 资源名称
     */
    private final String resourceName;

    private final List<String> keys = new ArrayList<>(1);

    /**
     * Redis 客户端
     */
    private final StringRedisTemplate redisTemplate;

    public ReentrantDistributedLock(String resourceName, StringRedisTemplate redisTemplate) {
        this.resourceName = resourceName;
        this.redisTemplate = redisTemplate;
        keys.add(resourceName);
    }

    /**
     * 加锁（等待指定时间）
     *
     * @param waitTime
     * @param leaseTime
     * @param unit
     * @return
     * @throws InterruptedException
     */
    @Override
    public boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
        long time = unit.toMillis(waitTime);
        long current = System.currentTimeMillis();
        long threadId = Thread.currentThread().getId();
        // lua 脚本获取锁
        Long ttl = tryAcquire(leaseTime, unit, threadId);
        // lock acquired
        if (ttl == null) {
            return true;
        }
        time -= System.currentTimeMillis() - current;
        // 等待时间用完，获取锁失败
        if (time <= 0) {
            return false;
        }
        // 自旋获取锁
        while (true) {
            long currentTime = System.currentTimeMillis();
            ttl = tryAcquire(leaseTime, unit, threadId);
            // lock acquired
            if (ttl == null) {
                return true;
            }
            time -= System.currentTimeMillis() - currentTime;
            if (time <= 0) {
                return false;
            }
        }
    }

    /**
     * 加锁（阻塞）
     *
     * @param leaseTime
     * @param unit
     */
    @Override
    public void lock(long leaseTime, TimeUnit unit) {
        long threadId = Thread.currentThread().getId();
        Long ttl = tryAcquire(leaseTime, unit, threadId);
        // lock acquired
        if (ttl == null) {
            return;
        }
        do {
            ttl = tryAcquire(leaseTime, unit, threadId);
            // lock acquired
        }
        while (ttl != null);
    }

    /**
     * 解锁
     */
    @Override
    public void unlock() {
        long threadId = Thread.currentThread().getId();
        // 执行 lua 脚本
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(LuaScript.reentrantUnlockScript(), Long.class);
        Long opStatus = redisTemplate.execute(redisScript, keys, String.valueOf(internalLockLeaseTime), getRequestId(threadId));
        if (opStatus == null) {
            throw new IllegalMonitorStateException("attempt to unlock lock, not locked by current thread by node id: " + id + " thread-id: " + threadId);
        }
    }

    /**
     * 获取分布式锁信息
     *
     * @param leaseTime
     * @param unit
     * @param threadId
     * @return
     */
    private Long tryAcquire(long leaseTime, TimeUnit unit, long threadId) {
        // 执行 lua 脚本
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(LuaScript.reentrantLockScript(), Long.class);
        return redisTemplate.execute(redisScript, keys, String.valueOf(unit.toMillis(leaseTime)), getRequestId(threadId));
    }

    /**
     * 获取分布式锁的key
     *
     * @param threadId
     * @return
     */
    private String getRequestId(long threadId) {
        return id + ":" + threadId;
    }
}
