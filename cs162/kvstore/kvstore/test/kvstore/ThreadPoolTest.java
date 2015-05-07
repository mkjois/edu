package kvstore;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

public class ThreadPoolTest {
    static final int SIZE = 10; // must be at least 3
    static ThreadPool threadPool;
    static int[] arguments, answers, allzeros;
    static int[] results;
    
    @Test
    public void nullTest() {
    }
    
    //@BeforeClass
    public static void initArrays() {
	arguments = new int[3*SIZE];
	answers = new int[3*SIZE];
	allzeros = new int[3*SIZE];
	for (int i=0; i<SIZE; i++) {
	    int answer = fib(i);
	    arguments[i] = i;
	    answers[i] = answer;
	    arguments[i+SIZE] = i;
	    answers[i+SIZE] = answer;
	    arguments[i+2*SIZE] = i;
	    answers[i+2*SIZE] = answer;
	}
    }

    //@Test
    public void testZeroRatio() {
	results = new int[3*SIZE];
	threadPool = new ThreadPool(0);
	try {
	    for (int i=0; i<3*SIZE; i++) {
		FibRunnable job = new FibRunnable(this, i);
		threadPool.addJob(job);
	    }
	} catch (InterruptedException e) {
	    fail("A thread was interrupted");
	}
	threadPool.waitUntilDone(3*SIZE);
	assertArrayEquals("testZeroRatio() failed", allzeros, results);
    }
    
    //@Test
    public void testLowRatio() {
	results = new int[3*SIZE];
	threadPool = new ThreadPool(SIZE);
	try {
	    for (int i=0; i<3*SIZE; i++) {
		FibRunnable job = new FibRunnable(this, i);
		threadPool.addJob(job);
	    }
	} catch (InterruptedException e) {
	    fail("A thread was interrupted");
	}
	threadPool.waitUntilDone(3*SIZE);
	assertArrayEquals("testLowRatio() failed", answers, results);
    }
    
    //@Test
    public void testMediumRatio() {
	results = new int[3*SIZE];
	threadPool = new ThreadPool(3*SIZE);
	try {
	    for (int i=0; i<3*SIZE; i++) {
		FibRunnable job = new FibRunnable(this, i);
		threadPool.addJob(job);
	    }
	} catch (InterruptedException e) {
	    fail("A thread was interrupted");
	}
	threadPool.waitUntilDone(3*SIZE);
	assertArrayEquals("testMediumRatio() failed", answers, results);
    }
    
    //@Test
    public void testHighRatio() {
	results = new int[3*SIZE];
	threadPool = new ThreadPool(9*SIZE);
	try {
	    for (int i=0; i<3*SIZE; i++) {
		FibRunnable job = new FibRunnable(this, i);
		threadPool.addJob(job);
	    }
	} catch (InterruptedException e) {
	    fail("A thread was interrupted");
	}
	threadPool.waitUntilDone(3*SIZE);
	assertArrayEquals("testHighRatio() failed", answers, results);
    }
    
    //@Test
    public void testBlocking() {
	int numJobsAdded = 0;
	results = new int[3*SIZE];
	threadPool = new ThreadPool(SIZE);
	try {
	    for (int i=0; i<1; i++) {
		FibRunnable job = new FibRunnable(this, i);
		threadPool.addJob(job);
		numJobsAdded++;
	    }
	    threadPool.waitUntilDone(numJobsAdded);
	    for (int i=1; i<3; i++) {
		FibRunnable job = new FibRunnable(this, i);
		threadPool.addJob(job);
		numJobsAdded++;
	    }
	    threadPool.waitUntilDone(numJobsAdded);
	    for (int i=3; i<SIZE; i++) {
		FibRunnable job = new FibRunnable(this, i);
		threadPool.addJob(job);
		numJobsAdded++;
	    }
	    threadPool.waitUntilDone(numJobsAdded);
	    for (int i=SIZE; i<3*SIZE; i++) {
		FibRunnable job = new FibRunnable(this, i);
		threadPool.addJob(job);
		numJobsAdded++;
	    }
	    threadPool.waitUntilDone(numJobsAdded);
	} catch (InterruptedException e) {
	    fail("A thread was interrupted");
	}
	assertArrayEquals("testBlocking() failed", answers, results);
    }
    
    private static int fib(int n) {
	if (n==0) return 0;
	if (n==1) return 1;
	return fib(n-1)+fib(n-2);
    }

    private class FibRunnable implements Runnable {
	ThreadPoolTest tester;
	int index;
	
	public FibRunnable(ThreadPoolTest tester, int index) {
	    this.tester = tester;
	    this.index = index;
	}
	
	@Override
	public void run() {
	    int arg = tester.arguments[index];
	    int result = fib(arg);
	    synchronized (tester) {
		tester.results[index] = result;
	    }
	}
	
	private int fib(int n) {
	    if (n==0) return 0;
	    if (n==1) return 1;
	    return fib(n-1)+fib(n-2);
	}
    }
}
