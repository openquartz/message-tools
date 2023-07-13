package com.openquartz.messagetools.packing;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * LogMessage Packing Listener
 *
 * @param <T> T
 * @author svnee
 */
public class LogMessagePackingListener<T> implements MessagePackingListener<T> {

    private final AtomicInteger total = new AtomicInteger(0);

    @Override
    public void onPacking(List<T> messageList) {
        System.out.println(messageList);
        int current = total.addAndGet(messageList.size());
        System.out.println("current: " + current);
    }
}
