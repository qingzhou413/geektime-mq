package cn.qingzhou413.geektime.mq.cas;

import lombok.AllArgsConstructor;
import lombok.Getter;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author qingzhou
 * 2019-09-04
 */
public class CasDemo {

    @AllArgsConstructor
    static class IntHolder {
        @Getter
        private volatile int number;
        private static Unsafe unsafe;
        private static final long valueOffset;

        static {
            try {
                Field f=Unsafe.class.getDeclaredField("theUnsafe");
                f.setAccessible(true);
                unsafe = (Unsafe) f.get(null);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            try {
                valueOffset = unsafe.objectFieldOffset
                        (IntHolder.class.getDeclaredField("number"));
            } catch (Exception ex) { throw new Error(ex); }
        }

        public void add() {
            while (true) {
                int num = number;
                boolean b = unsafe.compareAndSwapInt(this, valueOffset, num, num + 1);
                if (b) {
                    break;
                }
                Thread.yield();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        int threadCnt = 100;
        int numPerThread = 100000;

        Thread[] ts = new Thread[threadCnt];

        final IntHolder num = new IntHolder(0);

        long start = System.currentTimeMillis();
        for (int i = 0; i < threadCnt; i++) {
            Thread thread = new Thread(()->{
                int x = 0;
                while (x++ < numPerThread) {
                    num.add();
                }
            });
            ts[i] = thread;
            thread.start();
        }
        for (int i = 0; i <threadCnt; i++) {
            ts[i].join();
        }
        System.out.println("result " + num.getNumber() + " time spend " + (System.currentTimeMillis() - start));
    }
}
