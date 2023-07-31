package com.openquartz.messagetools.packing;

import java.util.List;

/**
 * Message Packing Listener
 * @param <T>
 * @author svnee
 */
@FunctionalInterface
public interface MessagePackingListener<T> {

    /**
     * on packing message
     * @param messageList packing result
     */
    void onPacking(List<T> messageList);

}
