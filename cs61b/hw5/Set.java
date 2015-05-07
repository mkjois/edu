/* Set.java */

import list.*;

/**
 *  A Set is a collection of Comparable elements stored in sorted order.
 *  Duplicate elements are not permitted in a Set.
 **/
public class Set {
  /* Fill in the data fields here. */

  protected List list;

  /**
   * Set ADT invariants:
   *  1)  The Set's elements must be precisely the elements of the List.
   *  2)  The List must always contain Comparable elements, and those elements 
   *      must always be sorted in ascending order.
   *  3)  No two elements in the List may be equal according to compareTo().
   **/

  // Change according to what List type you want to use to implement Set
  protected static List listOfChoice () {
      return new DList();
  }

  /**
   *  Constructs an empty Set. 
   *
   *  Performance:  runs in O(1) time.
   **/
  public Set() { 
      this.list = Set.listOfChoice();
  }

  /**
   *  cardinality() returns the number of elements in this Set.
   *
   *  Performance:  runs in O(1) time.
   **/
  public int cardinality() {
      return this.list.length();
  }

  /**
   *  insert() inserts a Comparable element into this Set.
   *
   *  Sets are maintained in sorted order.  The ordering is specified by the
   *  compareTo() method of the java.lang.Comparable interface.
   *
   *  Performance:  runs in O(this.cardinality()) time.
   **/
  public void insert(Comparable c) {
      ListNode node = this.list.front();
      try {
          while (c.compareTo(node.item()) > 0) {
              if (node == this.list.back()) {
                  node.insertAfter(c);
                  return;
              }
              node = node.next();
          }
          if (c.compareTo(node.item()) == 0) {
              return;
          }
          node.insertBefore(c);
      } catch (InvalidNodeException e) {
          if (this.cardinality() == 0) {
              this.list.insertFront(c);
          }
      }
  }

  /**
   *  union() modifies this Set so that it contains all the elements it
   *  started with, plus all the elements of s.  The Set s is NOT modified.
   *  Make sure that duplicate elements are not created.
   *
   *  Performance:  Must run in O(this.cardinality() + s.cardinality()) time.
   *
   *  Your implementation should NOT copy elements of s or "this", though it
   *  will copy _references_ to the elements of s.  Your implementation will
   *  create new _nodes_ for the elements of s that are added to "this", but
   *  you should reuse the nodes that are already part of "this".
   *
   *  DO NOT MODIFY THE SET s.
   *  DO NOT ATTEMPT TO COPY ELEMENTS; just copy _references_ to them.
   **/
  public void union(Set s) {
      ListNode tNode = this.list.front(), sNode = s.list.front();
      Comparable tItem, sItem;
      try {
          while (true) {
              tItem = (Comparable) tNode.item();
              sItem = (Comparable) sNode.item();
              if (sItem.compareTo(tItem) < 0) {
                  tNode.insertBefore(sItem);
                  sNode = sNode.next();
              } else if (sItem.compareTo(tItem) == 0) {
                  sNode = sNode.next();
                  tNode = tNode.next();
              } else {
                  tNode = tNode.next();
              }
          }
      } catch (InvalidNodeException et) {
          try {
              while (true) {
                  sItem = (Comparable) sNode.item();
                  this.list.insertBack(sItem);
                  sNode = sNode.next();
              }
          } catch (InvalidNodeException es) {
              return;
          }
      }
  }

  /**
   *  intersect() modifies this Set so that it contains the intersection of
   *  its own elements and the elements of s.  The Set s is NOT modified.
   *
   *  Performance:  Must run in O(this.cardinality() + s.cardinality()) time.
   *
   *  Do not construct any new ListNodes during the execution of intersect.
   *  Reuse the nodes of "this" that will be in the intersection.
   *
   *  DO NOT MODIFY THE SET s.
   *  DO NOT CONSTRUCT ANY NEW NODES.
   *  DO NOT ATTEMPT TO COPY ELEMENTS.
   **/
  public void intersect(Set s) {
      ListNode tNode = this.list.front(), sNode = s.list.front(), rmNode;
      Comparable tItem, sItem;
      try {
          while (true) {
              tItem = (Comparable) tNode.item();
              sItem = (Comparable) sNode.item();
              if (sItem.compareTo(tItem) == 0) {
                  sNode = sNode.next();
                  tNode = tNode.next();
              } else if (sItem.compareTo(tItem) > 0) {
                  rmNode = tNode;
                  tNode = tNode.next();
                  rmNode.remove();
              } else {
                  sNode = sNode.next();
              }
          }
      } catch (InvalidNodeException es) {
          try {
              while (true) {
                  rmNode = tNode;
                  tNode = tNode.next();
                  rmNode.remove();
              }
          } catch (InvalidNodeException et) {
              return;
          }
      }
  }

  /**
   *  toString() returns a String representation of this Set.  The String must
   *  have the following format:
   *    {  } for an empty Set.  No spaces before "{" or after "}"; two spaces
   *            between them.
   *    {  1  2  3  } for a Set of three Integer elements.  No spaces before
   *            "{" or after "}"; two spaces before and after each element.
   *            Elements are printed with their own toString method, whatever
   *            that may be.  The elements must appear in sorted order, from
   *            lowest to highest according to the compareTo() method.
   *
   *  WARNING:  THE AUTOGRADER EXPECTS YOU TO PRINT SETS IN _EXACTLY_ THIS
   *            FORMAT, RIGHT UP TO THE TWO SPACES BETWEEN ELEMENTS.  ANY
   *            DEVIATIONS WILL LOSE POINTS.
   **/
  public String toString() {
      String result = "{  ";
      ListNode node = this.list.front();
      try {
          while (true) {
              result += node.item().toString() + "  ";
              node = node.next();
          }
      } catch (InvalidNodeException e) {
      }
      return result + "}";
  }

  public static void main(String[] argv) {
    Set s = new Set();
    s.insert(new Integer(3));
    s.insert(new Integer(4));
    s.insert(new Integer(3));
    System.out.println("Set s = " + s);

    Set s2 = new Set();
    s2.insert(new Integer(4));
    s2.insert(new Integer(5));
    s2.insert(new Integer(5));
    System.out.println("Set s2 = " + s2);

    Set s3 = new Set();
    s3.insert(new Integer(5));
    s3.insert(new Integer(3));
    s3.insert(new Integer(8));
    System.out.println("Set s3 = " + s3);

    s.union(s2);
    System.out.println("After s.union(s2), s = " + s);

    s.intersect(s3);
    System.out.println("After s.intersect(s3), s = " + s);

    System.out.println("s.cardinality() = " + s.cardinality());
    System.out.println();

    System.out.println("TESTING UNION");
    s = new Set();
    s2 = new Set();
    s3 = new Set();
    s.union(s2);
    System.out.println("Two empty sets union: " + s);
    s.insert(new Integer(4));
    s.insert(new Integer(-6));
    s.insert(new Integer(0));
    s.union(s2);
    System.out.println("[ -6 0 4]: " + s);
    s2.insert(new Integer(4));
    s2.insert(new Integer(0));
    s2.insert(new Integer(2));
    s2.insert(new Integer(-5));
    System.out.println("[ -5 0 2 4 ]: " + s2);
    s.union(s2);
    s3.union(s2);
    System.out.println("[ -6 -5 0 2 4 ]: " + s);
    System.out.println("[ -5 0 2 4]: " + s3);
    s3.insert(new Integer(9));
    s3.insert(new Integer(7));
    s3.insert(new Integer(7));
    System.out.println("[ -5 0 2 4 7 9]: " + s3);
    s.union(s3);
    System.out.println("[ -6 -5 0 2 4 7 9 ]: " + s);
    System.out.println();

    System.out.println("TESTING INTERSECT");
    s = new Set();
    s2 = new Set();
    s3 = new Set();
    s.intersect(s2);
    System.out.println("Two empty sets intersect: " + s);
    s.insert(new Integer(2));
    s.insert(new Integer(-8));
    s.insert(new Integer(-6));
    s2.intersect(s);
    System.out.println("empty set: " + s2);
    System.out.println("[ -8 -6 2 ]: " + s);
    s.intersect(s3);
    System.out.println("empty set: " + s);
    s.insert(new Integer(2));
    s.insert(new Integer(-8));
    s.insert(new Integer(-6));
    s2.insert(new Integer(5));
    s2.insert(new Integer(-8));
    s2.insert(new Integer(-3));
    s.intersect(s2);
    System.out.println("[ -8 ]: " + s);
    s.insert(new Integer(5));
    s.insert(new Integer(-3));
    s2.intersect(s);
    System.out.println("[ -8 -3 5 ]: " + s2);
    s2.insert(new Integer(-5));
    s2.insert(new Integer(-7));
    s3.insert(new Integer(4));
    s3.insert(new Integer(0));
    s3.insert(new Integer(-3));
    s3.insert(new Integer(5));
    s3.insert(new Integer(-8));
    s3.intersect(s2);
    System.out.println("[ -8 -3 5 ]: " + s3);
    System.out.println();
  }
}
