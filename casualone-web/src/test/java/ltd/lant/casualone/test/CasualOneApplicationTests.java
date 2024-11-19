package ltd.lant.casualone.test;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.locks.StampedLock;

@SpringBootTest
class CasualOneApplicationTests {

    @Test
    void stampedLockTest() {
        /*int i = Runtime.getRuntime().availableProcessors();
        System.err.println("本设备CPU核心数量 = " + i);

        String b = DateTimeFormatter.ofPattern("a").format(LocalDateTime.now());
        System.err.println(b);*/
        User user = new User(20);
        StampedLock lock = new StampedLock();
        Runnable w = () -> {
            long writeLock = lock.writeLock();
            System.err.println("writeLock: " + writeLock);
            user.setAge(user.getAge() + 1);
            System.out.println("write age: " + user.getAge());
            lock.unlockWrite(writeLock);
        };

        Runnable r = () -> {
            long readLock = lock.tryOptimisticRead();
            System.err.println("readLock: " + readLock);
            boolean validate = lock.validate(readLock);
            System.out.println("validate: " + validate);
            if (validate) {
                readLock = lock.readLock();
                try {
                    System.out.println("read age: " + user.getAge());
                } finally {
                    lock.unlockRead(readLock);
                }
            } else {
                System.out.println("read age: " + user.getAge());
            }
        };

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 50; i++) {
            executorService.submit(w);
            executorService.submit(r);
        }
    }

    @Test
    void optionalTest() {

        String s = "00003";
        int i = Integer.parseInt(s);
        System.err.println(i);

        User zhangsan = new User("zhangsan", 18);
        User lisi = null;
        User wangwu = new User("wangwu", 81);
        User user = Optional.ofNullable(zhangsan).orElse(wangwu);
        User user1 = Optional.ofNullable(lisi).orElse(wangwu);

        Optional<User> optUser1 = Optional.of(user);
        User user2 = optUser1.get();

        Optional.ofNullable(user1).ifPresent(p -> System.out.println("年龄" + p.getAge()));

    }

    @Test
    void intTest() throws NoSuchFieldException, IllegalAccessException {

        Class cache = Integer.class.getDeclaredClasses()[0]; //1
        Field myCache = cache.getDeclaredField("cache"); //2
        myCache.setAccessible(true);//3

        Integer[] newCache = (Integer[]) myCache.get(cache); //4
        newCache[132] = newCache[133]; //5

        int a = 2;
        int b = a + a;
        System.out.printf("%d + %d = %d", a, a, b); //

    }

    @Test
    void futureTest() {
        ExecutorService threadPool = Executors.newFixedThreadPool(5);
        Callable<Object> c1 = () -> {
            String name = "";
            for (int i = 0; i < 1000; i++) {
                name = Thread.currentThread().getName();
                //    System.out.println(name + "----" + i);
            }
            System.out.println(name + "执行完成！");
            return null;
        };
        Callable<Object> c2 = () -> {
            String name = "";
            for (int i = 0; i < 100000; i++) {
                name = Thread.currentThread().getName();
                if (i % 10000 == 0) {
                    System.err.println(name + "----" + i);
                }
            }
            System.err.println(name + "执行完成！");
            return null;
        };
        Future<Object> f1 = threadPool.submit(c1);
        Future<Object> f2 = threadPool.submit(c2);

        f2.cancel(true);

    }

    @Test
    void paint() {
        String s = "]7D8^<;>]367381:\\2;:5::4N2>2::9<B7K9835;3@K893;>V6;2n6<2k6?3i3C3I6J3C3I7I3C3J5J3C3i3C3i372:3j22893j218:3j326:3[4;:;3I3:9;7>3I4:8;3B3I4:7<3B3J63:=3B3K=B3B3N7E3B3j3?6j3;:j3:443j39263j3:173j3:173j3:272j3;362i3<;h3?8g3F4d3H5`4K7V8Q=<EYRh696m696m696m696m696m878k969k787L", t = "";
        char f = 48;
        int n = 1, i, j;
        for (i = 0; i < 265; i++) {
            for (j = 0; j < s.charAt(i) - '0'; j++) {
                t += f;
                if (n++ % 82 == 0) t += '\n';
            }
            f = (f == 48 ? ' ' : '0');
        }
        System.out.print(t);
    }



    @Test
    void caffeineTest() throws InterruptedException {
        Cache<Integer, String> cache = Caffeine.newBuilder().maximumSize(10).evictionListener((k, v, f) -> {
            System.err.println("淘汰的k，v = " + k + "," + v);
        }).build();
        cache.put(1,"234");
        String v1 = cache.getIfPresent(1);
        System.err.println(v1);
        String v2 = cache.getIfPresent(2);
        System.err.println(v2);
        String v3 = cache.get(2, v -> "345");
        System.err.println(v3);


        AsyncCache<Object, Object> asyncCache = Caffeine.newBuilder().maximumSize(10)
                .evictionListener((k, v, f) -> System.err.println("淘汰的k，v = " + k + "," + v))
                .expireAfterAccess(1, TimeUnit.SECONDS)
                .buildAsync();

        AsyncLoadingCache<Object, String> asyncLoadingCache = Caffeine.newBuilder().maximumSize(10)
                .evictionListener((k, v, f) -> System.err.println("淘汰的k，v = " + k + "," + v))
                .refreshAfterWrite(1, TimeUnit.SECONDS).expireAfterAccess(1, TimeUnit.SECONDS)
                .buildAsync(k -> k + "new");

//        for (int i = 0; i < 20; i++) {
//            cache.put(i, i);
//            asyncCache.put(i,new CompletableFuture<>());
//            asyncLoadingCache.put(i,new CompletableFuture<>());
//        }
//
//        Thread.sleep(1000);
//
//        System.err.println(cache.asMap());

    }
















    class User implements Comparable<User> {
        public User(int age) {
            this.age = age;
        }

        public User(String name, int age) {
            this.name = name;
            this.age = age;
        }

        private String name;
        private int age;

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public int compareTo(User o) {
            return this.getAge() - o.getAge();
        }

        @Override
        public String toString() {
            return "User{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    '}';
        }
    }

}
