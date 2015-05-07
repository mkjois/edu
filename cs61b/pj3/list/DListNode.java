package list;

/**
 * A node in a doubly-linked list. Stores an item, a reference
 * to the list that contains the node, and references to the
 * previous and next nodes in the list.
 */
public class DListNode {

  private DList myList;

  Object item;
  DListNode prev;
  DListNode next;

  /**
   * DListNode (Object, DList, DListNode, DListNode)
   * 
   * Constructs a new node.
   *
   * @param item The object to store.
   * @param list The DList this node will be a part of.
   * @param prev The previous node in the list.
   * @param next The next node in the list.
   */
  DListNode(Object item, DList list, DListNode prev, DListNode next) {
    this.item = item;
    this.myList = list;
    this.prev = prev;
    this.next = next;
  }
  
  /**
   * isValidNode (DList) -- boolean
   *
   * Determines whether or not this node is in the given list.
   *
   * @param list The DList in question.
   * @return True if this node is part of the given list, false otherwise and
   *    false if given list is null.
   */
  boolean isValidNode (DList list) {
      if (list == null) {
          return false;
      } else {
          return (list == this.myList);
      }
  }

  /**
   * item () -- Object
   *
   * Returns the object stored by this node.
   *
   * @return The object this node holds.
   */
  public Object item () {
      return this.item;
  }
  
  /**
   * reset ()
   *
   * Sets all fields to null to improve memory usage.
   */
  void reset () {
      this.item = null;
      this.myList = null;
      this.prev = null;
      this.next = null;
  }
}
