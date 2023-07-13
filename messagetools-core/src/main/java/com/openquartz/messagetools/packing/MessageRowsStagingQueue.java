package com.openquartz.messagetools.packing;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MessageRowsStagingQueue
 *
 * @author svnee
 */
public class MessageRowsStagingQueue<T> implements MessageStagingQueue<T> {

    /**
     * 暂存数据队列
     */
    private final LinkedList<T> stagingQueue = new LinkedList<>();

    /**
     * 对应时间戳队列
     */
    private final LinkedList<Long> timestampQueue = new LinkedList<>();

    /**
     * 现存长度
     */
    private final AtomicInteger length = new AtomicInteger(0);

    /**
     * 打包监听
     */
    private final MessagePackingListener<T> listener;

    /**
     * 打包批次
     */
    private final Integer packingBatch;

    public MessageRowsStagingQueue(MessagePackingListener<T> listener, Integer packingBatch, long delayTimeOut) {
        this.listener = listener;
        this.packingBatch = packingBatch;

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(() -> {

                Long first = this.timestampQueue.peekFirst();
                long lastTimestamp = System.currentTimeMillis() - delayTimeOut;
                // 超期
                if (first != null && first.compareTo(lastTimestamp) < 0) {

                    synchronized (this) {
                        List<T> flushResultList = new ArrayList<>();
                        int len = 0;
                        while (!timestampQueue.isEmpty() && timestampQueue.peek().compareTo(lastTimestamp) < 0) {
                            timestampQueue.pop();
                            flushResultList.add(stagingQueue.pop());
                            len++;
                        }

                        length.getAndAdd(-len);

                        autoFlush(flushResultList);
                    }
                }
            },
            0, 10, TimeUnit.MILLISECONDS);

    }

    @Override
    public synchronized void put(T value) {
        int i = length.incrementAndGet();
        timestampQueue.add(System.currentTimeMillis());
        stagingQueue.add(value);
        if (i >= packingBatch) {
            flush();
        }
    }

    private void autoFlush(List<T> messageList) {
        listener.onPacking(messageList);
    }

    @Override
    public synchronized void putAndFlush(T value) {
        MessageStagingQueue.super.putAndFlush(value);
    }

    @Override
    public synchronized void flush() {

        autoFlush(new ArrayList<>(stagingQueue));

        timestampQueue.clear();
        stagingQueue.clear();
        length.compareAndSet(length.get(), 0);
    }
}