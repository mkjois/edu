package kvstore;

import static kvstore.KVConstants.*;

import java.net.Socket;
/**
 * Implements NetworkHandler to handle 2PC operation requests from the Master/
 * Coordinator Server
 */
public class TPCMasterHandler implements NetworkHandler {

    private long slaveID;
    private KVServer kvServer;
    private TPCLog tpcLog;
    private ThreadPool threadpool;

    public static final int MAX_KEY = 256;
    public static final int MAX_VAL = 256 * 1024;

    /**
     * Constructs a TPCMasterHandler with one connection in its ThreadPool
     *
     * @param slaveID the ID for this slave server
     * @param kvServer KVServer for this slave
     * @param log the log for this slave
     */
    public TPCMasterHandler(long slaveID, KVServer kvServer, TPCLog log) {
        this(slaveID, kvServer, log, 1);
    }

    /**
     * Constructs a TPCMasterHandler with a variable number of connections
     * in its ThreadPool
     *
     * @param slaveID the ID for this slave server
     * @param kvServer KVServer for this slave
     * @param log the log for this slave
     * @param connections the number of connections in this slave's ThreadPool
     */
    public TPCMasterHandler(long slaveID, KVServer kvServer, TPCLog log, int connections) {
        this.slaveID = slaveID;
        this.kvServer = kvServer;
        this.tpcLog = log;
        this.threadpool = new ThreadPool(connections);
    }

    /**
     * Registers this slave server with the master.
     *
     * @param masterHostname
     * @param server SocketServer used by this slave server (which contains the
     *               hostname and port this slave is listening for requests on
     * @throws KVException with ERROR_INVALID_FORMAT if the response from the
     *         master is received and parsed but does not correspond to a
     *         success as defined in the spec OR any other KVException such
     *         as those expected in KVClient in project 3 if unable to receive
     *         and/or parse message
     */
    public void registerWithMaster(String masterHostname, SocketServer server)
            throws KVException {
        Socket masterSock;;
        try {
            masterSock = new Socket(masterHostname, 9090);
        } catch (Exception e) {
            throw new KVException(ERROR_COULD_NOT_CONNECT);
        }
        String slaveInfo = slaveID + "@" + server.getHostname() + ":" + server.getPort();
        //System.out.println("register: " + slaveInfo);
        KVMessage registerRequest = new KVMessage(REGISTER, slaveInfo);
        registerRequest.sendMessage(masterSock);
        KVMessage registerResponse = new KVMessage(masterSock);
        //System.out.println(registerResponse.getMsgType() + ": " + registerResponse.getMessage());
        if (!registerResponse.getMessage().equals("Successfully registered " + slaveInfo)) {
            throw new KVException(ERROR_INVALID_FORMAT);
        }
    }

    /**
     * Creates a job to service the request on a socket and enqueues that job
     * in the thread pool. Ignore any InterruptedExceptions.
     *
     * @param master Socket connected to the master with the request
     */
    @Override
    public void handle(Socket master) {
        MasterHandler handler = new MasterHandler(master, kvServer, tpcLog);
        //handler.run();
        try {
            threadpool.addJob(handler);
        } catch (InterruptedException e) { }
    }

    /**
     * Runnable class containing routine to service a message from the master.
     */
    private class MasterHandler implements Runnable {

        private Socket master;
        private KVServer kvServer;
        private TPCLog tpcLog;

        /**
         * Construct a MasterHandler.
         *
         * @param master Socket connected to master with the message
         */
        public MasterHandler(Socket master, KVServer server, TPCLog log) {
            this.master = master;
            this.kvServer = server;
            this.tpcLog = log;
        }

        /**
         * Processes request from master and sends back a response with the
         * result. This method needs to handle both phase1 and phase2 messages
         * from the master. The delivery of the response is best-effort. If
         * we are unable to return any response, there is nothing else we can do.
         */
        @Override
        public void run() {
            KVMessage m;
            try {
                m = new KVMessage(master);
            } catch (KVException e) {
                return;
            }
            KVMessage r;
            if (KVConstants.GET_REQ.equals(m.getMsgType())) {
                tpcLog.appendAndFlush(m);
                String val;
                try {
                    val = kvServer.get(m.getKey());
                    r = new KVMessage(KVConstants.RESP);
                    r.setKey(m.getKey());
                    r.setValue(val);
                } catch (KVException e) {
                    r = e.getKVMessage();
                }
                try {
                    r.sendMessage(master);
                } catch (KVException e) { }
                return;
            } else if (KVConstants.PUT_REQ.equals(m.getMsgType())) {
                tpcLog.appendAndFlush(m);
                if (m.getKey().length() > MAX_KEY) {
                    r = new KVMessage(KVConstants.ABORT,
                                      KVConstants.ERROR_OVERSIZED_KEY);
                } else if (m.getValue().length() > MAX_VAL) {
                    r = new KVMessage(KVConstants.ABORT,
                                      KVConstants.ERROR_OVERSIZED_VALUE);
                } else {
                    r = new KVMessage(KVConstants.READY);
                }
                try {
                    r.sendMessage(master);
                } catch (KVException e) { }
                return;
            } else if (KVConstants.DEL_REQ.equals(m.getMsgType())) {
                tpcLog.appendAndFlush(m);
                if (!kvServer.hasKey(m.getKey())) {
                    r = new KVMessage(KVConstants.ABORT,
                                      KVConstants.ERROR_NO_SUCH_KEY);
                } else {
                    r = new KVMessage(KVConstants.READY);
                }
                try {
                    r.sendMessage(master);
                } catch (KVException e) { }
                return;
            } else if (KVConstants.COMMIT.equals(m.getMsgType())) {
                KVMessage last = tpcLog.getLastEntry();
                tpcLog.appendAndFlush(m);
                if (KVConstants.PUT_REQ.equals(last.getMsgType())) {
                    try {
                        kvServer.put(last.getKey(), last.getValue());
                    } catch (KVException e) { }
                } else if (KVConstants.DEL_REQ.equals(last.getMsgType())) {
                    try {
                        kvServer.del(last.getKey());
                    } catch (KVException e) {
                        // this should never happen.
                    }
                }
                r = new KVMessage(KVConstants.ACK);
                try {
                    r.sendMessage(master);
                } catch (KVException e) { }
                return;
            } else if (KVConstants.ABORT.equals(m.getMsgType())) {
                tpcLog.appendAndFlush(m);
                r = new KVMessage(KVConstants.ACK);
                try {
                    r.sendMessage(master);
                } catch (KVException e) { }
                return;
            }
        }
    }
}
