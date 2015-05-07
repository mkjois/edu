package list;

import java.util.*;

/**
 * A Doubly-linked list implementation that is Iterable for Objects using
 * the Java "for-each loop" syntax.
 */
public class DList implements Iterable<Object> {
    
    /**
     * DListIterator
     *
     * The iterator of Object objects over a DList.
     */
    private class DListIterator implements Iterator<Object> {

        private DList d;
        private DListNode n;

        private DListIterator (DList d) {
            this.d = d;
            this.n = this.d.head;
        }

        public boolean hasNext () {
            return (this.n.next != this.d.head);
        }

        public Object next () {
            if (this.hasNext()) {
                this.n = this.n.next;
                return this.n.item;
            } else {
                throw new NoSuchElementException();
            }
        }

        public void remove () {
        }
    }

  private DListNode head;
  private int lastIndex;

  /**
   * DList ()
   *
   * Constructs a new DList with no items.
   */
  public DList () {
      this.head = new DListNode(null, this, null, null);
      this.head.next = this.head;
      this.head.prev = this.head;
      this.lastIndex = -1;
  }

  /**
   * isEmpty () -- boolean
   *
   * Determines whether or not this list contains items.
   *
   * @return True if this DList contains no objects, false otherwise.
   */
  public boolean isEmpty () {
      return (this.lastIndex == -1);
  }
  
  /**
   * length () -- int
   *
   * Gives the length (i.e. size) of this list.
   *
   * @return The number of objects in this DList.
   */
  public int length () {
      return this.lastIndex + 1;
  }
  
  /**
   * add (Object)
   *
   * Adds an object to the end of this list, which is extended dynamically.
   *
   * @param item The object to be added to the end of this DList.
   */
  public void add (Object item) {
      DListNode node = new DListNode(item, this, this.head.prev, this.head);
      this.head.prev = node;
      node.prev.next = node;
      this.lastIndex++;
  }

  /**
   * addNode (Object) -- DListNode
   *
   * Adds an object to the end of this list and returns the node that
   * contains the object.
   *
   * @param item The object to add to the end of this DList.
   * @return The DListNode containing the given object.
   */
  public DListNode addNode (Object item) {
      DListNode node = new DListNode(item, this, this.head.prev, this.head);
      this.head.prev = node;
      node.prev.next = node;
      this.lastIndex++;
      return node;
  }

  /**
   * insert (Object, int)
   *
   * Inserts an object before the given index in this DList. If the index
   * doesn't exist with respect to this DList, throws an exception.
   *
   * @param item The object to be added.
   * @param index The index of this DList, such that the new item is inserted
   *    before this position.
   * @exception ArrayIndexOutOfBoundsException Thrown if the given index is out
   *    of the range of this DList.
   */
  public void insert (Object item, int index) {
      if ((index < 0) || (index > this.lastIndex)) {
          throw new ArrayIndexOutOfBoundsException(index);
      } else {
          DListNode node = this.head;
          for (int k = 0; k < index; k++) {
              node = node.next;
          }
          DListNode newNode = new DListNode(item, this, node, node.next);
          node.next = newNode;
          newNode.next.prev = newNode;
          this.lastIndex++;
      }
  }
  
  /**
   * indexOf (Object, int) -- int
   *
   * Gives the index of the given occurrence of the given object in this DList.
   * If given occurrence is less than 1 or the given occurrence of the object
   * is not in this DList, returns -1.
   *
   * @param item The object in question.
   * @param occurrence The nth occurrence of the given object.
   * @return The index in this DList of the nth occurrence of the object. If
   *    such an occurrence doesn't exist in this DList or if given occurrence
   *    is invalid, returns -1.
   */
  public int indexOf (Object item, int occurrence) {
      if (occurrence < 1) {
          return -1;
      }
      int index = 0;
      for (Object o : this) {
          if (o.equals(item)) {
              occurrence--;
              if (occurrence == 0) {
                  return index;
              }
          }
          index++;
      }
      return -1;
  }
  
  /**
   * contains (Object) -- boolean
   *
   * Determines whether or not the given object is in this DList.
   *
   * @param item The object in question.
   * @return True if given object is in this DList, false otherwise.
   */
  public boolean contains (Object item) {
      for (Object o : this) {
          if (o.equals(item)) {
              return true;
          }
      }
      return false;
  }

  /**
   * get (int) -- Object
   *
   * Returns the object in this DList at the given index. If given index
   * is not valid for this DList, throws an exception.
   *
   * @param index The index of the object to return.
   * @return The object at the given index, if valid, in this DList.
   * @exception ArrayIndexOutOfBoundsException Thrown if given index doesn't
   *    exist in this DList.
   */
  public Object get (int index) {
      if ((index < 0) || (index > this.lastIndex)) {
          throw new ArrayIndexOutOfBoundsException(index);
      }
      DListNode node = this.head.next;
      for (int k = 0; k < index; k++) {
          node = node.next;
      }
      return node.item;
  }
  
  /**
   * concat (DList)
   *
   * Concatenates this DList with another DList without mutating the other one.
   * This list becomes the sum of both lists.
   *
   * @param other The DList whose elements are to be added to this DList.
   */
  public void concat (DList other) {
      for (Object o : other) {
          this.add(o);
      }
  }

  /**
   * append (DList)
   *
   * Appends the given DList to this DList in constant time, making the given
   * one empty.
   *
   * @param other The DList to append to this DList.
   */
  public void append (DList other) {
      if (other == null || other.lastIndex == -1) {
          return;
      }
      this.head.prev.next = other.head.next;
      this.head.prev.next.prev = this.head.prev;
      this.head.prev = other.head.prev;
      this.head.prev.next = this.head;
      this.lastIndex += other.length();
      other.head = new DListNode(null, other, null, null);
      other.head.next = other.head;
      other.head.prev = other.head;
      other.lastIndex = -1;
  }
  
  /**
   * pop () -- Object
   *
   * Removes and returns the last object in this DList if there is one,
   * otherwise returns null.
   *
   * @return The last object in this DList if there is one, otherwise null.
   */
  public Object pop () {
      if (this.lastIndex == -1) {
          return null;
      } else {
          DListNode last = this.head.prev;
          Object popped = last.item;
          this.head.prev = last.prev;
          this.head.prev.next = this.head;
          this.lastIndex--;
          return popped;
      }
  }
  
  /**
   * remove (int) -- Object
   *
   * Removes and returns the object at the given index in this list. If given
   * index is invalid, throws an exception.
   *
   * @param index The index of the object to remove.
   * @exception ArrayIndexOutOfBoundsException Thrown if given index is invalid.
   */
  public Object remove (int index) {
      if ((index < 0) || (index > this.lastIndex)) {
          throw new ArrayIndexOutOfBoundsException(index);
      } else {
          DListNode node = this.head.next;
          for (int k = 0; k < index; k++) {
              node = node.next;
          }
          Object removed = node.item;
          node.prev.next = node.next;
          node.next.prev = node.prev;
          node.reset();
          this.lastIndex--;
          return removed;
      }
  }

  /**
   * remove (Object)
   *
   * Removes the first occurrence of the given Object from this list.
   * Does nothing if the Object is not found.
   *
   * @param item The Object in question.
   */
  public void remove (Object item) {
      DListNode node = this.head.next;
      while (node != this.head) {
          if (item.equals(node.item)) {
              node.prev.next = node.next;
              node.next.prev = node.prev;
              this.lastIndex--;
              return;
          }
          node = node.next;
      }
  }

  /**
   * removeNode (DListNode)
   *
   * Removes the given node from this list. If the node is not in this list,
   * does nothing.
   *
   * @param node The DListNode to remove.
   */
  public void removeNode (DListNode node) {
      if (! node.isValidNode(this)) {
          return;
      }
      node.prev.next = node.next;
      node.next.prev = node.prev;
      node.reset();
      this.lastIndex--;
  }

  /**
   * iterator () -- Iterator<Object>
   *
   * Returns an iterator over this list. Enables iteration through DList via the
   * Java for-each loop syntax:
   *
   *     DList d = new DList();
   *     // add objects to d
   *     for (Object o : d) {
   *         // process o
   *     }
   *
   * @return An iterator of Objects over this DList.
   */
  public Iterator<Object> iterator () {
      return new DListIterator(this);
  }
  
  /**
   * toString () -- String
   *
   * Returns the String representation of this list.
   *
   * @return The String which represents this DList.
   */
  public String toString () {
      String result = "[  ";
      DListNode current = head.next;
      while (current != head) {
        result = result + current.item.toString() + "  ";
        current = current.next;
      }
      return result + "]";
  }
}

