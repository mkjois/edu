package kvstore;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketServerTest {
    static String HOST;
    static int PORT = 8000;
    static int CLIENTS = 10;
    static int CONNECTIONS = 0;
    static SocketServer server;
    static NetworkHandler handler;
    static Socket[] clients;
    
    @Test
    public void nullTest() {
    }
    
    //@BeforeClass
    public static void initServerSocket() {
	try {
	    HOST = InetAddress.getLocalHost().getHostAddress();
	    server = new SocketServer(HOST, PORT);
	    server.connect();
	} catch (IOException e) {
	    fail("could not bind server socket");
	}
    }
    
    //@Before
    public void initClientSockets() {
	try {
	    clients = new Socket[CLIENTS];
	    for (int i=0; i<CLIENTS; i++) {
		clients[i] = new Socket(HOST, PORT);
	    }
	} catch (UnknownHostException e) {
	    fail("could not resolve localhost");
	} catch (IOException e) {
	    fail("could not create client socket");
	}
	CONNECTIONS = 0;
    }
    
    //@Before
    public void initMockHandler() {
	handler = mock(NetworkHandler.class);
	doAnswer(new Answer() {
	    public Object answer(InvocationOnMock invocation) {
		Object[] args = invocation.getArguments();
		if (args[0] instanceof Socket) {
		    Socket socket = (Socket) args[0];
		    boolean sameHost = socket.getLocalAddress().getHostAddress().equals(HOST);
		    boolean samePort = socket.getLocalPort() == PORT;
		    CONNECTIONS += sameHost && samePort ? 1 : 0;
		}
		return null;
	    }
	}).when(handler).handle(anyClient());
	server.addHandler(handler);
    }

    //@Test
    public void testArgs() {
	testConnectFail(HOST, PORT); // can't connect again
	testConnectFail(HOST, -1); // invalid port
	testConnectFail(HOST, 1); // not ephemeral port
	testConnectFail(HOST, 1023); // not ephemeral port
	testConnectFail(HOST, 65536); // invalid port
	for (int port=1024; port<PORT; port += 200) {
	    testConnectPass("", port);
	}
	for (int port=PORT+1; port<65536; port += 2000) {
	    testConnectPass("", port);
	}
	testConnectPass("", 0);
    }
    
    //@Test
    public void testStopBeforeStart() {
	server.stop();
	try {
	    server.start();
	} catch (IOException e) {
	    fail("server couldn't close (testing stop before start)");
	}
	try {
	    server = new SocketServer(HOST, PORT);
	    server.connect();
	} catch (IOException e) {
	    fail("server couldn't reconnect");
	}
	assertEquals("phantom connections have been made", 0, CONNECTIONS);
	verify(handler, never()).handle(anyClient());
    }
    
    //@Test
    public void testHandler() {
	try {
	    new Thread(new Runnable() {
		public void run() {
		    try {
			server.start();
		    } catch (IOException e) {
			fail("couldn't establish connections with cliens");
		    }
		}
	    }).start();
	    Thread.sleep(500);
	} catch (InterruptedException e) {
	    System.out.println("main thread interrupted after starting");
	}
	server.stop();
	try {
	    Thread.sleep(500);
	    server.connect();
	} catch (IOException e) {
	    fail("server couldn't reconnect");
	} catch (InterruptedException e) {
	    System.out.println("main thread interrupted after stopping");
	}
	assertEquals("not the right number of connections", CLIENTS, CONNECTIONS);
	verify(handler, times(CLIENTS)).handle(anyClient());
    }
    
    //@After
    public void closeClientSockets() {
	try {
	    for (Socket s : clients) {
		s.close();
	    }
	} catch (IOException e) {
	    // ignore
	}
    }
    
    private void testConnectFail(String hostname, int port) {
	try {
	    SocketServer ss = new SocketServer(hostname, port);
	    ss.connect();
	    fail("invalid SocketServer succeeded with connect()");
	} catch (IOException e) {
	}
    }
    
    private void testConnectPass(String hostname, int port) {
	try {
	    SocketServer ss = new SocketServer(hostname, port);
	    ss.connect();
	    int assignedPort = ss.getPort();
	    if (port == 0) {
		assertTrue("incorrectly allocated dynamic port: "+assignedPort,
			   1023<assignedPort && assignedPort<65536);
	    } else {
		assertTrue("incorrectly assigned ephemeral port: "+assignedPort
			   +" (given: "+port+")", assignedPort == port);
	    }
	} catch (IOException e) {
	    fail("valid SocketServer failed with connect()");
	}
    }
    
    private Socket anyClient() {
	return argThat(new IsClientSocket());
    }
    
    private class IsClientSocket extends ArgumentMatcher<Socket> {
	public boolean matches(Object socket) {
	    if (socket instanceof Socket) {
		return true;
	    } else {
		return false;
	    }
	}
    }
}
