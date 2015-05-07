package dict;

/**
 * A container to map a key to a value, used in any dictionary-type data
 * structure.
 */
public class Entry {

  private Object key;
  private Object value;
  
  /**
   * Entry (Object, Object)
   *
   * Constructs a new Entry with the given key and value.
   *
   * @param k The key.
   * @param v The value the given key will be mapped to.
   */
  public Entry (Object k, Object v) {
      this.key = k;
      this.value = v;
  }

  /**
   * key () -- Object
   *
   * Returns the key of this Entry.
   *
   * @return This Entry's key.
   */
  public Object key () {
      return this.key;
  }

  /**
   * value () -- Object
   *
   * Returns the value of this Entry.
   *
   * @return This Entry's value.
   */
  public Object value () {
      return this.value;
  }
}
