package nachos.threads;

import nachos.machine.*;
import nachos.threads.PriorityScheduler.PriorityQueue;
import nachos.threads.PriorityScheduler.ThreadState;

import java.util.LinkedList;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;

/**
 * A scheduler that chooses threads using a lottery.
 *
 * <p>
 * A lottery scheduler associates a number of tickets with each thread. When a
 * thread needs to be dequeued, a random lottery is held, among all the tickets
 * of all the threads waiting to be dequeued. The thread that holds the winning
 * ticket is chosen.
 *
 * <p>
 * Note that a lottery scheduler must be able to handle a lot of tickets
 * (sometimes billions), so it is not acceptable to maintain state for every
 * ticket.
 *
 * <p>
 * A lottery scheduler must partially solve the priority inversion problem; in
 * particular, tickets must be transferred through locks, and through joins.
 * Unlike a priority scheduler, these tickets add (as opposed to just taking
 * the maximum).
 */
public class LotteryScheduler extends PriorityScheduler {
    /**
     * Allocate a new lottery scheduler.
     */
    /**
     * The minimum number of tickets that a thread can have.
     */
    public static final int priorityMinimum = 1;
    /**
     * The maximum number of tickets that a thread can have.
     */
    public static final int priorityMaximum = Integer.MAX_VALUE;
 
    public LotteryScheduler() {
    }
    
    /**
     * Allocate a new lottery thread queue.
     *
     * @param	transferPriority	<tt>true</tt> if this queue should
     *					transfer tickets from waiting threads
     *					to the owning thread.
     * @return	a new lottery thread queue.
     */
    public LotteryQueue newThreadQueue(boolean transferPriority) {
	// implement me
    	return new LotteryQueue(transferPriority);
    }
   
    protected LThreadState getThreadState(KThread thread) {
        if (thread.schedulingState == null)
            thread.schedulingState = new LThreadState(thread);

        return (LThreadState) thread.schedulingState;
    }
    
    
    protected class LotteryQueue extends PriorityQueue {
    	
    	protected LThreadState holder;
    	protected java.util.LinkedList<LThreadState> realQueue;
    	
    	LotteryQueue(boolean transferPriority){
    		super(transferPriority);
    		this.realQueue = new java.util.LinkedList<LThreadState>();
    	}
    	
    	public void waitForAccess(KThread thread) {
             Lib.assertTrue(Machine.interrupt().disabled());
             LThreadState threadState = getThreadState(thread);
             threadState.waitForAccess(this);
             this.realQueue.add(threadState);
             if (this.holder != null && this.transferPriority) {
            	 this.holder.effPriority = this.holder.recalculate();
            	 this.holder.notifyPriorityChange();
             }
        }
    	
    	public void acquire(KThread thread) {
            Lib.assertTrue(Machine.interrupt().disabled());
            this.holder = getThreadState(thread);
            this.holder.acquire(this);
        }
    	
    	public KThread nextThread(){    	
    		Lib.assertTrue(Machine.interrupt().disabled());
    		LThreadState picked_t = pickNextThread(),
    					oldHolder = this.holder;
    		
    		this.holder = picked_t;
       		if (this.holder == null){  //queue is empty
    			return null;
    		} 
    		realQueue.remove(picked_t);
    		acquire(picked_t.thread);
    		
    		if (oldHolder != null) {
                oldHolder.release(this);
            }
    		
    		this.holder.effPriority = holder.recalculate();
    		return picked_t.thread;
    	}
    	
    	protected LThreadState pickNextThread(){
    		Lib.assertTrue(Machine.interrupt().disabled());
    		if (realQueue.isEmpty()){ //queue is empty
    			return null;
    		}
    		int rando = (int)(Math.random() * numTickets()); //pick a random ticket
    		for (LThreadState t : realQueue){
    			rando -= t.getEffectivePriority();
    			if (rando <= 0){ //we have found the holder of random ticket
    				return t;
    			}
    		}
    		return null;
    	}
    	
    	/**
    	 * calculate number of tickets held by all threads in queue
    	 * @return num of tickets in queue
    	 */
    	protected int numTickets(){
    		int total = 0;
    		for (LThreadState t : realQueue){
    			total += t.getEffectivePriority();
    		}
    		return total;
       }
    }
    
    protected class LThreadState extends ThreadState{
    	protected LinkedList<LotteryQueue> heldQueues;
    	protected LotteryQueue waitQueue;
    	
    	LThreadState(KThread thread){
    		super(thread);
    		heldQueues = new LinkedList<LotteryQueue>();
    	}
    	
    	public void waitForAccess(LotteryQueue waitQueue) {
            this.waitQueue = waitQueue;
        }
    	
    	 protected void release(LotteryQueue heldQueue) {
             heldQueues.remove(heldQueue);
             effPriority = recalculate();
             notifyPriorityChange();
         }
    	
    	 public void acquire(LotteryQueue waitQueue) {
    		 this.heldQueues.add(waitQueue);
    		 this.waitQueue = null;
         }
    	 
    	
    	 protected void notifyPriorityChange() {
             if (waitQueue != null && waitQueue.holder != null && waitQueue.transferPriority) {
            	 int a = waitQueue.holder.recalculate();
            	 waitQueue.holder.effPriority = a;
            	 waitQueue.holder.notifyPriorityChange();
             }
         }

    	 
    	 
    	protected int recalculate(){
    		int effpriority = priority;
    		for (LotteryQueue q : heldQueues){
    			effpriority += q.numTickets();
    		}
    		return effpriority;
    	}
    }
    
    /**
     * this is where the testing methods begin
     */
    public static void selfTest(){
    	boolean testDonation = true;
    	boolean testCascade = true;
    	
    	if (testDonation){
    		donationTest();
    	}
    	
    	if (testCascade){
    		cascadeTest();
    	}
    }
    
    public static void cascadeTest(){
    	LotteryScheduler x = (LotteryScheduler) ThreadedKernel.scheduler,
    				     y = (LotteryScheduler) ThreadedKernel.scheduler,
    					 z = (LotteryScheduler) ThreadedKernel.scheduler;
    	
    	LotteryQueue r1 = x.newThreadQueue(true),
    				 r2 = y.newThreadQueue(true),
    				 r3 = z.newThreadQueue(true);
    	
    	KThread t1, t2, t3;
    	t1 = new KThread(new LotteryRunnable(1, "t1"));
    	t2 = new KThread(new LotteryRunnable(3, "t2"));
    	t3 = new KThread(new LotteryRunnable(4, "t3"));
    	LThreadState l1, l2, l3;
    	l1 = x.getThreadState(t1);
    	l2 = y.getThreadState(t2);
    	l3 = z.getThreadState(t3);

    	Machine.interrupt().disable();
    	x.setPriority(t1, 1);
    	Machine.interrupt().enable();
    	
    	Machine.interrupt().disable();
    	y.setPriority(t2, 2);
    	Machine.interrupt().enable();
    	
    	Machine.interrupt().disable();
    	z.setPriority(t3, 3);
    	Machine.interrupt().enable();
    	
    	Machine.interrupt().disable();
    	r1.acquire(t1);
    	Machine.interrupt().enable();
    	Machine.interrupt().disable();
    	r2.acquire(t2);
    	Machine.interrupt().enable();
    	Machine.interrupt().disable();
    	r3.acquire(t3);
    	Machine.interrupt().enable();
    	
    	Machine.interrupt().disable();
    	r1.waitForAccess(t2);
    	Machine.interrupt().enable();
    	Machine.interrupt().disable();
    	r2.waitForAccess(t3);
    	Machine.interrupt().enable();
    	
    	Machine.interrupt().disable();
    	Lib.assertTrue(r1.holder.effPriority == 6);
    	Machine.interrupt().enable();
    	Machine.interrupt().disable();
    	Lib.assertTrue(r2.holder.effPriority == 5);
    	Machine.interrupt().enable();
    	Machine.interrupt().disable();
    	Lib.assertTrue(r3.holder.effPriority == 3);
    	Machine.interrupt().enable();
    	
    	Machine.interrupt().disable();
    	r2.nextThread();
    	Machine.interrupt().enable();
    	
    	Machine.interrupt().disable();
    	Lib.assertTrue(r1.holder.effPriority == 3);
    	Machine.interrupt().enable();
    	
    	Machine.interrupt().disable();
    	Lib.assertTrue(r2.holder.effPriority == 3);
    	Machine.interrupt().enable();
    	
    	Machine.interrupt().disable();
    	Lib.assertTrue(r1.holder.effPriority == 3);
    	Machine.interrupt().enable();
    	
    	Machine.interrupt().disable();
    	r1.nextThread();
    	Machine.interrupt().enable();
    	
    	Machine.interrupt().disable();
    	Lib.assertTrue(r1.holder.effPriority == 2);
    	Machine.interrupt().enable();
    	
    	System.out.println("PASSED!");
    }
    
    public static void donationTest(){
    	LotteryScheduler a = (LotteryScheduler) ThreadedKernel.scheduler;
    	LotteryQueue r1 = a.newThreadQueue(true);
    	KThread t1, t2, t3;
    	t1 = new KThread(new LotteryRunnable(1, "t1"));
    	t2 = new KThread(new LotteryRunnable(3, "t2"));
    	t3 = new KThread(new LotteryRunnable(4, "t3"));
    	LThreadState l1, l2, l3;
    	l1 = a.getThreadState(t1);
    	l2 = a.getThreadState(t2);
    	l3 = a.getThreadState(t3);
    	
    	Machine.interrupt().disable();
    	a.setPriority(t1, 1);
    	Machine.interrupt().enable();
    	
    	Machine.interrupt().disable();
    	a.setPriority(t2, 3);
    	Machine.interrupt().enable();
    	
    	Machine.interrupt().disable();
    	a.setPriority(t3, 4);
    	Machine.interrupt().enable();
    	
    	Machine.interrupt().disable();
    	r1.acquire(t1);
    	Machine.interrupt().enable();
    	
    	Machine.interrupt().disable();
    	r1.waitForAccess(t2);
    	Machine.interrupt().enable();
    	
    	Machine.interrupt().disable();
    	r1.waitForAccess(t3);
    	Machine.interrupt().enable();
    	
    	Machine.interrupt().disable();
    	Lib.assertTrue(r1.holder.effPriority == 8);
    	Machine.interrupt().enable();
    	
    	Machine.interrupt().disable();
    	r1.nextThread();
    	Machine.interrupt().enable();
    	
    	Machine.interrupt().disable();
    	Lib.assertTrue(r1.holder.effPriority == 7);
    	Machine.interrupt().enable();
    	
    	Machine.interrupt().disable();
    	r1.nextThread();
    	Machine.interrupt().enable();
    	
    	Machine.interrupt().disable();
    	Lib.assertTrue(r1.holder.effPriority == 3 || r1.holder.effPriority == 4);
    	Machine.interrupt().enable();
    	
    	System.out.println("PASSED!");
    	
    }
    
    /**
     * basic runnable class to test LotteryScheduler
     * @author Shrayus
     *
     */
    public static class LotteryRunnable implements Runnable {
    	int priority;
    	String name;
    	LotteryRunnable(int priority, String name){
    		this.priority = priority;
    		this.name = name;
    	}
    	public int getPriority(){
    		return this.priority;
    	}
    	public void run(){
    		System.out.println(name + " : " + priority);
    	}
    }
}
