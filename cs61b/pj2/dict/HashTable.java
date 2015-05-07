package dict;

import list.*;

/**
 * Our hash table implementation.
 */
public class HashTable {

  private DList[] table;
  private int size, collisions;
  private int compA, compB, compP, compN;
  protected static final int DEFAULT_SIZE_EST = 103;

  private void generateCompParams (int numBuckets) {
      this.compN = numBuckets;
      this.compA = PrimeGen.nextPrime(this.compN / 11);
      this.compB = PrimeGen.nextPrime(this.compA * 3);
      int[] potentials = PrimeGen.primes(1000*this.compN, 1000*this.compN + 1000);
      int bestP = potentials[0], minRemainder = this.compN;
      for (int i : potentials) {
          if (i % this.compN < minRemainder) {
              minRemainder = i % this.compN;
              bestP = i; // chooses P such that P > 1000N and P is close to a multiple of N
          }
      }
      this.compP = bestP;
  }

  private void initHashTable () {
      this.table = new DList[this.compN];
      this.size = 0;
      this.collisions = 0;
  }

  /** 
   *  Construct a new empty hash table intended to hold roughly sizeEstimate
   *  entries.
   **/

  public HashTable (int sizeEstimate) {
      int[] primes = PrimeGen.primes(sizeEstimate, sizeEstimate * 2);
      int prime = primes[primes.length / 2];
      this.generateCompParams(prime);
      this.initHashTable();
  }

  /** 
   *  Construct a new empty hash table with a default size.
   **/

  public HashTable () {
      this.generateCompParams(DEFAULT_SIZE_EST);
      this.initHashTable();
  }

  // Computes the positive value of (a mod b)
  private static int posMod (int a, int b) {
      int modulus = a % b;
      if (modulus < 0) {
          modulus += b;
      }
      return modulus;
  }

  /**
   *  Converts a hash code in the range Integer.MIN_VALUE...Integer.MAX_VALUE
   *  to a value in the range [0, (number of buckets)-1]
   **/

  private int compFunction(int code) {
      return posMod(posMod(this.compA * code + this.compB, this.compP), this.compN);
  }

  /** 
   *  Returns the number of entries stored in the dictionary.  Entries with
   *  the same key (or even the same key and value) each still count as
   *  a separate entry.
   *  @return number of entries in the dictionary.
   **/

  public int size() {
      return this.size;
  }

  /** 
   *  Tests if the dictionary is empty.
   *
   *  @return true if the dictionary has no entries; false otherwise.
   **/

  public boolean isEmpty() {
      return (this.size == 0);
  }

  /**
   *  Create a new Entry object referencing the input key and associated value,
   *  and insert the entry into the dictionary.  Return a reference to the new
   *  entry.  Multiple entries with the same key (or even the same key and
   *  value) can coexist in the dictionary.
   *
   *  This method should run in O(1) time if the number of collisions is small.
   *
   *  @param key the key by which the entry can be retrieved.
   *  @param value an arbitrary object.
   *  @return an entry containing the key and value.
   **/
  public Entry insert(Object key, Object value) {
      Entry e = new Entry(key, value);
      int bucket = this.compFunction(key.hashCode());
      if (this.table[bucket] == null) {
          this.table[bucket] = new DList();
      } else {
          this.collisions++;
      }
      this.table[bucket].add(e);
      this.size++;
      return e;
  }

  /** 
   *  Search for an entry with the specified key.  If such an entry is found,
   *  return it; otherwise return null.  If several entries have the specified
   *  key, choose one arbitrarily and return it.
   *
   *  This method should run in O(1) time if the number of collisions is small.
   *
   *  @param key the search key.
   *  @return an entry containing the key and an associated value, or null if
   *          no entry contains the specified key.
   **/
  public Entry find(Object key) {
      int bucket = this.compFunction(key.hashCode());
      Entry e, result = null;
      if (this.table[bucket] != null) {
          for (Object o : this.table[bucket]) {
              e = (Entry) o;
              if (e.key().equals(key)) {
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
   *  Remove an entry with the specified key.  If such an entry is found,
   *  remove it from the table and return it; otherwise return null.
   *  If several entries have the specified key, choose one arbitrarily, then
   *  remove and return it.
   *
   *  This method should run in O(1) time if the number of collisions is small.
   *
   *  @param key the search key.
   *  @return an entry containing the key and an associated value, or null if
   *          no entry contains the specified key.
   */

  public Entry remove(Object key) {
      int bucket = this.compFunction(key.hashCode());
      int index = 0;
      Entry e, result = null;
      for (Object o : this.table[bucket]) {
          e = (Entry) o;
          if (e.key().equals(key)) {
              result = e;
              this.table[bucket].remove(index);
              this.size--;
              break;
          }
          index++;
      }
      return result;
  }

  /**
   *  Remove all entries from the dictionary.
   */
  public void makeEmpty() {
      this.table = new DList[this.compN];
      this.size = 0;
  }
}
