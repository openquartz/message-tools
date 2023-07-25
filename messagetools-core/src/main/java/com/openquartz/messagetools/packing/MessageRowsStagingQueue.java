package com.openquartz.messagetools.packing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * MessageRowsStagingQueue
 *
 * @author svnee
 */
public class MessageRowsStagingQueue<T> implements MessageStagingQueue<T> {

    /**
     * 暂存数据队列
     */
    private final BlockingQueue<DelayMessage<T>> stagingQueue = new DelayQueue<>();

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

    /**
     * 延迟时间
     */
    private final long delayTime;

    public MessageRowsStagingQueue(MessagePackingListener<T> listener, Integer packingBatch, long delayTime) {
        this.listener = listener;
        this.packingBatch = packingBatch;
        this.delayTime = delayTime;
        new Thread(() -> {
            while (true) {
                try {
                    DelayMessage<T> delayMessage = stagingQueue.take();
                    T message = delayMessage.getMessage();

                    ArrayList<T> flushResultList = new ArrayList<>();
                    flushResultList.add(message);
                    length.addAndGet(-1);
                    autoFlush(flushResultList);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();


    }

    @Override
    public synchronized void put(T value) {
        int i = length.incrementAndGet();
        stagingQueue.add(new DelayMessage<>(value, delayTime));
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

        List<T> messageList = stagingQueue
            .stream()
            .map(DelayMessage::getMessage)
            .collect(Collectors.toList());

        autoFlush(messageList);

        stagingQueue.clear();
        length.compareAndSet(length.get(), 0);
    }
}