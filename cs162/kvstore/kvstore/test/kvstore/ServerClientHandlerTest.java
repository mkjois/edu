package kvstore;

import static org.junit.Assert.*;
import static kvstore.KVConstants.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Relies on functional KVClient and KVMessage.
 * @author mj
 */
public class ServerClientHandlerTest {
    static String OSKEY = "z", LKEY = "", OSVALUE = "q", LVALUE = "";
    static int CLIENTS = 3;
    static KVServer kvserver;
    static ServerRunner runner;
    static KVClient[] clients;
    
    @Test
    public void nullTest() {
    }
    
    //@BeforeClass
    public static void initKVServer() throws KVException {
	for (int i=0; i<256; i++) {
	    OSKEY += "z";
	    LKEY += "z";
	}
	for (int i=0; i<256*1024; i++) {
	    OSVALUE += "q";
	    LVALUE += "q";
	}
	System.out.println("initialized keys");
	kvserver = new KVServer(4,4);
	System.out.println("initialized KVServer");
    }
    
    //@Before
    public void initServerAndClients() throws UnknownHostException, InterruptedException {
	String hostname = InetAddress.getLocalHost().getHostAddress();
	SocketServer server = new SocketServer(hostname, 8002);
	server.addHandler(new ServerClientHandler(kvserver, CLIENTS));
	runner = new ServerRunner(server, "ServerClientHandlerTest");
	runner.start();
	clients = new KVClient[CLIENTS];
	for (int i=0; i<CLIENTS; i++) {
	    clients[i] = new KVClient(hostname, 8002);
	}
    }

    //@Test
    public void test() throws InterruptedException {
	int counter = 0;
	String result;
	for (KVClient client : clients) {
	    counter++;
	    System.out.println("NEW CLIENT: #" + counter);
	    System.out.println("round of gets");
	    try {
		result = client.get(null);
		fail("getting a null key succeeded");
	    } catch (KVException e) {
		assertEquals(ERROR_INVALID_KEY, e.getKVMessage().getMessage());
	    }
	    try {
		result = client.get("");
		fail("getting an empty key succeeded");
	    } catch (KVException e) {
		assertEquals(ERROR_INVALID_KEY, e.getKVMessage().getMessage());
	    }
	    try {
		result = client.get(OSKEY);
		fail("getting an oversized key succeeded");
	    } catch (KVException e) {
		assertEquals(ERROR_NO_SUCH_KEY, e.getKVMessage().getMessage());
	    }
	    try {
		result = client.get(LKEY);
		fail("getting a nonexistent key succeeded");
	    } catch (KVException e) {
		assertEquals(ERROR_NO_SUCH_KEY, e.getKVMessage().getMessage());
	    }
	    System.out.println("round of puts");
	    try {
		client.put(null, null);
		fail("putting null key succeeded");
	    } catch (KVException e) {
		assertEquals(ERROR_INVALID_KEY, e.getKVMessage().getMessage());
	    }
	    try {
		client.put("word", null);
		fail("putting null value succeeded");
	    } catch (KVException e) {
		assertEquals(ERROR_INVALID_VALUE, e.getKVMessage().getMessage());
	    }
	    try {
		client.put("", "word");
		fail("putting empty key succeeded");
	    } catch (KVException e) {
		assertEquals(ERROR_INVALID_KEY, e.getKVMessage().getMessage());
	    }
	    try {
		client.put("word", "");
		fail("putting empty value succeeded");
	    } catch (KVException e) {
		assertEquals(ERROR_INVALID_VALUE, e.getKVMessage().getMessage());
	    }
	    try {
		client.put(OSKEY, OSVALUE);
		fail("putting oversized key succeeded");
	    } catch (KVException e) {
		assertEquals(ERROR_OVERSIZED_KEY, e.getKVMessage().getMessage());
	    }
	    try {
		client.put(LKEY, OSVALUE);
		fail("putting oversized value succeeded");
	    } catch (KVException e) {
		assertEquals(ERROR_OVERSIZED_VALUE, e.getKVMessage().getMessage());
	    }
	    try {
		client.put(LKEY, LVALUE);
	    } catch (KVException e) {
		System.out.println(e.getKVMessage());
		fail("putting good key/value pair failed");
	    }
	    System.out.println("intermediate round");
	    try {
		result = client.get(LKEY);
		assertEquals(LVALUE, result);
	    } catch (KVException e) {
		fail("getting good key failed");
	    }
	    try {
		client.put(LKEY, LKEY);
	    } catch (KVException e) {
		fail("getting good key failed");
	    }
	    try {
		result = client.get(LKEY);
		assertEquals(LKEY, result);
	    } catch (KVException e) {
		fail("getting good key failed");
	    }
	    System.out.println("round of dels");
	    try {
		client.del(null);
		fail("deleting null key succeeded");
	    } catch (KVException e) {
		assertEquals(ERROR_INVALID_KEY, e.getKVMessage().getMessage());
	    }
	    try {
		client.del("");
		fail("deleting empty key succeeded");
	    } catch (KVException e) {
		assertEquals(ERROR_INVALID_KEY, e.getKVMessage().getMessage());
	    }
	    try {
		client.del(OSKEY);
		fail("deleting oversized key succeeded");
	    } catch (KVException e) {
		assertEquals(ERROR_NO_SUCH_KEY, e.getKVMessage().getMessage());
	    }
	    try {
		client.del(LKEY);
	    } catch (KVException e) {
		fail("deleting good key failed");
	    }
	    try {
		client.del(LKEY);
		fail("deleting non-existent key succeeded");
	    } catch (KVException e) {
		assertEquals(ERROR_NO_SUCH_KEY, e.getKVMessage().getMessage());
	    }
	    try {
		result = client.get(LKEY);
		fail("getting non-existent key succeeded");
	    } catch (KVException e) {
		assertEquals(ERROR_NO_SUCH_KEY, e.getKVMessage().getMessage());
	    }
	    System.out.println("END CLIENT: #" + counter);
	}
    }
    
    //@Test
    public void testConcurrency() {
	System.out.println("printed");
    }
    
    //@After
    public void tearDown() {
	try {
	    runner.stop();
	} catch (InterruptedException e) {
	    System.out.println("server runner interrupted during call to stop()");
	}
	// TODO: check kvserver stuff here
    }
}
