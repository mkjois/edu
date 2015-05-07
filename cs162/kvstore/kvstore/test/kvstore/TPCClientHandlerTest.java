package kvstore;

import static kvstore.KVConstants.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class TPCClientHandlerTest {
    static int numExceptions = 0;
    static int numReturns = 0;

    @Test
    public void test() throws InterruptedException {
	
	new Thread() {
            @Override
            public void run() {
        	try {
        	    TPCMaster master = mock(TPCMaster.class);

        	    doNothing().when(master).registerSlave((TPCSlaveInfo) anyObject());
        	    doAnswer(new Answer() {
        		public Object answer(InvocationOnMock invocation) throws KVException {
        		    Random rand = new Random();
        		    if (rand.nextBoolean()) {
        			//System.out.println("put/del exception");
        			numExceptions++;
        			throw new KVException(ERROR_INVALID_FORMAT);
        		    } else {
        			//System.out.println("put/del return");
        			numReturns++;
        			return null;
        		    }
        		}
        	    }).when(master).handleTPCRequest((KVMessage) anyObject(), anyBoolean());
        	    doAnswer(new Answer() {
        		public Object answer(InvocationOnMock invocation) throws KVException {
        		    Random rand = new Random();
        		    if (rand.nextBoolean()) {
        			numExceptions++;
        			throw new KVException(ERROR_INVALID_FORMAT);
        		    } else {
        			numReturns++;
        			return "v2";
        		    }
        		}
        	    }).when(master).handleGet((KVMessage) anyObject());

        	    SampleMaster.tpcMaster = master;
        	    try {
        		SampleMaster.main(null);
        	    } catch (IOException | InterruptedException e) {
        		fail("master io/interrupted exception");
        	    }
        	} catch (KVException e) {
        	    fail("master thread kv exception");
        	}
            }
        }.start();
        Thread.sleep(500);
        
        new Thread() {
            @Override
            public void run() {
        	try {
		    SampleSlave.main(new String[] {"$"});
		} catch (KVException e) {
		    e.printStackTrace();;
		    fail("slave kv exception");
		} catch (IOException e) {
		    System.out.println(e.getMessage());
		    fail("slave io exception");
		}
            }
        }.start();
        Thread.sleep(500);
        
        int numEx = 0, numRet = 0;
	KVClient client = null;
	try {
	    client = new KVClient(InetAddress.getLocalHost().getHostAddress(), 8080);
	} catch (UnknownHostException e) {
	    fail("unknown local host?");
	}
	String value;
	for (int i=0; i<10; i++) {
	    try {
		client.put("k1", "v1");
		numRet++;
	    } catch (KVException e) {
		numEx++;
	    }
	    try {
		value = client.get("k2");
		numRet++;
	    } catch (KVException e) {
		numEx++;
	    }
	    try {
		client.del("k3");
		numRet++;
	    } catch (KVException e) {
		numEx++;
	    }
	}
	System.out.println("numEx: " + numEx + ", numExceptions: " + numExceptions);
	System.out.println("numRet: " + numRet + ", numReturns: " + numReturns);
	assertEquals("exceptions don't match", numEx, numExceptions);
	assertEquals("returns don't match", numRet, numReturns);
    }
}
