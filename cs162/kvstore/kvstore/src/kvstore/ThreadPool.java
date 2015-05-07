package kvstore;

import java.util.LinkedList;

public class ThreadPool {

    /* Array of threads in the threadpool */
    private Thread threads[];
    private LinkedList<Runnable> jobQueue;
    private int jobsFinished; // for testing purposes only


    /**
     * Constructs a Threadpool with a certain number of threads.
     *
     * @param size number of threads in the thread pool
     */
    public ThreadPool(int size) {
        this.threads = new Thread[size];
        this.jobQueue = new LinkedList<Runnable>();
        for (int i=0; i<size; i++) {
            this.threads[i] = new WorkerThread(this);
        }
        for (int i=0; i<size; i++) {
            this.threads[i].start();
        }
    }

    /**
     * Add a job to the queue of jobs that have to be executed. As soon as a
     * thread is free, the thread will retrieve a job from this queue if
     * if one exists and start processing it.
     *
     * @param r job that has to be executed
     * @throws InterruptedException if thread is interrupted while in blocked
     *         state. Your implementation may or may not actually throw this.
     */
    public synchronized void addJob(Runnable r) throws InterruptedException {
	if (r == null) return;
        this.jobQueue.offer(r);
        this.notify();
    }

    /**
     * Block until a job is present in the queue and retrieve the job
     * @return A runnable task that has to be executed
     * @throws InterruptedException if thread is interrupted while in blocked
     *         state. Your implementation may or may not actually throw this.
     */
    private synchronized Runnable getJob() throws InterruptedException {
        while (this.jobQueue.isEmpty()) {
            this.wait();
        }
        return this.jobQueue.poll();
    }
    
    /**
     * For testing purposes only.
     */
    public synchronized void waitUntilDone(int numJobs) {
	if (this.threads.length == 0) return;
	while (this.jobsFinished < numJobs) {
	    try {
		this.wait(1000);
	    } catch (InterruptedException e) {
		// ignore
	    }
	}
    }
    
    /**
     * For testing purposes only.
     */
    private synchronized void finishJob() {
	this.jobsFinished++;
    }

    /**
     * A thread in the thread pool.
     */
    private class WorkerThread extends Thread {

        private ThreadPool threadPool;

        /**
         * Constructs a thread for this particular ThreadPool.
         *
         * @param pool the ThreadPool containing this thread
         */
        public WorkerThread(ThreadPool pool) {
            threadPool = pool;
        }

        /**
         * Scan for and process tasks.
         */
        @Override
        public void run() {
            Runnable job;
            while (true) {
        	try {
        	    job = this.threadPool.getJob();
        	    if (job != null) {
        		job.run();
        		this.threadPool.finishJob(); // for testing purposes only
        	    }
        	} catch (InterruptedException e) {
        	    // ignore
        	}
            }
        }
    }
}
