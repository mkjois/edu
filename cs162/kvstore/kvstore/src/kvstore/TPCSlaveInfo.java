package kvstore;

import static kvstore.KVConstants.*;

import java.io.IOException;
import java.net.*;
import java.util.regex.*;

/**
 * Data structure to maintain information about SlaveServers
 */
public class TPCSlaveInfo {

    private long slaveID;
    private String hostname;
    private int port;

    /**
     * Construct a TPCSlaveInfo to represent a slave server.
     *
     * @param info as "SlaveServerID@Hostname:Port"
     * @throws KVException ERROR_INVALID_FORMAT if info string is invalid
     */
    public TPCSlaveInfo(String info) throws KVException {
	try {
	    slaveID = Long.parseLong(info.substring(0, info.indexOf('@')));
	    hostname = info.substring(info.indexOf('@')+1, info.indexOf(':'));
	    port = Integer.parseInt(info.substring(info.indexOf(':')+1, info.length()));
	} catch (Exception e) {
	    throw new KVException(ERROR_INVALID_FORMAT);
    	}
    }
    
    /**
     * To construct a dummy key slave for consistent hashing purposes.
     * Also used for unit-testing.
     * @param id is a string's 64-bit hash code
     */
    public TPCSlaveInfo(long id) {
	this.slaveID = id;
    }

    public long getSlaveID() {
        return slaveID;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    /**
     * Create and connect a socket within a certain timeout.
     *
     * @return Socket object connected to SlaveServer, with timeout set
     * @throws KVException ERROR_SOCKET_TIMEOUT, ERROR_COULD_NOT_CREATE_SOCKET,
     *         or ERROR_COULD_NOT_CONNECT
     */
    public Socket connectHost(int timeout) throws KVException {
	try {
	    Socket sock = new Socket(hostname, port);
	    sock.setSoTimeout(timeout);
	    return sock;
	} catch (Exception e) {
	    throw new KVException(ERROR_COULD_NOT_CONNECT);
	}
    }

    /**
     * Closes a socket.
     * Best effort, ignores error since the response has already been received.
     *
     * @param sock Socket to be closed
     */
    public void closeHost(Socket sock) {
	try {
	    sock.close();
	} catch (IOException e) {
	    // ignore
	}
    }
}
