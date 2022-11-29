package timedelayqueue;

import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.lang.*;
import java.util.UUID;
import java.util.stream.Collectors;

// TODO: write a description for this class
// TODO: complete all methods, irrespective of whether there is an explicit TODO or not
// TODO: write clear specs
// TODO: State the rep invariant and abstraction function
// TODO: what is the thread safety argument?

public class TimeDelayQueue {

    private PriorityQueue<PubSubMessage> pq;
    private int delay;
    private int count = 0;
    private List<Long> operations = new ArrayList<>();

    private Map<PubSubMessage, Long> transients = new HashMap<>();

    // a comparator to sort messages
    private class PubSubMessageComparator implements Comparator<PubSubMessage> {
        public int compare(PubSubMessage msg1, PubSubMessage msg2) {
            return msg1.getTimestamp().compareTo(msg2.getTimestamp());
        }
    }

    /**
     * Create a new TimeDelayQueue
     * @param delay the delay, in milliseconds, that the queue can tolerate, >= 0
     */
    public TimeDelayQueue(int delay) {
        this.delay = delay;
        this.pq = new PriorityQueue<>(new PubSubMessageComparator());
    }

    // add a message to the TimeDelayQueue
    // if a message with the same id exists then
    // return false
    public synchronized boolean add(PubSubMessage msg) {
        if(!pq.stream()
                .map(PubSubMessage::getId)
                .anyMatch(uuid -> uuid == msg.getId())) {
            if(msg.isTransient()) {
                TransientPubSubMessage msgTrans = (TransientPubSubMessage) msg;
                transients.put(msg, msgTrans.getLifetime() + System.currentTimeMillis());
            }
            count++;
            operations.add(System.currentTimeMillis());
            return pq.add(msg);
        }

        return false;
    }

    /**
     * Get the count of the total number of messages processed
     * by this TimeDelayQueue
     * @return
     */
    public long getTotalMsgCount() {
        return count;
    }

    // return the next message and PubSubMessage.NO_MSG
    // if there is ni suitable message
    public synchronized PubSubMessage getNext() {
        Long currentTime = System.currentTimeMillis();
        List<PubSubMessage> removed = new ArrayList<>();
        Map<PubSubMessage, Long> filtered = transients.entrySet().stream()
                .filter(x -> {
                    if(x.getValue() < currentTime) {
                        removed.add(x.getKey());
                        return true;
                    }
                    return false;
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        transients = filtered;
        pq.removeAll(removed);

        if(pq.size() != 0) {
            PubSubMessage next = pq.poll();
            if(currentTime - next.getTimestamp().getTime() >= delay) {
                operations.add(currentTime);
                return next;
            }
        }
        operations.add(currentTime);
        return PubSubMessage.NO_MSG;
    }

    // return the maximum number of operations
    // performed on this TimeDelayQueue over
    // any window of length timeWindow
    // the operations of interest are add and getNext
    public synchronized int getPeakLoad(int timeWindow) {
        int peak = 0;
        for(int i = 0; i < operations.size(); i++) {
            int count = 0;
            for(int j = 0; j < operations.size(); j++) {
                if(Math.abs(operations.get(j) - operations.get(i)) < timeWindow) {
                    count++;
                }
            }
            if(count > peak) {
                peak = count;
            }
        }
        return peak;
    }
}
