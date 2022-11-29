package timedelayqueue;

import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.lang.*;
import java.util.UUID;

// TODO: write a description for this class
// TODO: complete all methods, irrespective of whether there is an explicit TODO or not
// TODO: write clear specs
// TODO: State the rep invariant and abstraction function
// TODO: what is the thread safety argument?

class MsgThread extends Thread {
    private final long start;
    private final long end;

    private volatile boolean isRunning;

    public MsgThread() {
        this.start = System.currentTimeMillis();
        this.end = Integer.MAX_VALUE;               //change later
        this.isRunning = true;
    }

    public MsgThread(int msgLifetime) {
        long current = System.currentTimeMillis();
        this.start = current;
        this.end = current + msgLifetime;
        this.isRunning = true;
    }

    @Override
    public void run()
    {
        System.out.println("Thread is running");
        while(isRunning == true) {
            if(System.currentTimeMillis() >= end) {
                isRunning = false;
            }
        }
    }

    public long runTime() {
        return System.currentTimeMillis() - start;
    }

    public boolean isContinuing() {
        return isRunning;
    }
}

class TransMSG extends Thread {

}

public class TimeDelayQueue {

    private PriorityQueue<PubSubMessage> pq;
    private int delay;
    private int count = 0;
    private List<Long> operations = new ArrayList<>();
    MsgThread msgThread;

    private Map<TransientPubSubMessage, MsgThread> transientThreads = new HashMap<>();

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
        this.msgThread = new MsgThread();
        msgThread.start();
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
                transientThreads.put(msgTrans, new MsgThread(msgTrans.getLifetime()));
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
        while(pq.size() != 0) {
        }
        if(msgThread.runTime() >= delay) {
            operations.add(System.currentTimeMillis());
            return pq.poll();
        }
        operations.add(System.currentTimeMillis());
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
