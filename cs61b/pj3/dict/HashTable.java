package dict;

import list.*;

/**
 * Our hash table implementation, including resizing.
 */
public class HashTable {

  private DList[] table;
  private int size;
  private int compA, compB, compP, compN; // compN = number of buckets

  private static final int DEFAULT_SIZE_EST = 5; // always set to a prime number
  private static final double MAXLOAD = 1.0, MINLOAD = 0.25;

  /**
   * HashTable (int)
   *
   * Constructs a new empty hash table intended to hold roughly sizeEstimate
   * entries.
   *
   * @param sizeEstimate An estimation of the number of entries to store.
   */
  public HashTable (int sizeEstimate) {
      int[] primes = PrimeGen.primes(sizeEstimate, sizeEstimate * 2);
      int prime = primes[primes.length / 2];
      this.generateCompParams(prime);
      this.makeEmpty();
  }

  /**
   * HashTable ()
   *
   * Constructs a new empty hash table with a default size.
   */
  public HashTable () {
      this.generateCompParams(DEFAULT_SIZE_EST);
      this.makeEmpty();
  }

  /** 
   * size () -- int
   *
   * Returns the number of entries stored in the dictionary.  Entries with
   * the same key (or even the same key and value) each still count as
   * a separate entry.
   *
   * @return The number of entries in the dictionary.
   */
  public int size () {
      return this.size;
  }

  /** 
   * isEmpty () -- boolean
   *
   * Tests if the dictionary is empty.
   *
   * @return True if the dictionary has no entries, false otherwise.
   */
  public boolean isEmpty () {
      return (this.size == 0);
  }

  /**
   * insert (Object, Object) -- Entry
   *
   * Create a new Entry object referencing the input key and associated value,
   * and insert the entry into the dictionary.  Return a reference to the new
   * entry.  Multiple entries with the same key (or even the same key and
   * value) can coexist in the dictionary. Resizes the hash table with twice the
   * current number of buckets if the load factor grows too large.
   *
   * @param key The key by which the entry can be retrieved.
   * @param value An arbitrary object.
   * @return An Entry containing the key and value.
   */
  public Entry insert (Object key, Object value) {
      Entry e = new Entry(key, value);
      int bucket = this.compFunction(key.hashCode());
      if (this.table[bucket] == null) {
          this.table[bucket] = new DList();
      }
      this.table[bucket].add(e);
      this.size++;
      if (this.loadFactor() >= MAXLOAD) {
          this.resize(2 * this.compN);
      }
      return e;
  }

  /**
   * find (Object) -- Entry
   *
   * Search for an entry with the specified key.  If such an entry is found,
   * return it; otherwise return null.  If several entries have the specified
   * key, choose one arbitrarily and return it.
   *
   * @param key The search key.
   * @return An Entry containing the key and an associated value, or null if
   *     no entry contains the specified key.
   */
  public Entry find (Object key) {
      int bucket = this.compFunction(key.hashCode());
      Entry e, result = null;
      if (this.table[bucket] != null) {
          for (Object o : this.table[bucket]) {
              e = (Entry) o;
              if (key.equals(e.key())) {
                  result = e;
                  break;
              }
          }
          return result;
      } else {
          return null;
      }
  }

  /** 
   * remove (Object) -- Entry
   *
   * Remove an entry with the specified key.  If such an entry is found,
   * remove it from the table and return it; otherwise return null.
   * If several entries have the specified key, choose the first one, then
   * remove and return it. Resizes the hash table with half the current number
   * of buckets if the load factor becomes too small.
   *
   * @param key The search key.
   * @return An Entry containing the key and an associated value, or null if
   *     no entry contains the specified key.
   */
  public Entry remove (Object key) {
      int bucket = this.compFunction(key.hashCode());
      boolean removed = false;
      Entry e, result = null;
      if (this.table[bucket] != null) {
          for (Object o : this.table[bucket]) {
              e = (Entry) o;
              if (e.key().equals(key)) {
                  result = e;
                  this.table[bucket].remove(e);
                  this.size--;
                  removed = true;
                  break;
              }
          }
      }
      if (removed && (this.size > 0) && (this.loadFactor() <= MINLOAD)) {
          this.resize(this.compN / 2);
      }
      return result;
  }

  /**
   * makeEmpty ()
   *
   * Remove all entries from the dictionary.
   */
  public void makeEmpty () {
      this.table = new DList[this.compN];
      this.size = 0;
  }

  /**
   * compFunction (int) -- int
   *
   * Converts a hash code in the range Integer.MIN_VALUE...Integer.MAX_VALUE
   * to a value in the range [0, N-1], where N is the number of buckets.
   *
   * h(code) = ((A*code + B) % P) % N)
   *
   * @param code The hash code to map to a bucket.
   * @return The bucket the hash code maps to.
   */
  private int compFunction (int code) {
      return posMod(posMod(this.compA * code + this.compB, this.compP), this.compN);
  }
  
  /**
   * resize (int)
   *
   * Resizes the hash table to keep the load factor in a roughly constant range
   * so as to keep hash table operations running in roughly constant time.
   *
   * @param numBuckets The new number of buckets to have.
   */
  private void resize (int numBuckets) {
      DList[] resized = new DList[numBuckets];
      this.generateCompParams(numBuckets);
      Entry e;
      int bucket;
      for (DList d : this.table) {
          if (d != null) {
              for (Object o : d) {
                  e = (Entry) o;
                  bucket = this.compFunction(e.key().hashCode());
                  if (resized[bucket] == null) {
                      resized[bucket] = new DList();
                  }
                  resized[bucket].add(e);
              }
          }
      }
      this.table = resized;
  }

  /**
   * Generates parameters A, B, P, N for a good compression function:
   *
   * h(code) = ((A*code + B) % P) % N)
   */
  private void generateCompParams (int numBuckets) {
      this.compN = numBuckets;
      this.compA = PrimeGen.nextPrime(this.compN / 11);
      this.compB = PrimeGen.nextPrime(this.compA * 3);
      int[] potentials = PrimeGen.primes(1000*this.compN, 1000*this.compN + 1000);
      int bestP = potentials[0], minRemainder = this.compN;
      for (int i : potentials) {
          if (i % this.compN < minRemainder) {
              minRemainder = i % this.compN;
              bestP = i; // choose P so P > 1000N and P close to a multiple of N
          }
      }
      this.compP = bestP;
  }

  /**
   * Computes the load factor n/N
   */
  private double loadFactor () {
      return ((double) this.size) / this.compN;
  }

  /**
   * Computes the positive value of (a mod b)
   */
  private static int posMod (int a, int b) {
      int modulus = a % b;
      if (modulus < 0) {
          modulus += b;
      }
      return modulus;
  }
}
