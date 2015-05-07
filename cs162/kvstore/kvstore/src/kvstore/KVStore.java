package kvstore;

import static kvstore.KVConstants.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import nu.xom.*;

/**
 * This is a basic key-value store. Ideally this would go to disk, or some other
 * backing store.
 */
public class KVStore implements KeyValueInterface {

    private ConcurrentHashMap<String, String> store;

    /**
     * Construct a new KVStore.
     */
    public KVStore() {
        resetStore();
    }

    private void resetStore() {
        this.store = new ConcurrentHashMap<String, String>();
    }

    /**
     * Insert key, value pair into the store.
     *
     * @param  key String key
     * @param  value String value
     */
    @Override
    public void put(String key, String value) {
        store.put(key, value);
    }

    /**
     * Retrieve the value corresponding to the provided key
     * @param  key String key
     * @throws KVException with ERROR_NO_SUCH_KEY if key does not exist in store
     */
    @Override
    public String get(String key) throws KVException {
        String retVal = this.store.get(key);
        if (retVal == null) {
            KVMessage msg = new KVMessage(KVConstants.RESP, ERROR_NO_SUCH_KEY);
            throw new KVException(msg);
        }
        return retVal;
    }

    /**
     * Delete the value corresponding to the provided key.
     *
     * @param  key String key
     * @throws KVException with ERROR_NO_SUCH_KEY if key does not exist in store
     */
    @Override
    public void del(String key) throws KVException {
        if(key != null) {
            if (!this.store.containsKey(key)) {
                KVMessage msg = new KVMessage(KVConstants.RESP, ERROR_NO_SUCH_KEY);
                throw new KVException(msg);
            }
            this.store.remove(key);
        }
    }

    /**
     * Writes an XML representation of this KVStore to a stream in UTF-8.
     * @param stream is the OutputStream to write to.
     */
    private void writeXMLToStream(OutputStream stream) throws IOException {
        Serializer serializer;
        try {
            serializer = new Serializer(stream, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // this won't actually happen
            return;
        }
        Element root = new Element("KVStore");
        Enumeration<String> keys = this.store.keys();
        String key;
        while (keys.hasMoreElements()) {
            try {
                key = keys.nextElement();
            } catch (NoSuchElementException e) {
                break; // this won't actually happen either
            }
            Element curr = new Element("KVPair");
            Element k = new Element("Key");
            Element v = new Element("Value");
            k.appendChild(key);
            v.appendChild(this.store.get(key));
            curr.appendChild(k);
            curr.appendChild(v);
            root.appendChild(curr);
        }
        serializer.setIndent(0);
        Document doc = new Document(root);
        serializer.write(doc);
    }

    /**
     * Serialize this store to XML. See the spec for specific output format.
     * This method is best effort. Any exceptions that appear can be dropped.
     */
    public String toXML() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            writeXMLToStream(out);
            return out.toString("UTF-8");
        } catch (Exception e) {
            return null; // drop any exceptions that arise
        }
    }

    @Override
    public String toString() {
        return this.toXML();
    }

    /**
     * Serialize to XML and write to a file.
     * This method is best effort. Any exceptions that arise can be dropped.
     * @param fileName the file to write the serialized store
     */
    public void dumpToFile(String fileName) {
        try {
            FileOutputStream out = new FileOutputStream(fileName);
            writeXMLToStream(out);
        } catch (Exception e) {
            // drop any exceptions that arise
        }
    }

    /**
     * Replaces the contents of the store with the contents of a file
     * written by dumpToFile; the previous contents of the store are lost.
     * The store is cleared even if the file doesn't exist.
     * This method is best effort. Any exceptions that arise can be dropped.
     *
     * @param fileName the file containing the serialized store data
     */
    public void restoreFromFile(String fileName) {
        resetStore();

        try {
            Builder parser = new Builder();
            Document doc = parser.build(new File(fileName));
            Element root = doc.getRootElement();
            Elements elems = root.getChildElements();
            for (int i = 0; i < elems.size(); i++) {
                Element elem = elems.get(i);
                Element k = elem.getChildElements().get(0);
                Element v = elem.getChildElements().get(1);
                put(k.getValue(), v.getValue());
            }
        } catch (Exception e) {
            // drop any exceptions that arise
        }
    }
}
