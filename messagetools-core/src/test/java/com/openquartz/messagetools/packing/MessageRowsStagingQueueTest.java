package com.openquartz.messagetools.packing;

import org.junit.Test;

public class MessageRowsStagingQueueTest {

    @Test
    public void test() throws InterruptedException {

        MessageStagingQueue<Integer> stagingQueue =
            new MessageRowsStagingQueue<>(new LogMessagePackingListener<>(), 10, 800);

        new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                stagingQueue.put(i);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(() -> {
            for (int i = 100; i < 200; i++) {
                stagingQueue.put(i);

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        Thread.sleep(10000000);
    }

}
