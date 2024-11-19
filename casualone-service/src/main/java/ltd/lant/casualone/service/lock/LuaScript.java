package ltd.lant.casualone.service.lock;

/**
 * @author zhanglei
 * @description 供redis执行的Lua脚本
 * @date 2024/11/19  15:27
 */
public class LuaScript {

    private LuaScript() {
    }

    /**
     * KEYS[1]是 lockKey 表示获取的锁资源，比如 lock:168。ARGV[1]表示表示锁的有效时间（单位毫秒）。ARGV[2]表示客户端唯一标识，在 Redisson 中使用 UUID:ThreadID。
     * 锁不存在或者锁存在且值与客户端唯一标识匹配，则执行'hincrby'和 pexpire指令，接着 return nil。
     * 表示的含义就是锁不存在就设置锁并设置锁重入计数值为 1，设置过期时间；
     * 锁存在且唯一标识匹配表明当前加锁请求是锁重入请求，锁从如计数 +1，重新锁超时时间。
     * redis.call('exists', KEYS[1]) == 0判断锁是否存在，0 表示不存在。
     * redis.call('hexists', KEYS[1], ARGV[2]) == 1)锁存在的话，判断 hash 结构中 fieldKey 与客户端的唯一标识是否相等。相等表示当前加锁请求是锁重入。
     * redis.call('hincrby', KEYS[1], ARGV[2], 1)将存储在 hash 结构的 ARGV[2]的值 +1，不存在则支持成 1。
     * redis.call('pexpire', KEYS[1], ARGV[1])对 KEYS[1]设置超时时间。
     * 锁存在，但是唯一标识不匹配，表明锁被其他线程持有，调用 pttl返回锁剩余的过期时间。
     * 可重入分布式锁加锁脚本
     * @return 当且仅当返回 `nil`才表示加锁成功；
     * 返回锁剩余过期时间是让客户端感知锁是否成功。
     */
    public static String reentrantLockScript() {
        return "if ((redis.call('exists', KEYS[1]) == 0) " +
                "or (redis.call('hexists', KEYS[1], ARGV[2]) == 1)) then " +
                "redis.call('hincrby', KEYS[1], ARGV[2], 1); " +
                "redis.call('pexpire', KEYS[1], ARGV[1]); " +
                "return nil; " +
                "end; " +
                "return redis.call('pttl', KEYS[1]);";
    }

    /**
     * 1 代表解锁成功，锁被释放。0 代表可重入次数被减 1。nil 代表其他线程尝试解锁，解锁失败。
     * KEYS[1] 是 lockKey，表示锁的资源，比如 lock:order:pay。ARGV[1]，锁的超时时间。ARGV[2]，Hash 表的 FieldKey。
     * -首先使用 hexists 判断 Redis 的 Hash 表是否存在 fileKey，如果不存在则直接返回 nil 解锁失败。
     * -若存在的情况下，且唯一标识匹配，使用 hincrby 对 fileKey 的值 -1，然后判断计算之后可重入次数。
     * --当前值 > 0 表示持有的锁存在重入情况，重新设置超时时间，返回值 1；
     * --若值小于等于 0，表明锁释放了，执行del释放锁。
     * 可重入分布式锁解锁脚本
     * @return 当且仅当返回 1 才表示解锁成功；
     */
    public static String reentrantUnlockScript() {
        return "if (redis.call('hexists', KEYS[1], ARGV[2]) == 0) then " +
                "return nil;" +
                "end; " +
                "local counter = redis.call('hincrby', KEYS[1], ARGV[2], -1); " +
                "if (counter > 0) then " +
                "redis.call('pexpire', KEYS[1], ARGV[1]); " +
                "return 0; " +
                "else " +
                "redis.call('del', KEYS[1]); " +
                "return 1; " +
                "end; " +
                "return nil;";
    }
}
