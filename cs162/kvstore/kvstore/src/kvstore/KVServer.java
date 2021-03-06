package kvstore;

import static kvstore.KVConstants.*;

import java.io.*;
import java.util.concurrent.locks.Lock;

import nu.xom.*;

/**
 * This class services all storage logic for an individual key-value server.
 * All KVServer request on keys from different sets must be parallel while
 * requests on keys from the same set should be serial. A write-through
 * policy should be followed when a put request is made.
 */
public class KVServer implements KeyValueInterface {

    private KVStore dataStore;
    private KVCache dataCache;

    private static final int MAX_KEY_SIZE = 256;
    private static final int MAX_VAL_SIZE = 256 * 1024;

    /**
     * Constructs a KVServer backed by a KVCache and KVStore.
     *
     * @param numSets the number of sets in the data cache
     * @param maxElemsPerSet the size of each set in the data cache
     */

    public KVServer(int numSets, int maxElemsPerSet) {
        this.dataCache = new KVCache(numSets, maxElemsPerSet);
        this.dataStore = new KVStore();
    }

    /**
     * Performs put request on cache and store.
     *
     * @param  key String key
     * @param  value String value
     * @throws KVException if key or value is too long
     */
    @Override
    public void put(String key, String value) throws KVException {
        final int MAX_KEY_SIZE = 256;
        final int MAX_VALUE_SIZE = 256 * 1024;
        if (key.length() > MAX_KEY_SIZE) {
            throw new KVException(KVConstants.ERROR_OVERSIZED_KEY);
        }
        if (value.length() > MAX_VALUE_SIZE) {
            throw new KVException(KVConstants.ERROR_OVERSIZED_VALUE);
        }
        Lock lock = dataCache.getLock(key);
        lock.lock();
        dataStore.put(key, value);
        dataCache.put(key, value);
        lock.unlock();
    }

    /**
     * Performs get request.
     * Checks cache first. Updates cache if not in cache but found in store.
     *
     * @param  key String key
     * @return String value associated with key
     * @throws KVException with ERROR_NO_SUCH_KEY if key does not exist in store
     */
    @Override
    public String get(String key) throws KVException {
        if (key == null || key.equals("")) {
            throw new KVException(KVConstants.ERROR_INVALID_KEY);
        }
        Lock lock = dataCache.getLock(key);
        lock.lock();
        String result = dataCache.get(key);
        try {
            if (result == null) {
                result = dataStore.get(key);
                dataCache.put(key, result);
            }
        } finally {
            lock.unlock();
        }
        return result;
    }

    /**
     * Performs del request.
     *
     * @param  key String key
     * @throws KVException with ERROR_NO_SUCH_KEY if key does not exist in store
     */
    @Override
    public void del(String key) throws KVException {
        if (key == null || key.equals("")) {
            throw new KVException(KVConstants.ERROR_INVALID_KEY);
        }
        Lock lock = dataCache.getLock(key);
        lock.lock();
        try {
            dataCache.del(key);
            dataStore.del(key);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Check if the server has a given key. This is used for TPC operations
     * that need to check whether or not a transaction can be performed but
     * you don't want to modify the state of the cache by calling get(). You
     * are allowed to call dataStore.get() for this method.
     *
     * @param key key to check for membership in store
     */
    public boolean hasKey(String key) {
        Lock lock = dataCache.getLock(key);
        lock.lock();
        String result = dataCache.get(key);
        try {
            if (result == null) {
                result = dataStore.get(key);
            }
        } catch (KVException e) {
            result = null;
        } finally {
            lock.unlock();
        }
        return (result != null);
    }

    /** This method is purely for convenience and will not be tested. */
    @Override
    public String toString() {
        return dataStore.toString() + dataCache.toString();
    }

}
