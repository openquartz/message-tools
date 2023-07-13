package com.openquartz.messagetools.packing;

import com.openquartz.messagetools.utils.RamUsageEstimator;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * MessageSizeStagingQueue
 *
 * @author xuzhao
 */
public class MessageSizeStagingQueue<T> implements MessageStagingQueue<T> {

    /**
     * 暂存数据队列
     */
    private final LinkedList<T> stagingQueue = new LinkedList<>();

    /**
     * 对应时间戳队列
     */
    private final LinkedList<Long> timestampQueue = new LinkedList<>();

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

    public MessageSizeStagingQueue(MessagePackingListener<T> listener, long packingSize, long delayTimeOut) {
        this.listener = listener;
        this.packingSize = packingSize;

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(() -> {

                Long first = this.timestampQueue.peekFirst();
                long lastTimestamp = System.currentTimeMillis() - delayTimeOut;
                // 超期
                if (first != null && first.compareTo(lastTimestamp) < 0) {

                    synchronized (this) {
                        List<T> flushResultList = new ArrayList<>();
                        long size = 0;
                        while (!timestampQueue.isEmpty() && timestampQueue.peek().compareTo(lastTimestamp) < 0) {
                            timestampQueue.pop();
                            flushResultList.add(stagingQueue.pop());
                            size += sizeQueue.pop();
                        }

                        totalSize.getAndAdd(-size);

                        autoFlush(flushResultList);
                    }
                }
            },
            0, 10, TimeUnit.MILLISECONDS);

    }

    @Override
    public synchronized void put(T value) {

        long valueSize = RamUsageEstimator.shallowSizeOf(value);
        long size = totalSize.addAndGet(valueSize);

        timestampQueue.add(System.currentTimeMillis());
        stagingQueue.add(value);
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

        autoFlush(new ArrayList<>(stagingQueue));

        timestampQueue.clear();
        stagingQueue.clear();
        sizeQueue.clear();
        totalSize.compareAndSet(totalSize.get(), 0);
    }
}