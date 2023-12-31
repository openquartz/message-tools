package com.openquartz.messagetools.packing;

public class MessageSizeStagingQueueTest {

    public static void main(String[] args) throws InterruptedException {

        MessageStagingQueue<Integer> stagingQueue =
            new MessageSizeStagingQueue<>(new LogMessagePackingListener<>(), 100, 8000);

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
