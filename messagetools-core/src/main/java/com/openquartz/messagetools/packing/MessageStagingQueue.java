package com.openquartz.messagetools.packing;

/**
 * MessageStagingQueue
 *
 * @param <T> T
 */
public interface MessageStagingQueue<T> {

    /**
     * put message
     *
     * @param value value message
     */
    void put(T value);

    /**
     * add message
     *
     * @param value value
     */
    default void add(T value) {
        put(value);
    }

    /**
     * put and flush message
     *
     * @param value value message
     */
    default void putAndFlush(T value) {

        // put message
        put(value);

        // flush message
        flush();
    }

    /**
     * flush message
     */
    void flush();

}
