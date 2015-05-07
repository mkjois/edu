package kvstore;

import static kvstore.KVConstants.*;

import java.net.Socket;
import java.net.SocketException;

/**
 * This NetworkHandler will asynchronously handle the socket connections.
 * Uses a thread pool to ensure that none of its methods are blocking.
 */
public class ServerClientHandler implements NetworkHandler {

    private KVServer kvServer;
    private ThreadPool threadPool;

    /**
     * Constructs a ServerClientHandler with ThreadPool with a single thread.
     *
     * @param kvServer KVServer to carry out requests
     */
    public ServerClientHandler(KVServer kvServer) {
        this(kvServer, 1);
    }

    /**
     * Constructs a ServerClientHandler with ThreadPool of thread equal to
     * the number given as connections.
     *
     * @param kvServer KVServer to carry out requests
     * @param connections number of threads in threadPool to service requests
     */
    public ServerClientHandler(KVServer kvServer, int connections) {
        this.kvServer = kvServer;
        this.threadPool = new ThreadPool(connections);
    }

    /**
     * Creates a job to service the request for a socket and enqueues that job
     * in the thread pool. Ignore all InterruptedExceptions.
     *
     * @param client Socket connected to the client with the request
     */
    @Override
    public void handle(Socket client) {
        try {
            ClientHandler job = new ClientHandler(client);
            job.kvServer = this.kvServer;
            this.threadPool.addJob(job);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    /**
     * Runnable class with routine to service a request from the client.
     */
    private class ClientHandler implements Runnable {

        private Socket client;
        private KVServer kvServer;

        /**
         * Construct a ClientHandler.
         *
         * @param client Socket connected to client with the request
         */
        public ClientHandler(Socket client) {
            this.client = client;
        }

        /**
         * Processes request from client and sends back a response with the
         * result. The delivery of the response is best-effort. If we are
         * unable to return a response, there is nothing else we can do.
         */
        @Override
        public void run() {
            KVMessage response;
            try {
        	KVMessage request = new KVMessage(this.client, this.client.getSoTimeout());
        	String key = request.getKey(),
        	       val = request.getValue(),
        	       msg = request.getMessage();
        	switch (request.getMsgType()) {
        	case KVConstants.GET_REQ:
        	    response = this.handleGet(key, val, msg); break;
        	case KVConstants.PUT_REQ:
        	    response = this.handlePut(key, val, msg); break;
        	case KVConstants.DEL_REQ:
        	    response = this.handleDel(key, val, msg); break;
        	default:
        	    response = new KVMessage(KVConstants.RESP, KVConstants.ERROR_INVALID_FORMAT);
        	}
            } catch (KVException e) {
        	response = e.getKVMessage();
        	//System.out.println(response);
            } catch (SocketException e) {
        	System.out.print("socket exception");
        	response = new KVMessage(KVConstants.RESP, KVConstants.ERROR_COULD_NOT_RECEIVE_DATA);
            }
            try {
        	response.sendMessage(this.client);
            } catch (KVException e) {
        	// ignore; sending is best-effort
            }
        }
        
        private KVMessage handleGet(String key, String value, String message)
        	throws KVException {
            KVMessage response = new KVMessage(KVConstants.RESP);
            //System.out.println(this.kvServer);
            value = this.kvServer.get(key);
            response.setKey(key);
            response.setValue(value);
            return response;
        }
        
        private KVMessage handlePut(String key, String value, String message)
        	throws KVException {
            KVMessage response = new KVMessage(KVConstants.RESP);
            this.kvServer.put(key, value);
            response.setMessage(KVConstants.SUCCESS);
            return response;
        }
        
        private KVMessage handleDel(String key, String value, String message)
        	throws KVException {
            KVMessage response = new KVMessage(KVConstants.RESP);
            this.kvServer.del(key);
            response.setMessage(KVConstants.SUCCESS);
            return response;
        }
    }
}
