package com.openquartz.messagetools.packing;

import com.openquartz.messagetools.utils.RamUsageEstimator;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * MessageSizeStagingQueue
 *
 * @author xuzhao
 */
public class MessageSizeStagingQueue<T> implements MessageStagingQueue<T> {

    /**
     * 暂存数据队列
     */
    private final BlockingQueue<DelayMessage<T>> stagingQueue = new DelayQueue<>();

    /**
     * 暂存数据大小
     */
    private final LinkedList<Long> sizeQueue = new LinkedList<>();

    /**
     * 总大小
     */
    private final AtomicLong totalSize = new AtomicLong(0);

    /**
     * 打包监听
     */
    private final MessagePackingListener<T> listener;

    /**
     * 打包批次
     */
    private final Long packingSize;

    /**
     * delay time
     */
    private final long delayTime;

    public MessageSizeStagingQueue(MessagePackingListener<T> listener, long packingSize, long delayTime) {
        this.listener = listener;
        this.packingSize = packingSize;
        this.delayTime = delayTime;

        new Thread(() -> {
            while (true) {
                try {
                    DelayMessage<T> delayMessage = stagingQueue.take();
                    T message = delayMessage.getMessage();

                    // 扣减总数
                    totalSize.addAndGet(-sizeQueue.pop());

                    // flush result
                    ArrayList<T> flushResultList = new ArrayList<>();
                    flushResultList.add(message);
                    autoFlush(flushResultList);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    @Override
    public synchronized void put(T value) {

        long valueSize = RamUsageEstimator.shallowSizeOf(value);
        long size = totalSize.addAndGet(valueSize);

        stagingQueue.add(new DelayMessage<>(value, delayTime));
        sizeQueue.add(valueSize);

        if (size >= packingSize) {
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
        sizeQueue.clear();
        totalSize.compareAndSet(totalSize.get(), 0);
    }
}