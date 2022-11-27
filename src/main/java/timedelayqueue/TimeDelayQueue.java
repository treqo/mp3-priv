package timedelayqueue;

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

    public MsgThread() {
        start = System.currentTimeMillis();
        end = -1; //change later
    }

    public MsgThread(int msgLifetime) {
        long current = System.currentTimeMillis();
        this.start = current;
        this.end = current + msgLifetime;
    }

    @Override
    public void run()
    {
        System.out.println("Thread is running");
    }

    public long runTime() {
        return System.currentTimeMillis() - start;
    }

    public boolean isContinuing() {
        if(System.currentTimeMillis() < end || end == -1) {
            return true;
        }
        return false;
    }
}

public class TimeDelayQueue {

    private PriorityQueue<PubSubMessage> pq;
    private int delay;
    private int count = 0;

    MsgThread msgThread;

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
    public boolean add(PubSubMessage msg) {
        if(!pq.stream()
                .map(PubSubMessage::getId)
                .anyMatch(uuid -> uuid == msg.getId())) {
            count++;
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
    public PubSubMessage getNext() {

        if(msgThread.runTime() >= delay) {
            return pq.poll();
        }
        return PubSubMessage.NO_MSG;
    }

    // return the maximum number of operations
    // performed on this TimeDelayQueue over
    // any window of length timeWindow
    // the operations of interest are add and getNext
    public int getPeakLoad(int timeWindow) {
        return -1;
    }

}
