package kvstore;

import static kvstore.KVConstants.*;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Client API used to send requests to key-value server.
 */
public class KVClient implements KeyValueInterface {

    private String server;
    private int port;

    /**
     * Constructs a KVClient connected to a server.
     *
     * @param server is the DNS reference to the server
     * @param port is the port to which the server is listening
     */
    public KVClient(String server, int port) {
        this.server = server;
        this.port = port;
    }

    /**
     * Creates a socket connected to the server to make a request.
     *
     * @return Socket connected to server
     * @throws KVException if unable to make or connect socket
     * @throws IOException 
     * @throws UnknownHostException 
     */
    private Socket connectHost() throws KVException {
    	try {
    	    Socket mySock = new Socket(server, port);
    	    return mySock;
    	} catch (Exception e) {
    	    throw new KVException(ERROR_COULD_NOT_CREATE_SOCKET);
    	}
    }

    /**
     * Closes a socket.
     * Best effort, ignores error since the response has already been received.
     *
     * @param  sock Socket to be closed
     */
    private void closeHost(Socket sock) {
	try {
	    sock.close();
	} catch (Exception e) {
	    // ignore
	}
    }

    /**
     * Issues a PUT request to the server.
     *
     * @param  key String to put in server as key
     * @throws KVException if the request was not successful in any way
     */
    @Override
    public void put(String key, String value) throws KVException {
	if (key == null || key.isEmpty()) {
	    throw new KVException(ERROR_INVALID_KEY);
        }
	if (value == null || value.isEmpty()) {
	    throw new KVException(ERROR_INVALID_VALUE);
        }
    	KVMessage message = new KVMessage(PUT_REQ);
    	Socket newSock = connectHost();
    	message.setKey(key);
    	message.setValue(value);
    	message.sendMessage(newSock);
    	KVMessage returnMessage = new KVMessage(newSock);
    	closeHost(newSock);
    	String retVal = returnMessage.getMessage();
    	if (!SUCCESS.equals(retVal)) {
    	    throw new KVException(returnMessage);
    	}
    }

    /**
     * Issues a GET request to the server.
     *
     * @param  key String to get value for in server
     * @return String value associated with key
     * @throws KVException if the request was not successful in any way
     */
    @Override
    public String get(String key) throws KVException {
	if (key == null || key.isEmpty()) {
	    throw new KVException(ERROR_INVALID_KEY);
        }
    	KVMessage message = new KVMessage(GET_REQ);
    	Socket newSock = connectHost();
    	message.setKey(key);
    	message.sendMessage(newSock);
    	KVMessage returnMessage = new KVMessage(newSock);
    	closeHost(newSock);
    	String retVal = returnMessage.getValue();
    	if (retVal == null) {
    	    throw new KVException(returnMessage);
    	}
    	return retVal;
    }

    /**
     * Issues a DEL request to the server.
     *
     * @param  key String to delete value for in server
     * @throws KVException if the request was not successful in any way
     */
    @Override
    public void del(String key) throws KVException {
	if (key == null || key.isEmpty()) {
	    throw new KVException(ERROR_INVALID_KEY);
        }
    	KVMessage message = new KVMessage(DEL_REQ);
    	Socket newSock = connectHost();
    	message.setKey(key);
    	message.sendMessage(newSock);
    	KVMessage returnMessage = new KVMessage(newSock);
    	closeHost(newSock);
    	String retVal = returnMessage.getMessage();
    	if (!SUCCESS.equals(retVal)) {
    	    throw new KVException(returnMessage);
    	}
    }


}
