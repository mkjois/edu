package nachos.threads;

import java.util.*;
import nachos.machine.*;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *
 * @see nachos.threads.Condition
 */
public class Condition2 {
    /**
     * Allocate a new condition variable.
     *
     * @param   conditionLock   the lock associated with this condition
     *                          variable. The current thread must hold this
     *                          lock whenever it uses <tt>sleep()</tt>,
     *                          <tt>wake()</tt>, or <tt>wakeAll()</tt>.
     */
    public Condition2(Lock conditionLock) {
        this.conditionLock = conditionLock;
        waitQueue = ThreadedKernel.scheduler.newThreadQueue(true);
        Machine.interrupt().disable();
        waitQueue.acquire(KThread.currentThread());
        Machine.interrupt().enable();
    }

    /**
     * Atomically release the associated lock and go to sleep on this condition
     * variable until another thread wakes it using <tt>wake()</tt>. The
     * current thread must hold the associated lock. The thread will
     * automatically reacquire the lock before <tt>sleep()</tt> returns.
     */
    public void sleep() {
        Lib.assertTrue(conditionLock.isHeldByCurrentThread());
        Machine.interrupt().disable();
        conditionLock.release();
        waitQueue.waitForAccess(KThread.currentThread());
        KThread.sleep();
        conditionLock.acquire();
    }

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() {
        Lib.assertTrue(conditionLock.isHeldByCurrentThread());
        Machine.interrupt().disable();
        KThread nextThread = waitQueue.nextThread();
        if (nextThread != null) {
            nextThread.ready();
        }
        Machine.interrupt().enable();
    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() {
        Lib.assertTrue(conditionLock.isHeldByCurrentThread());
        Machine.interrupt().disable();
        KThread nextThread = waitQueue.nextThread();
        while (nextThread != null) {
            nextThread.ready();
            nextThread = waitQueue.nextThread();
        }
        Machine.interrupt().enable();
    }

    public static void selfTest() {
        System.out.print("Initiating Condition2 critical section test... ");
        final int[] arr = new int[1];
        final Lock conditionLock = new Lock();
        final boolean[] writer = new boolean[1];
        final Condition2 test = new Condition2(conditionLock);
        Runnable r = new Runnable() {
            public void run() {
                conditionLock.acquire();
                while (writer[0]) {
                    test.sleep();
                }
                writer[0] = true;
                conditionLock.release();
                int store = arr[0];
                for (int i = 0; i < 10; i++) {
                    KThread.yield();
                }
                arr[0] = store + 1;
                conditionLock.acquire();
                writer[0] = false;
                test.wake();
                conditionLock.release();
            }
        };
        LinkedList<KThread> forked;
        KThread curr;
        arr[0] = 0;
        writer[0] = false;
        for (int i = 0; i < 100; i++) {
            forked = new LinkedList<KThread>();
            for (int j = 0; j < 10; j++) {
                curr = new KThread(r);
                forked.add(curr);
                curr.fork();
            }
            while (!forked.isEmpty()) {
                forked.poll().join();
            }
        }
        if (arr[0] == 1000) {
            System.out.println("passed.\n");
        } else {
            System.out.println("failed.\n");
        }
    }

    private Lock conditionLock;

    private ThreadQueue waitQueue;
}
