package com.openquartz.messagetools.splitter;

import com.openquartz.messagetools.utils.RamUsageEstimator;
import java.util.Iterator;
import java.util.List;

public class ListSplitter<T> implements Iterator<List<T>> {

    private final long sizeLimit;
    private final List<T> messages;
    private int currIndex;

    public ListSplitter(List<T> messages, long sizeLimit) {
        this.messages = messages;
        this.sizeLimit = sizeLimit;
    }

    @Override
    public boolean hasNext() {
        return currIndex < messages.size();
    }

    @Override
    public List<T> next() {
        int nextIndex = currIndex;
        int totalSize = 0;
        for (; nextIndex < messages.size(); nextIndex++) {
            T message = messages.get(nextIndex);
            long tmpSize = RamUsageEstimator.shallowSizeOf(message);
            tmpSize = tmpSize + 20;
            if (tmpSize > sizeLimit) {
                // 出乎意料的是，单条消息超过了 sizeLimit 这里就放手吧，否则会阻止切分过程
                if (nextIndex - currIndex == 0) {
                    // 如果下一个子列表没有元素，则添加此 SubList，然后 Break，否则只 break
                    nextIndex++;
                }
                break;
            }
            if (tmpSize + totalSize > sizeLimit) {
                break;
            } else {
                totalSize += (int) tmpSize;
            }

        }
        List<T> subList = messages.subList(currIndex, nextIndex);
        currIndex = nextIndex;
        return subList;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not allowed to remove");
    }
}
