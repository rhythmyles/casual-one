package ltd.lant.casualone.test;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
        User zhangsan = new User("zhangsan", 18);
        User lisi = null;
        User wangwu = new User("wangwu", 81);
        User user = Optional.ofNullable(zhangsan).orElse(wangwu);
        User user1 = Optional.ofNullable(lisi).orElse(wangwu);

        Optional<User> optUser1 = Optional.of(user);
        User user2 = optUser1.get();

        Optional.ofNullable(user1).ifPresent(p -> System.out.println("年龄"+p.getAge()));

    }

    @Test
    void assertTest() {
        try {
            User user = null;
            //     user = new User("zhaoliu", 80);
            assert user != null;
            System.out.println(user.getName());
        } catch (Exception | Error e) {
            System.err.println("Exception | Error");
        }
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
    void dateTest() {

        Date date1 = new Date(0L); // 0时区的0点,东8区的8点
        Date date2 = new Date(3600 * 1000L); //0时区的1点,东8区的9点
        Date date3 = new Date(7200L * 1000); //0时区的3点,东8区的11点

        Date date4 = new Date(date1.getTime() + date2.getTime());
        Date date5 = new Date(date1.getTime() + date2.getTime() + date3.getTime());
        System.out.println(date4);
        System.out.println(date5);
    }

    @Test
    void integerTest() throws NoSuchFieldException, IllegalAccessException {
        Class cache = Integer.class.getDeclaredClasses()[0];
        Field myCache = cache.getDeclaredField("cache");
        myCache.setAccessible(true);

        Integer[] newCache = (Integer[]) myCache.get(cache);
        newCache[132] = newCache[138];
        int a = 2;
        int b = a + a;
        System.out.printf("%d + %d = %d", a, a, b);
    }



    class User {
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
    }


}
