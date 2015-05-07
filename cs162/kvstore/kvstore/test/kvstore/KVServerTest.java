package kvstore;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

public class KVServerTest {

    KVServer server;

    @Before
    public void setupServer() {
        server = new KVServer(10, 10);
    }

    @Test
    public void fuzzTest() throws KVException {
        Random rand = new Random(8); // no reason for 8
        Map<String, String> map = new HashMap<String, String>(10000);
        String key, val;
        for (int i = 0; i < 10000; i++) {
            key = Integer.toString(rand.nextInt());
            val = Integer.toString(rand.nextInt());
            server.put(key, val);
            map.put(key, val);
        }
        Iterator<Map.Entry<String, String>> mapIter = map.entrySet().iterator();
        Map.Entry<String, String> pair;
        while(mapIter.hasNext()) {
            pair = mapIter.next();
            assertTrue(server.hasKey(pair.getKey()));
            assertEquals(pair.getValue(), server.get(pair.getKey()));
            mapIter.remove();
        }
        assertTrue(map.size() == 0);
    }

    @Test
    public void testNonexistentGetFails() {
        try {
            server.get("this key shouldn't be here");
            fail("get with nonexistent key should error");
        } catch (KVException e) {
            assertEquals(KVConstants.RESP, e.getKVMessage().getMsgType());
            assertEquals(KVConstants.ERROR_NO_SUCH_KEY, e.getKVMessage().getMessage());
        }
    }

    @Test
    public void redefinitionTest() {
        try {
            for (int i = 0; i < 10; i++) {
                server.put(new Integer(i).toString(),
                           new Integer(i * 7 % 10).toString());
            }
            for (int i = 0; i < 10; i++) {
                assertEquals(server.get(new Integer(i).toString()),
                             new Integer(i * 7 % 10).toString());
            }
            for (int i = 1; i < 10; i += 2) {
                server.put(new Integer(i).toString(),
                           new Integer(i * 100).toString());
            }
            for (int i = 0; i < 10; i += 2) {
                assertEquals(server.get(new Integer(i).toString()),
                             new Integer(i * 7 % 10).toString());
            }
            for (int i = 1; i < 10; i += 2) {
                assertEquals(server.get(new Integer(i).toString()),
                             new Integer(i * 100).toString());
            }
        } catch (Exception e) {
            fail("redefinition test failed: " + e.toString());
        }
    }
}
