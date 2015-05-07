package kvstore;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

import java.net.Socket;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

import java.io.*;

public class TPCMasterHandlerTest {

    public static KVMessage sendAndReceive(String message,
                                        TPCMasterHandler handler)
                                        throws KVException {
        Socket s = mock(Socket.class);
        InputStream inStream =
            new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        try {
            when(s.getInputStream()).thenReturn(inStream);
            when(s.getOutputStream()).thenReturn(outStream);
        } catch (IOException e) { }
        Socket s2 = mock(Socket.class);
        handler.handle(s);
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) { }
        String resp = null;
        try {
            resp = outStream.toString("UTF-8");
        } catch (UnsupportedEncodingException e) { }
        InputStream inStream2 = new ByteArrayInputStream(resp.getBytes(StandardCharsets.UTF_8));
        try {
            when(s2.getInputStream()).thenReturn(inStream2);
            when(s2.getOutputStream()).thenReturn(outStream);
        } catch (IOException e) { }
        return new KVMessage(s2);
    }

    public static String createPut(String key, String value) throws KVException {
        KVMessage m = new KVMessage(KVConstants.PUT_REQ);
        m.setKey(key);
        m.setValue(value);
        return m.toXML();
    }

    public static String createDel(String key) throws KVException {
        KVMessage m = new KVMessage(KVConstants.DEL_REQ);
        m.setKey(key);
        return m.toXML();
    }

    public static String createGet(String key) throws KVException {
        KVMessage m = new KVMessage(KVConstants.GET_REQ);
        m.setKey(key);
        return m.toXML();
    }

    @Test
    public void unitTestMasterHandler() throws KVException {
        try {
        KVServer server = new KVServer(10, 10);
        TPCLog log = new TPCLog("testFile.test", server);
        TPCMasterHandler slave = new TPCMasterHandler(1, server, log);
        String req;
        KVMessage resp;
        for (int i = 0; i < 10; i++) {
            req = createPut((new Integer(i)).toString(),
                            (new Integer(i*7%10)).toString());
            resp = sendAndReceive(req, slave);
            assertEquals(resp.getMsgType(), KVConstants.READY);
            try {
                server.get((new Integer(i)).toString());
            } catch (KVException e) { }
            if (i < 5) {
                resp = sendAndReceive((new KVMessage(KVConstants.COMMIT)).toXML(), slave);
            } else {
                resp = sendAndReceive((new KVMessage(KVConstants.ABORT)).toXML(), slave);
            }
            assertEquals(resp.getMsgType(), KVConstants.ACK);
        }
        for (int i = 0; i < 5; i++) {
            req = createGet((new Integer(i)).toString());
            resp = sendAndReceive(req, slave);
            assertEquals(resp.getValue(), (new Integer(i*7%10)).toString());
        }
        for (int i = 5; i < 10; i++) {
            req = createGet((new Integer(i)).toString());
            resp = sendAndReceive(req, slave);
            assertEquals(resp.getMessage(), KVConstants.ERROR_NO_SUCH_KEY);
        }
        for (int i = 0; i < 5; i++) {
            req = createDel((new Integer(i)).toString());
            resp = sendAndReceive(req, slave);
            assertEquals(resp.getMsgType(), KVConstants.READY);
            resp = sendAndReceive((new KVMessage(KVConstants.COMMIT)).toXML(), slave);
            assertEquals(resp.getMsgType(), KVConstants.ACK);
        }
        for (int i = 0; i < 5; i++) {
            req = createGet((new Integer(i)).toString());
            resp = sendAndReceive(req, slave);
            assertEquals(resp.getMessage(), KVConstants.ERROR_NO_SUCH_KEY);
        }
        } catch (KVException e) { fail(e.getKVMessage().toString()); }
        new File("testFile.test").deleteOnExit();
    }
}
