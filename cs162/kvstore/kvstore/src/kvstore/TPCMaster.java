package kvstore;

import static kvstore.KVConstants.*;

import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.Lock;

public class TPCMaster {

    private int numSlaves;
    private HashSet<String> usedKeysReaders;
    private HashSet<String> usedKeysWriters;
    private KVCache masterCache;
    private TreeSet<TPCSlaveInfo> slavePool;

    public static final int TIMEOUT = 3000;

    /**
     * Creates TPCMaster, expecting numSlaves slave servers to eventually register
     *
     * @param numSlaves number of slave servers expected to register
     * @param cache KVCache to cache results on master
     */
    public TPCMaster(int numSlaves, KVCache cache) {
        this.numSlaves = numSlaves;
        this.usedKeysReaders = new HashSet<String>();
        this.usedKeysWriters = new HashSet<String>();
        this.masterCache = cache;
        this.slavePool = new TreeSet<TPCSlaveInfo>(new Comparator<TPCSlaveInfo>() {
            public int compare(TPCSlaveInfo s1, TPCSlaveInfo s2) {
                long id1 = s1.getSlaveID(), id2 = s2.getSlaveID();
                if (id1 == id2) {
                    return 0;
                }
                return isLessThanUnsigned(id1, id2) ? -1 : 1;
            }
        });
    }

    /**
     * Registers a slave. Drop registration request if numSlaves already
     * registered. Note that a slave re-registers under the same slaveID when
     * it comes back online.
     *
     * @param slave the slaveInfo to be registered
     */
    public synchronized void registerSlave(TPCSlaveInfo slave) {
        if (this.slavePool.size() >= this.numSlaves) return;
        this.slavePool.add(slave);
        if (this.slavePool.size() < this.numSlaves) return;
        this.notifyAll();
    }

    /**
     * Converts Strings to 64-bit longs. Borrowed from http://goo.gl/le1o0W,
     * adapted from String.hashCode().
     *
     * @param string String to hash to 64-bit
     * @return long hashcode
     */
    public static long hashTo64bit(String string) {
        long h = 1125899906842597L;
        int len = string.length();

        for (int i = 0; i < len; i++) {
            h = (31 * h) + string.charAt(i);
        }
        return h;
    }

    /**
     * Compares two longs as if they were unsigned (Java doesn't have unsigned
     * data types except for char). Borrowed from http://goo.gl/QyuI0V
     *
     * @param n1 First long
     * @param n2 Second long
     * @return is unsigned n1 less than unsigned n2
     */
    public static boolean isLessThanUnsigned(long n1, long n2) {
        return (n1 < n2) ^ ((n1 < 0) != (n2 < 0));
    }

    /**
     * Compares two longs as if they were unsigned, uses isLessThanUnsigned
     *
     * @param n1 First long
     * @param n2 Second long
     * @return is unsigned n1 less than or equal to unsigned n2
     */
    public static boolean isLessThanEqualUnsigned(long n1, long n2) {
        return isLessThanUnsigned(n1, n2) || (n1 == n2);
    }

    /**
     * Find primary replica for a given key. This method uses binary search,
     * so it's behavior is undefined if not all slaves have been registered
     * (i.e. the array of slaves is not sorted).
     *
     * @param key String to map to a slave server replica
     * @return SlaveInfo of first replica
     */
    public TPCSlaveInfo findFirstReplica(String key) {
        TPCSlaveInfo dummy = new TPCSlaveInfo(hashTo64bit(key));
        TPCSlaveInfo slave = this.slavePool.ceiling(dummy);
        return slave == null ? this.slavePool.first() : slave;
    }

    /**
     * Find the successor of firstReplica. Also undefined behavior if not all
     * slaves registered.
     *
     * @param firstReplica SlaveInfo of primary replica
     * @return SlaveInfo of successor replica
     */
    public TPCSlaveInfo findSuccessor(TPCSlaveInfo firstReplica) {
        TPCSlaveInfo slave = this.slavePool.higher(firstReplica);
        return slave == null ? this.slavePool.first() : slave;
    }

    /**
     * Perform 2PC operations from the master node perspective. This method
     * contains the bulk of the two-phase commit logic. It performs phase 1
     * and phase 2 with appropriate timeouts and retries.
     *
     * See the spec for details on the expected behavior.
     *
     * @param msg KVMessage corresponding to the transaction for this TPC request
     * @param isPutReq boolean to distinguish put and del requests
     * @throws KVException if the operation cannot be carried out for any reason
     */
    public synchronized void handleTPCRequest(KVMessage msg, boolean isPutReq)
            throws KVException {
        
        while (numSlaves < slavePool.size()) {
            try { wait(); } catch (InterruptedException e) {}
        }
        while (usedKeysReaders.contains(msg.getKey())) {
            try { wait(); } catch (InterruptedException e) {}
        }
        usedKeysWriters.add(msg.getKey());
        TPCSlaveInfo rep1 = findFirstReplica(msg.getKey());
        TPCSlaveInfo rep2 = findSuccessor(rep1);
        Socket sock1, sock2;

        KVMessage abortmsg = new KVMessage(ABORT);
        KVMessage commitmsg = new KVMessage(COMMIT);
        KVMessage reply1 = null;
        KVMessage reply2 = null;

        boolean retry = true;
        boolean abort = false;

        try {
            sock1 = rep1.connectHost(TIMEOUT);
            sock2 = rep2.connectHost(TIMEOUT);
        } catch (KVException e) {
            throw new KVException(ERROR_COULD_NOT_CONNECT);
        }

        try {
            msg.sendMessage(sock1);
        } catch (KVException e) {
            throw new KVException(ERROR_COULD_NOT_CONNECT);
        }

        try {
            msg.sendMessage(sock2);
        } catch (KVException e) {
            do {
                try {
                    abortmsg.sendMessage(sock1);
                    retry = false;
                } catch (KVException ea) {
                    retry = true;
                    try {
                        rep1.closeHost(sock1);
                        rep1 = findFirstReplica(msg.getKey());
                        sock1 = rep1.connectHost(TIMEOUT);
                    } catch (KVException eb) {}
                }
            } while (retry);
            do {
                try {
                    reply1 = new KVMessage(sock1, TIMEOUT);
                    retry = false;
                } catch (KVException ea) {
                    retry = true;
                    try {
                        rep1.closeHost(sock1);
                        rep1 = findFirstReplica(msg.getKey());
                        sock1 = rep1.connectHost(TIMEOUT);
                    } catch (KVException eb) {}
                }
                if (reply1.getMsgType().equals(ACK)) {
                    throw new KVException(ERROR_COULD_NOT_CONNECT);
                } else {
                    throw new KVException(ERROR_INVALID_FORMAT);
                }
            } while (retry);
        }
        
        try {
            reply1 = new KVMessage(sock1, TIMEOUT);
            reply2 = new KVMessage(sock2, TIMEOUT);
            if (!reply1.getMsgType().equals(READY) || !reply2.getMsgType().equals(READY)) {
                abort = true;
            }
        } catch (KVException e) {
            abort = true;
        }

        if (abort) {
            do {
                try {
                    abortmsg.sendMessage(sock1);
                    retry = false;
                } catch (KVException e) {
                    retry = true;
                    try {
                        rep1.closeHost(sock1);
                        rep1 = findFirstReplica(msg.getKey());
                        sock1 = rep1.connectHost(TIMEOUT);
                    } catch (KVException ea) {}
                }
            } while (retry);
            do {
                try {
                    abortmsg.sendMessage(sock2);
                    retry = false;
                } catch (KVException e) {
                    retry = true;
                    try {
                        rep2.closeHost(sock2);
                        rep2 = findFirstReplica(msg.getKey());
                        sock2 = rep2.connectHost(TIMEOUT);
                    } catch (KVException ea) {}
                }
            } while (retry);
            throw new KVException(ERROR_INVALID_FORMAT);
        } else {
            do {
                try {
                    commitmsg.sendMessage(sock1);
                    retry = false;
                } catch (KVException e) {
                    retry = true;
                    try {
                        rep1.closeHost(sock1);
                        rep1 = findFirstReplica(msg.getKey());
                        sock1 = rep1.connectHost(TIMEOUT);
                    } catch (KVException ea) {}
                }
            } while (retry);
            do {
                try {
                    commitmsg.sendMessage(sock2);
                    retry = false;
                } catch (KVException e) {
                    retry = true;
                    try {
                        rep2.closeHost(sock2);
                        rep2 = findFirstReplica(msg.getKey());
                        sock2 = rep2.connectHost(TIMEOUT);
                    } catch (KVException ea) {}
                }
            } while (retry);
        }

        try {
            reply1 = new KVMessage(sock1, TIMEOUT);
            retry = false;
        } catch (KVException e) {
            retry = true;
            try {
                rep1.closeHost(sock1);
                rep1 = findFirstReplica(msg.getKey());
                sock1 = rep1.connectHost(TIMEOUT);
            } catch (KVException ea) {}
        }

        try {
            reply2 = new KVMessage(sock2, TIMEOUT);
            retry = false;
        } catch (KVException e) {
            retry = true;
            try {
                rep2.closeHost(sock2);
                rep2 = findFirstReplica(msg.getKey());
                sock2 = rep1.connectHost(TIMEOUT);
            } catch (KVException ea) {}
        }

        if (!reply1.getMsgType().equals(ACK) || !reply2.getMsgType().equals(ACK)) {
            throw new KVException("BADNESS ERROR: WE GOT A REPLY THAT SHOULDNT EVER HAPPEN");
        }


        if (!abort){
            Lock l;
            String k = msg.getKey();
            if (isPutReq){
                l = masterCache.getLock(k);
                l.lock();
                masterCache.put(k, msg.getValue());
                l.unlock();
            } else {
                l = masterCache.getLock(k);
                l.lock();
                masterCache.del(k);
                l.unlock();
            }
        }


        usedKeysWriters.remove(msg.getKey());
        synchronized (this){
            notify();
        }
       
    }

    /**
     * Perform GET operation in the following manner:
     * - Try to GET from cache, return immediately if found
     * - Try to GET from first/primary replica
     * - If primary succeeded, return value
     * - If primary failed, try to GET from the other replica
     * - If secondary succeeded, return value
     * - If secondary failed, return KVExceptions from both replicas
     *
     * @param msg KVMessage containing key to get
     * @return value corresponding to the Key
     * @throws KVException with ERROR_NO_SUCH_KEY if unable to get
     *         the value from either slave for any reason
     */
    public String handleGet(KVMessage msg) throws KVException {
        while (numSlaves < slavePool.size()) {
            try { wait(); } catch (InterruptedException e) {}
        }
        while (usedKeysWriters.contains(msg.getKey())) {
            try { wait(); } catch (InterruptedException e) {}
        }

        Lock l;
        usedKeysReaders.add(msg.getKey());

        String value = masterCache.get(msg.getKey());
        if (value != null){
            usedKeysReaders.remove(msg.getKey());
            synchronized (this){
                notify();
            }
            return value;
        }

        TPCSlaveInfo rep1 = findFirstReplica(msg.getKey());
        Socket sock1 = null;
        KVMessage reply;
        boolean messageSent = true;

        try {
            sock1 = rep1.connectHost(TIMEOUT);
            msg.sendMessage(sock1);
        } catch (KVException e) {
            messageSent = false; // set flag and continue
        }

        if (messageSent) {
            try {
                reply = new KVMessage(sock1, TIMEOUT);
                if (reply.getMsgType().equals(RESP) && reply.getMessage() == null) {
                    usedKeysReaders.remove(msg.getKey());
                    synchronized (this){
                        notify();
                    }
                    l = masterCache.getLock(msg.getKey());
                    l.lock();
                    masterCache.put(msg.getKey(), reply.getValue());
                    l.unlock();
                    rep1.closeHost(sock1);
                    return reply.getValue();
                }
            } catch (KVException e) {} // ignore error and continue
        }

        TPCSlaveInfo rep2 = findSuccessor(rep1);
        Socket sock2 = null;

        try {
            sock2 = rep2.connectHost(TIMEOUT);
            msg.sendMessage(sock2);
        } catch (KVException e) {
            usedKeysReaders.remove(msg.getKey());
            synchronized (this){
                notify();
            }
            rep2.closeHost(sock2);
            throw new KVException(ERROR_NO_SUCH_KEY);
        }

        try {
            reply = new KVMessage(sock1, TIMEOUT);
            if (reply.getMsgType().equals(RESP) && reply.getMessage() == null) {
                usedKeysReaders.remove(msg.getKey());
                synchronized (this){
                    notify();
                }
                l = masterCache.getLock(msg.getKey());
                l.lock();
                masterCache.put(msg.getKey(), reply.getValue());
                l.unlock();
                rep2.closeHost(sock2);
                return reply.getValue();
            }
        } catch (KVException e) {
            usedKeysReaders.remove(msg.getKey());
            synchronized (this){
                notify();
            }
            rep2.closeHost(sock2);
            throw new KVException(ERROR_NO_SUCH_KEY);
        }
        return null;
    }
    
    /**
     * The following methods are for testing purposes only.
     */
    public boolean contains(TPCSlaveInfo slave) {
    return this.slavePool.contains(slave);
    }
    public int size() {
    return this.slavePool.size();
    }
    
}
