package kvstore;

import static kvstore.KVConstants.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class KVClientTest extends EndToEndTemplate {

    @Test(timeout = 20000)
    public void testInvalidKey() {
        try {
            client.put("", "bar");
            fail("Didn't fail on null value");
        } catch (KVException kve) {
            String errorMsg = kve.getKVMessage().getMessage();
            assertEquals(errorMsg, ERROR_INVALID_KEY);
        }
        try {
            client.put("key1", "value1");
            String value1 = client.get("key1");
            assertEquals("value1", value1); //Should be "value1"
        } catch (KVException kve) {
            fail("valid put/get failed");
        }
    }

}
