package nachos.threads;

import java.util.*;
import nachos.machine.*;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * The priority queue we will use to keep track of which threads need to
     * be woken up at which times. With this data structure we get O(log n)
     * enqueueing and dequeueing of the item with minimum priority. We also
     * get constant time "peeking" at the item with minimum priority.
     */
    private PriorityQueue<ThreadHolder> threadHeap;

    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
    public Alarm() {
        Machine.timer().setInterruptHandler(new Runnable() {
                public void run() { timerInterrupt(); }
        });
        threadHeap = new PriorityQueue<ThreadHolder>();
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
        Machine.interrupt().disable();
        long clock = Machine.timer().getTime();
        while (!threadHeap.isEmpty() && threadHeap.peek().priority <= clock) {
            ThreadHolder curr = threadHeap.poll();
            curr.thread.ready();
        }
        Machine.interrupt().enable();
        KThread.currentThread().yield();
    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param x the minimum number of clock ticks to wait.
     *
     * @see nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
        Machine.interrupt().disable();
        long wakeTime = Machine.timer().getTime() + x;
        ThreadHolder threadHolder = new ThreadHolder();
        threadHolder.priority = wakeTime;
        threadHolder.thread = KThread.currentThread();
        threadHeap.add(threadHolder);
        KThread.sleep();
    }

    /**
     * Initiates two tests, a basic timing test, and a test that checks
     * if threads with interleaved wakeup times work correctly. This test
     * will generally take a long time. As it is currently written, it will
     * take at least 10^8 clock ticks to run.
     */
    public static void selfTest() {
        System.out.print("Initiating Alarm timing correctness test... ");
        KThread curr;
        final Alarm alarm = new Alarm();
        final boolean[] arr = { true };
        final int shortPeriod = 500000;
        final int longPeriod = 5000000;
        for (int i = 0; i < 50; i++) {
            curr = new KThread(new Runnable() {
                public void run() {
                    long t1 = Machine.timer().getTime();
                    alarm.waitUntil(shortPeriod);
                    long t2 = Machine.timer().getTime();
                    if (t2 - t1 < shortPeriod) {
                        arr[0] = false;
                    }
                }
            });
            curr.fork();
            curr.join();
        }
        if (arr[0]) {
            System.out.println("passed.\n");
        } else {
            System.out.println("failed.\n");
        }

        // Uses longPeriod to try to get around scheduler inconsistencies.
        System.out.print("Initiating Alarm interleave test... ");
        long clock = Machine.timer().getTime();
        final Lock listLock = new Lock();
        final LinkedList<Integer> list = new LinkedList<Integer>();
        final int[] threadNum = { 0 };
        for (int i = 0; i < 3; i++) {
            threadNum[0] = i;
            curr = new KThread(new Runnable() {
                public void run() {
                    int offset = threadNum[0];
                    for (int j = 0; j < 5; j++) {
                        alarm.waitUntil(3 * longPeriod);
                        listLock.acquire();
                        list.add(j * 3 + offset);
                        listLock.release();
                    }
                }
            });
            curr.fork();
            alarm.waitUntil(longPeriod);
            if (i == 2) {
                curr.join();
            }
        }
        int count = 0;
        boolean correct = true;
        if (Machine.timer().getTime() - clock < (2 + 3 * 5) * longPeriod) {
            correct = false;
        }
        while (!list.isEmpty()) {
            if (list.poll() != count) {
                correct = false;
            }
            count++;
        }
        if (correct) {
            System.out.println("passed.\n");
        } else {
            System.out.println("failed.\n");
        }
    }
}

class ThreadHolder implements Comparable<ThreadHolder>{
    public KThread thread;
    public long priority;

    public int compareTo(ThreadHolder t) {
        return new Long(this.priority).compareTo(new Long(t.priority));
    }
}
