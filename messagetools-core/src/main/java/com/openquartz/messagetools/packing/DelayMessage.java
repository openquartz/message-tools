package com.openquartz.messagetools.packing;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * DelayMessage
 *
 * @param <T> T
 * @author svnee
 */
public class DelayMessage<T> implements Delayed {

    /**
     * 消息
     */
    private final T message;

    /**
     * 延迟时间
     */
    private final long delayTime;

    /**
     * 过期时间
     */
    private final long expireTime;

    public DelayMessage(T message, long delayTime) {
        this.message = message;
        this.delayTime = delayTime;
        this.expireTime = System.currentTimeMillis() + delayTime;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(this.expireTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        long f = this.getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS);
        return (int) f;
    }

    public T getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "DelayMessage{" +
            "message=" + message +
            ", delayTime=" + delayTime +
            ", expireTime=" + expireTime +
            '}';
    }
}
