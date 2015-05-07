package kvstore;

import static kvstore.KVConstants.*;
import static org.junit.Assert.*;

import org.junit.*;

public class KVStoreTest {

    KVStore store;

    @Before
    public void setupStore() {
        store = new KVStore();
    }

    @Test
    public void putAndGetOneKey() throws KVException {
        String key = "this is the key.";
        String val = "this is the value.";
        store.put(key, val);
        assertEquals(val, store.get(key));
    }

    @Test
    public void testFileWritingAndParsing() throws KVException {
        for (int i = 0; i < 5; i++) {
            store.put((new Integer(i)).toString(),
                      (new Integer(i)).toString());
        }
        store.dumpToFile("test.xml");
        for (int i = 5; i < 10; i++) {
            store.put((new Integer(i)).toString(),
                      (new Integer(i)).toString());
        }
        for (int i = 0; i < 10; i++) {
            assertEquals(store.get((new Integer(i)).toString()),
                         (new Integer(i)).toString());
        }
        store.restoreFromFile("test.xml");
        for (int i = 0; i < 5; i++) {
            assertEquals(store.get((new Integer(i)).toString()),
                         (new Integer(i)).toString());
        }
        for (int i = 5; i < 10; i++) {
            try {
                store.get((new Integer(i)).toString());
                fail("new keys still exist after restoration");
            } catch (KVException e) { }
        }
    }

}
