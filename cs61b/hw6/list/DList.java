package list;

import java.util.*;

public class DList implements Iterable<Object> {
    
    /**
     * DListIterator
     * The iterator of Object objects through a DList.
     */
    private class DListIterator implements Iterator<Object> {

        private DList d;
        private DListNode n;

        DListIterator (DList d) {
            this.d = d;
            this.n = this.d.head;
        }

        public boolean hasNext () {
            return this.n.next != this.d.head;
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

  protected DListNode head;
  protected int lastIndex;

  /**
   * DList()
   * Constructs a new DList object with size 0.
   */
  public DList () {
      this(new Object[0]);
  }

  /**
   * DList(Object[])
   * Constructs a new DList object with the objects in the given array.
   *
   * @param objArray The array of objects added to this DList.
   */
  public DList (Object[] objArray) {
      this.head = new DListNode(null, null, null);
      this.head.next = this.head;
      this.head.prev = this.head;
      this.lastIndex = -1;
      for (Object o : objArray) {
          this.add(o);
      }
  }
  
  /**
   * isEmpty()
   * Determines whether or not there are items in this DList.
   *
   * @return true if this DList contains no objects, false otherwise.
   */
  public boolean isEmpty () {
    return lastIndex == -1;
  }
  
  /**
   * length()
   * Gives the length (i.e. size) of this DList.
   *
   * @return The number of objects in this DList.
   */
  public int length () {
    return lastIndex + 1;
  }
  
  /**
   * add(Object)
   * Adds an object to the end of this DList. This DList is extended
   * dynamically. 
   *
   * @param item The object to be added to the end of this DList.
   */
  public void add (Object item) {
      DListNode node = new DListNode(item, this.head.prev, this.head);
      this.head.prev = node;
      node.prev.next = node;
      this.lastIndex++;
  }
  
  /**
   * insert(Object, int)
   * Inserts an object before the given index in this DList. If the index
   * doesn't exist with respect to this DList, throws an exception. This
   * DList is extended dynamically.
   *
   * @param item The object to be added.
   * @param index The index of this DList, such that the new item is inserted
   *    before this position.
   * @exception ArrayIndexOutOfBoundsException Thrown if the given index is out
   *    of the range of the DList.
   */
  public void insert (Object item, int index) throws ArrayIndexOutOfBoundsException {
      if ((index < 0) || (index > this.lastIndex)) {
          throw new ArrayIndexOutOfBoundsException(index);
      } else {
          DListNode node = this.head;
          for (int k = 0; k < index; k++) {
              node = node.next;
          }
          DListNode newNode = new DListNode(item, node, node.next);
          node.next = newNode;
          newNode.next.prev = newNode;
          this.lastIndex++;
      }
  }
  
  /**
   * indexOf(Object, int)
   * Gives the index of the given occurrence of the given object in this DList.
   * If given occurrence is less than 1 or the given occurrence of the object
   * is not in this DList, returns -1.
   *
   * @param item The object to find the index of in this DList.
   * @param occurrence The nth occurrence of the given object. Must be greater
   *    than 0 to be valid.
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
          if (o == item) {
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
   * contains(Object)
   * Determines whether or not a given object is in this DList.
   *
   * @param item The object to be searched for in this DList.
   * @return true if given object is in this DList, false otherwise.
   */
  public boolean contains (Object item) {
      for (Object o : this) {
          if (o == item) {
              return true;
          }
      }
      return false;
  }

  /**
   * get(int)
   * Gives the object in this DList at the given index. If given index
   * is not valid for this DList, throws an exception.
   *
   * @param index The index of the object to return.
   * @return The object at the given index, if valid, in this DList.
   * @exception ArrayIndexOutOfBoundsException Thrown if given index doesn't
   *    exist in this DList.
   */
  public Object get (int index) throws ArrayIndexOutOfBoundsException {
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
   * concat(DList)
   * Concatenates this DList with another DList without mutating the other one.
   * This DList becomes the sum of both DList objects.
   *
   * @param other The DList whose elements are to be added to this DList.
   */
  public void concat (DList other) {
      for (Object o : other) {
          this.add(o);
      }
  }
  
  /**
   * pop()
   * Removes and returns the last object in this DList if there is one,
   * otherwise returns null.
   *
   * @return The last object in this DList if such an object exists, otherwise
   *    null.
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
          last.reset();
          return popped;
      }
  }
  
  /**
   * remove(int)
   * Removes the object at the given index in this DList. If given index is
   * invalid for this DList, throws an exception.
   *
   * @param index The index of the object to remove.
   * @exception ArrayIndexOutOfBoundsException Thrown if given index is invalid.
   */
  public void remove (int index) throws ArrayIndexOutOfBoundsException {
      if ((index < 0) || (index > this.lastIndex)) {
          throw new ArrayIndexOutOfBoundsException(index);
      } else {
          DListNode node = this.head.next;
          for (int k = 0; k < index; k++) {
              node = node.next;
          }
          node.prev.next = node.next;
          node.next.prev = node.prev;
          node.reset();
          this.lastIndex--;
      }
  }

  /**
   * iterator()
   * Gives an iterator over this DList. Enables iteration through DList via a
   * for-each loop:
   *    DList d = new DList();
   *    for (Object o : d) {
   *        // do stuff
   *    }
   *
   * @return An iterator of Object objects over this DList.
   */
  public Iterator<Object> iterator () {
      return new DListIterator(this);
  }
  
  /**
   * toString()
   * Gives the String representation of this DList.
   *
   * @return The String which represents this DList.
   */
  public String toString () {
    String result = "[  ";
    DListNode current = head.next;
    while (current != head) {
      result = result + current.item + "  ";
      current = current.next;
    }
    return result + "]";
  }
}

