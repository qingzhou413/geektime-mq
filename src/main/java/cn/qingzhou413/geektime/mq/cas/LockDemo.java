package cn.qingzhou413.geektime.mq.cas;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author qingzhou
 * 2019-09-04
 */
public class LockDemo {

    @AllArgsConstructor
    static class IntHolder {
        @Getter
        private int number;

        public void add() {
            number++;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        int threadCnt = 100;
        int numPerThread = 100000;

        Thread[] ts = new Thread[threadCnt];
        final IntHolder num = new IntHolder(0);

        long start = System.currentTimeMillis();
        Lock lock = new ReentrantLock();
        for (int i = 0; i < threadCnt; i++) {
            Thread thread = new Thread(() -> {
                int x = 0;
                while (x++ < numPerThread) {
                    try {
                        lock.lock();
                        num.add();
                    } finally {
                        lock.unlock();
                    }
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
