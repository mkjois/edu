package kvstore;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

import java.net.Socket;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

import java.io.*;

public class TPCEndToEndTest extends TPCEndToEndTemplate {


    @Test(timeout = 150)
    public void testPutGet() throws KVException {
        client.put("foo", "bar");
        assertEquals("get failed", client.get("foo"), "bar");
    }


}
