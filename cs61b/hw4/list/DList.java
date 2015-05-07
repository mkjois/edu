/* DList.java */

package list;

/**
 *  A DList is a mutable doubly-linked list ADT.  Its implementation is
 *  circularly-linked and employs a sentinel (dummy) node at the head
 *  of the list.
 *
 *  DO NOT CHANGE ANY METHOD PROTOTYPES IN THIS FILE.
 */

public class DList {

  /**
   *  head references the sentinel node.
   *  size is the number of items in the list.  (The sentinel node does not
   *       store an item.)
   *
   *  DO NOT CHANGE THE FOLLOWING FIELD DECLARATIONS.
   */

  protected DListNode head;
  protected int size;

  /* DList invariants:
   *  1)  head != null.
   *  2)  For any DListNode x in a DList, x.next != null.
   *  3)  For any DListNode x in a DList, x.prev != null.
   *  4)  For any DListNode x in a DList, if x.next == y, then y.prev == x.
   *  5)  For any DListNode x in a DList, if x.prev == y, then y.next == x.
   *  6)  size is the number of DListNodes, NOT COUNTING the sentinel,
   *      that can be accessed from the sentinel (head) by a sequence of
   *      "next" references.
   */

  /**
   *  newNode() calls the DListNode constructor.  Use this class to allocate
   *  new DListNodes rather than calling the DListNode constructor directly.
   *  That way, only this method needs to be overridden if a subclass of DList
   *  wants to use a different kind of node.
   *  @param item the item to store in the node.
   *  @param prev the node previous to this node.
   *  @param next the node following this node.
   */
  protected DListNode newNode(Object item, DListNode prev, DListNode next) {
    return new DListNode(item, prev, next);
  }

  /**
   *  DList() constructor for an empty DList.
   */
  public DList() {
      this.head = newNode(null, null, null);
      this.head.next = this.head;
      this.head.prev = this.head;
      this.size = 0;
  }

  /**
   *  isEmpty() returns true if this DList is empty, false otherwise.
   *  @return true if this DList is empty, false otherwise. 
   *  Performance:  runs in O(1) time.
   */
  public boolean isEmpty() {
    return size == 0;
  }

  /** 
   *  length() returns the length of this DList. 
   *  @return the length of this DList.
   *  Performance:  runs in O(1) time.
   */
  public int length() {
    return size;
  }

  /**
   *  insertFront() inserts an item at the front of this DList.
   *  @param item is the item to be inserted.
   *  Performance:  runs in O(1) time.
   */
  public void insertFront(Object item) {
      DListNode node = newNode(item, this.head, this.head.next);
      this.head.next = node;
      node.next.prev = node;
      this.size++;
  }

  /**
   *  insertBack() inserts an item at the back of this DList.
   *  @param item is the item to be inserted.
   *  Performance:  runs in O(1) time.
   */
  public void insertBack(Object item) {
      DListNode node = newNode(item, this.head.prev, this.head);
      this.head.prev = node;
      node.prev.next = node;
      this.size++;
  }

  /**
   *  front() returns the node at the front of this DList.  If the DList is
   *  empty, return null.
   *
   *  Do NOT return the sentinel under any circumstances!
   *
   *  @return the node at the front of this DList.
   *  Performance:  runs in O(1) time.
   */
  public DListNode front() {
      if (this.size > 0) {
          return this.head.next;
      } else {
          return null;
      }
  }

  /**
   *  back() returns the node at the back of this DList.  If the DList is
   *  empty, return null.
   *
   *  Do NOT return the sentinel under any circumstances!
   *
   *  @return the node at the back of this DList.
   *  Performance:  runs in O(1) time.
   */
  public DListNode back() {
      if (this.size > 0) {
          return this.head.prev;
      } else {
          return null;
      }
  }

  /**
   *  next() returns the node following "node" in this DList.  If "node" is
   *  null, or "node" is the last node in this DList, return null.
   *
   *  Do NOT return the sentinel under any circumstances!
   *
   *  @param node the node whose successor is sought.
   *  @return the node following "node".
   *  Performance:  runs in O(1) time.
   */
  public DListNode next(DListNode node) {
      if (node == null || node.next == this.head) {
          return null;
      } else {
          return node.next;
      }
  }

  /**
   *  prev() returns the node prior to "node" in this DList.  If "node" is
   *  null, or "node" is the first node in this DList, return null.
   *
   *  Do NOT return the sentinel under any circumstances!
   *
   *  @param node the node whose predecessor is sought.
   *  @return the node prior to "node".
   *  Performance:  runs in O(1) time.
   */
  public DListNode prev(DListNode node) {
      if (node == null || node.prev == this.head) {
          return null;
      } else {
          return node.prev;
      }
  }

  /**
   *  insertAfter() inserts an item in this DList immediately following "node".
   *  If "node" is null, do nothing.
   *  @param item the item to be inserted.
   *  @param node the node to insert the item after.
   *  Performance:  runs in O(1) time.
   */
  public void insertAfter(Object item, DListNode node) {
      if (node == null) {
          return;
      }
      DListNode nodeNew = newNode(item, node, node.next);
      nodeNew.next.prev = nodeNew;
      nodeNew.prev.next = nodeNew;
      this.size++;
  }

  /**
   *  insertBefore() inserts an item in this DList immediately before "node".
   *  If "node" is null, do nothing.
   *  @param item the item to be inserted.
   *  @param node the node to insert the item before.
   *  Performance:  runs in O(1) time.
   */
  public void insertBefore(Object item, DListNode node) {
      if (node == null) {
          return;
      }
      DListNode nodeNew = newNode(item, node.prev, node);
      nodeNew.next.prev = nodeNew;
      nodeNew.prev.next = nodeNew;
      this.size++;
  }

  /**
   *  remove() removes "node" from this DList.  If "node" is null, do nothing.
   *  Performance:  runs in O(1) time.
   */
  public void remove(DListNode node) {
      if (node == null || node == this.head) {
          return;
      }
      node.prev.next = node.next;
      node.next.prev = node.prev;
      this.size--;
  }

  /**
   *  toString() returns a String representation of this DList.
   *
   *  DO NOT CHANGE THIS METHOD.
   *
   *  @return a String representation of this DList.
   *  Performance:  runs in O(n) time, where n is the length of the list.
   */
  public String toString() {
    String result = "[  ";
    DListNode current = head.next;
    while (current != head) {
      result = result + current.item + "  ";
      current = current.next;
    }
    return result + "]";
  }
  
  // TESTING
  public static void main (String[] args) {
      DList d = new DList();
      System.out.println(d); // []
      d.insertFront(1);
      d.insertFront(2);
      System.out.println(d); // [2 1]
      d.insertBack(3);
      d.insertBack(4);
      d.insertFront(5);
      System.out.println(d); // [5 2 1 3 4]
      System.out.println(d.front().item); // 5
      System.out.println(d.back().item); // 4
      System.out.println((new DList()).front()); // null
      System.out.println((new DList()).back()); // null
      DListNode node;
      node = d.head.next.next.next;
      System.out.println(d.next(node).item); // 3
      System.out.println(d.prev(node).item); // 2
      d.insertAfter(6, node);
      d.insertBefore(7, node);
      d.insertAfter(8, d.head);
      d.insertBefore(9, d.head);
      d.insertBefore(10, d.head.next);
      d.insertAfter(11, d.head.prev);
      System.out.println(d); // [10 8 5 2 7 1 6 3 4 9 11]
      DListNode node2 = node.prev.prev.prev.prev;
      DListNode node3 = node.next.next;
      d.remove(node);
      d.remove(node2);
      d.remove(node3);
      d.remove(d.head);
      System.out.println(d); // [10 5 2 7 6 4 9 11]
  }
}
