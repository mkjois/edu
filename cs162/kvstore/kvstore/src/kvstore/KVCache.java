package kvstore;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.LinkedList;
import java.util.Hashtable;
import java.io.IOException;
import nu.xom.*;

/**
 * A set-associate cache which has a fixed maximum number of sets (numSets).
 * Each set has a maximum number of elements (MAX_ELEMS_PER_SET).
 * If a set is full and another entry is added, an entry is dropped based on
 * the eviction policy.
 */
public class KVCache implements KeyValueInterface {

    private Hashtable<Integer, LockList> cache;
    int numSets;
    
	/**
     * Constructs a second-chance-replacement cache.
     *
     * @param numSets the number of sets this cache will have
     * @param maxElemsPerSet the size of each set
     */
    public KVCache(int numSets, int maxElemsPerSet) {
        this.cache = new Hashtable<Integer, LockList>();
        this.numSets = numSets;
        for (int i = 0; i < numSets; i++){
        	LockList l = new LockList(maxElemsPerSet);
        	this.cache.put(i, l);
        }
    }

    /**
     * Retrieves an entry from the cache.
     * Assumes access to the corresponding set has already been locked by the
     * caller of this method.
     *
     * @param  key the key whose associated value is to be returned.
     * @return the value associated to this key or null if no value is
     *         associated with this key in the cache
     */
    @Override
    public String get(String key) {
        int id = getSetId(key);
        cacheNode n = cache.get(id).find(key);
        if (n != null){
        	n.setUsed(true);
        	return n.getValue();
        }
        return null;
    }

    /**
     * Adds an entry to this cache.
     * If an entry with the specified key already lives in the cache, it is
     * replaced by the new entry. When an entry is replaced, its reference bit
     * will be set to True. If the set is full, an entry is removed from
     * the cache based on the eviction policy. If the set is not full, the entry
     * will be inserted behind all existing entries. For this policy, we suggest
     * using a LinkedList over an array to keep track of entries in a set since
     * deleting an entry in an array will leave a gap in the array, likely not
     * at the end. More details and explanations in the spec. Assumes access to
     * the corresponding set has already been locked by the caller of this
     * method.
     *
     * @param key the key with which the specified value is to be associated
     * @param value a value to be associated with the specified key
     */
    @Override
    public void put(String key, String value) {
        int id = getSetId(key);
        LockList l = cache.get(id);
        cacheNode n = l.find(key);
        if (n != null){
        	n.setValue(value);
        	n.setUsed(true);
        } else {
        	cacheNode temp = new cacheNode(key,value);
        	if (l.isFull()) makeSpace(l);
        	l.getList().add(temp);
        }
    }

    /**
     * Removes an entry from this cache.
     * Assumes usage of the corresponding set has already been locked by the
     * caller of this method. Does nothing if called on a key not in the cache.
     *
     * @param key key with which the specified value is to be associated
     */
    @Override
    public void del(String key) {
        int id = getSetId(key);
        LockList l = cache.get(id);
        cacheNode n = l.find(key);
        if (n != null){
        	l.getList().remove(n);
        }
    }

    /**
     * Get a lock for the set corresponding to a given key.
     * The lock should be used by the caller of the get/put/del methods 
     * so that different sets can be modified in parallel.
     *
     * @param  key key to determine the lock to return
     * @return lock for the set that contains the key
     */
    public Lock getLock(String key) {
        int id = getSetId(key);
        LockList l = cache.get(id);
        return l.getLock();
    }

    /**
     * Get the id of the set for a particular key.
     *
     * @param  key key of interest
     * @return set of the key
     */
    private int getSetId(String key) {
        int h = key.hashCode();
        int id = h % this.numSets;
        if (id < 0 ) id = id + this.numSets;
        return id;
    }
    
    private void makeSpace(LockList l){
    	boolean replaced = false;
    	LinkedList<cacheNode> list = l.getList();
    	for (cacheNode n : list){
    		if (n.getUsed() == false){
    			list.remove(n);
    			replaced = true;
    			break;
    		} 
    		n.setUsed(false);
    	}
    	if (!replaced){ //if all used bits were true initially, then remove head
    		list.removeFirst();
    	}

    }


    /**
     * Serialize this store to XML. See spec for details on output format.
     */
    public String toXML() {
    	
    	Element rootNode = new Element("KVCache");
    		
    	for (int s = 0; s < this.numSets; s++){
    		Element set = new Element("Set");
    		String name = String.valueOf(s);
    		
    		Attribute Id = new Attribute("Id", name);
    		set.addAttribute(Id);
    		
    		LockList l = cache.get(s);
    		for (cacheNode c : l.set){
    			Element cacheEntry = new Element("CacheEntry");
    			Attribute isReferenced = new Attribute("isReferenced", String.valueOf(c.getUsed()));
    			cacheEntry.addAttribute(isReferenced);
    			
    			Element key = new Element("Key");
    			key.appendChild(c.getKey());
    			
    			Element value = new Element("Value");
    			value.appendChild(c.getValue());
    			
    			cacheEntry.appendChild(key);
    			cacheEntry.appendChild(value);
    			set.appendChild(cacheEntry);
    		}
    		
    		rootNode.appendChild(set);
    		
    	}
    	Document output = new Document(rootNode);
    	return output.toXML();
    }

    @Override
    public String toString() {
        return this.toXML();
    }
    
    private class LockList{
    	ReentrantLock lock;
    	LinkedList<cacheNode> set;
    	int size;
    	
    	LockList(int size){
    			this.size = size;
    			this.set = new LinkedList<cacheNode>();
    			this.lock = new ReentrantLock();
    	}
    	
    	boolean isFull(){
    			return this.size == this.set.size(); 
    	}
    	
    	cacheNode find(String key){
    			for (cacheNode node : this.set){
    					if (key == node.key) return node;
    			}
    			return null;
    	}
    	
    	LinkedList<cacheNode> getList(){
    		return this.set;
    	}
    	
    	ReentrantLock getLock(){
    		return this.lock;
    	}
    }
    
    private class cacheNode{
    	boolean used;
    	String key;
    	String value;
    	cacheNode(String k, String v){
    		this.key = k;
    		this.value = v;
    		this.used = false;
    	}
    	
    	boolean getUsed(){
    		return this.used;
    	}
    	
    	void setUsed(boolean b){
    		this.used = b;
    	}
    	
    	String getKey(){
    		return this.key;
    	}
    	
    	String getValue(){
    		return this.value;
    	}
    	
    	void setValue(String value){
    		this.value = value;	
    	}
    }

}
