package kvstore;

import static org.junit.Assert.*;

import java.util.Comparator;
import java.util.Random;
import java.util.Collections;
import java.util.LinkedList;

import junit.framework.AssertionFailedError;

import org.junit.Test;

public class ConsistentHashTest {
    
    @Test
    public void testRegistration() {
	testReg(2, 0);
	testReg(2, 2);
	testReg(2, 3);
	testReg(5, 2);
	testReg(5, 5);
	testReg(5, 7);
	testReg(15, 10);
	testReg(15, 15);
	testReg(15, 25);
	testReg(100, 50);
	testReg(100, 100);
	testReg(100, 150);
	System.out.println("registration tests pass");
    }
    
    @Test
    public void testHashing() {
	testHash(2);
	testHash(5);
	testHash(15);
	testHash(40);
	testHash(100);
	System.out.println("hashing tests pass");
    }

    private void testReg(int expected, int actual) {
	Random r = new Random();
	int minimum = Math.min(expected, actual);
	TPCMaster master = new TPCMaster(expected, null);
	TPCSlaveInfo[] expSlaves = new TPCSlaveInfo[expected],
		       actSlaves = new TPCSlaveInfo[actual];
	Thread[] threads = new Thread[3];
	for (int i=0; i<threads.length; i++) {
	    threads[i] = new Thread(new TestRunnable(master));
	    threads[i].start();
	}
	for (int i=0; i<threads.length; i++) {
	    try {
		threads[i].join(20L);
	    } catch (InterruptedException e) {
	    }
	}
	for (int i=0; i<minimum; i++) {
	    long id = r.nextLong();
	    expSlaves[i] = new TPCSlaveInfo(id);
	    actSlaves[i] = new TPCSlaveInfo(id);
	}
	for (int i=minimum; i<expected; i++) {
	    expSlaves[i] = new TPCSlaveInfo(r.nextLong());
	}
	for (int i=minimum; i<actual; i++) {
	    actSlaves[i] = new TPCSlaveInfo(r.nextLong());
	}
	for (int i=0; i<actual; i++) {
	    master.registerSlave(actSlaves[i]);
	}
	assertEquals("not the right number of slaves registered",
		minimum, master.size());
	for (int i=0; i<minimum; i++) {
	    assertTrue("not all slaves registered", master.contains(expSlaves[i]));
	}
	for (int i=minimum; i<actual; i++) {
	    assertFalse("too many slaves registered", master.contains(actSlaves[i]));
	}
	for (int i=minimum; i<expected; i++) {
	    assertFalse("too many slaves registered", master.contains(expSlaves[i]));
	}
	for (int i=0; i<minimum; i++) {
	    int sizeBefore = master.size();
	    for (int j=0; j<10; j++) {
		master.registerSlave(actSlaves[r.nextInt(minimum)]);
	    }
	    assertEquals("repeated slave registration", sizeBefore, master.size());
	}
	for (int i=0; i<threads.length; i++) {
	    try {
		threads[i].join(20L);
	    } catch (InterruptedException e) {
	    }
	}
	assertThreadState(threads, actual < expected ?
		Thread.State.WAITING : Thread.State.TERMINATED);
    }
    
    private void testHash(int expected) {
	Random r = new Random();
	TPCMaster master = new TPCMaster(expected, null);
	LinkedList<TPCSlaveInfo> slaves = new LinkedList<TPCSlaveInfo>();
	for (int i=0; i<expected; i++) {
	    TPCSlaveInfo slave = new TPCSlaveInfo(r.nextLong());
	    slaves.add(slave);
	    master.registerSlave(slave);
	}
	Collections.sort(slaves, new Comparator<TPCSlaveInfo>() {
	    public int compare(TPCSlaveInfo s1, TPCSlaveInfo s2) {
		long id1 = s1.getSlaveID(), id2 = s2.getSlaveID();
		if (id1 == id2) {
		    return 0;
		}
		return TPCMaster.isLessThanUnsigned(id1, id2) ? -1 : 1;
	    }
        });
	int numKeys = 50;
	byte[] bytes = new byte[100];
	String[] keys = new String[numKeys];
	TPCSlaveInfo[] firsts = new TPCSlaveInfo[numKeys],
		      seconds = new TPCSlaveInfo[numKeys];
	for (int i=0; i<numKeys; i++) {
	    r.nextBytes(bytes);
	    keys[i] = new String(bytes);
	    firsts[i] = master.findFirstReplica(keys[i]);
	    seconds[i] = master.findSuccessor(firsts[i]);
	    TPCSlaveInfo trueFirst = getTrueSlave(slaves, keys[i], false);
	    TPCSlaveInfo trueSecond = getTrueSlave(slaves, keys[i], true);
	    assertSame("incorrect first server", trueFirst, firsts[i]);
	    assertSame("incorrect second server", trueSecond, seconds[i]);
	}
	for (int i=0; i<10*numKeys; i++) {
	    int index = i % numKeys;
	    String key = keys[index];
	    TPCSlaveInfo first = master.findFirstReplica(key);
	    TPCSlaveInfo second = master.findSuccessor(first);
	    assertSame("1st replica not consistent", firsts[index], first);
	    assertSame("2nd replica not consistent", seconds[index], second);
	    int indexOfFirst = slaves.indexOf(first);
	    int indexOfSecond = slaves.indexOf(second);
	    assertFalse(indexOfFirst == -1 || indexOfSecond == -1);
	    if (indexOfFirst == slaves.size()-1) {
		assertEquals("not correct successor", 0, indexOfSecond);
	    } else {
		assertEquals("not correct successor", indexOfFirst+1, indexOfSecond);
	    }
	}
    }
    
    private void assertThreadState(Thread[] threads, Thread.State state) {
	for (int i=0; i<threads.length; i++) {
	    assertEquals("thread in wrong state", state, threads[i].getState());
	}
    }
    
    private TPCSlaveInfo getTrueSlave(LinkedList<TPCSlaveInfo> slaves, String key, boolean second) {
	long hashCode = TPCMaster.hashTo64bit(key);
	boolean next = false;
	for (TPCSlaveInfo slave : slaves) {
	    if (next) return slave;
	    if (TPCMaster.isLessThanEqualUnsigned(hashCode, slave.getSlaveID())) {
		if (second) {
		    next = true;
		    continue;
		} else {
		    return slave;
		}
	    }
	}
	return next ? slaves.get(0) : (second ? slaves.get(1) : slaves.get(0));
    }
    
    class TestRunnable implements Runnable {
	TPCMaster master;
	public TestRunnable(TPCMaster master) {
	    this.master = master;
	}
	@Override
	public void run() {
	    try {
		synchronized (master) {
		    master.wait();
		}
	    } catch (InterruptedException e) {
	    }
	}
    }
}
