package timedelayqueue;

public class MsgThread extends Thread {

    private final long start;

    public MsgThread() {
        start = System.currentTimeMillis();
    }

    @Override
    public void run()
    {
        System.out.println("Thread is running");
    }

//    public long runTime() {
//        return System.currentTimeMillis() - start;
//    }
}
