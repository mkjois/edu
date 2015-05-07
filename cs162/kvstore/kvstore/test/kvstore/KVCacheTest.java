package kvstore;

import static org.junit.Assert.*;

import org.junit.*;

public class KVCacheTest {
    
    @Test
    public void nullTest() {
    }

    /**
     * Verify the cache can put and get a KV pair successfully.
     */
    //@Test
    public void singlePutAndGet() {
        KVCache cache = new KVCache(1, 4);
        cache.put("hello", "world");
        assertEquals("world", cache.get("hello"));
    }
    
    public boolean test1(KVCache c){
	System.out.println("test1");
	c.put("1", "1");
	c.put("2", "2");
	c.put("3", "3");
	c.put("4", "4");
	c.put("5", "5");
	c.put("6", "6");
	c.put("7", "7");
	c.put("8", "8");
	assertEquals("1", c.get("1"));
	assertEquals("2", c.get("2"));
	assertEquals("3", c.get("3"));
	assertEquals("4", c.get("4"));
	assertEquals("5", c.get("5"));
	assertEquals("6", c.get("6"));
	assertEquals("7", c.get("7"));
	assertEquals("8", c.get("8"));
	return true;
    }

    public boolean test2(KVCache c){
	System.out.println("test2");
	c.put("1", "1");
	c.put("1", "2");
	c.put("1", "3");
	c.put("1", "4");
	assertEquals("4", c.get("1"));

	return true;
    }

    public boolean test3(KVCache c){
	System.out.println("test3");
	c.put("1", "1");
	c.put("2", "2");
	c.put("3", "3");
	c.put("4", "4");
	c.put("5", "5");
	c.put("6", "6");
	c.put("7", "7");
	c.put("8", "8");

	c.del("1");
	c.del("2");
	c.del("3");
	c.del("4");
	c.del("5");
	c.del("6");
	c.del("7");
	c.del("8");

	assertEquals(null, c.get("1"));
	assertEquals(null, c.get("2"));
	assertEquals(null, c.get("3"));
	assertEquals(null, c.get("4"));
	assertEquals(null, c.get("5"));
	assertEquals(null, c.get("6"));
	assertEquals(null, c.get("7"));
	assertEquals(null, c.get("8"));

	return true;
    }

    public boolean test4(KVCache c){
	System.out.println("test4");
	c.put("1", "1");
	c.put("2", "2");
	c.put("3", "3");
	c.put("4", "4");
	c.put("5", "5");
	c.put("6", "6");
	c.put("7", "7");
	c.put("8", "8");

	c.get("1");
	c.get("2");
	c.get("3");
	c.get("4");
	c.get("5");
	c.get("6");
	c.get("7");
	c.get("8");

	c.put("9", "9");
	assertEquals(null, c.get("1"));
	assertEquals("9", c.get("9"));

	c.del("1");
	c.del("2");
	c.del("3");
	c.del("4");
	c.del("5");
	c.del("6");
	c.del("7");
	c.del("8");
	c.del("9");

	c.put("1", "1");
	c.put("2", "2");
	c.put("3", "3");
	c.put("4", "4");
	c.put("5", "5");
	c.put("6", "6");
	c.put("7", "7");
	c.put("8", "8");

	c.get("1");
	c.get("2");

	c.put("9", "9");
	assertEquals(null, c.get("3"));
	assertEquals("9", c.get("9"));

	return true;
    }

    public boolean test5(KVCache c){

	c.put("1", "1");
	c.put("2", "2");
	c.put("3", "3");
	c.put("4", "4");
	c.put("5", "5");
	c.put("6", "6");
	c.put("7", "7");
	c.put("8", "8");
	System.out.println(c);
	return true;
    }


    //@Test
    public void cacheTest() {
	int numSets  = 2;
	int maxElemsPerSet = 4;
	KVCache c = new KVCache(numSets, maxElemsPerSet);
	boolean t1,t2,t3,t4,t5,p1,p2,p3,p4,p5;
	t1 = true;
	t2 = true;
	t3 = true;
	t4 = true;
	t5 = true;
	if (t1){
	    p1 = test1(c);
	    assertTrue("failed p1", p1);
	}
	if (t2){
	    c.del("1");
	    c.del("2");
	    c.del("3");
	    c.del("4");
	    c.del("5");
	    c.del("6");
	    c.del("7");
	    c.del("8");
	    p2 = test2(c);
	    assertTrue("failed p2", p2);
	}
	if (t3){
	    c.del("1");
	    c.del("2");
	    c.del("3");
	    c.del("4");
	    c.del("5");
	    c.del("6");
	    c.del("7");
	    c.del("8");
	    p3 = test3(c);
	    assertTrue("failed p3", p3);
	}
	if (t4){
	    c.del("1");
	    c.del("2");
	    c.del("3");
	    c.del("4");
	    c.del("5");
	    c.del("6");
	    c.del("7");
	    c.del("8");
	    p4 = test4(c);
	    assertTrue("failed p4", p4);
	}
	if (t5){
	    c.del("9");
	    c.del("2");
	    c.del("3");
	    c.del("4");
	    c.del("5");
	    c.del("6");
	    c.del("7");
	    c.del("8");
	    p5 = test5(c);
	    assertTrue("failed p5", p5);
	}
	System.out.println("done");
    }
}
