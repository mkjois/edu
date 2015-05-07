/* Tree234Node.java */

package dict;

/**
 *  A Tree234Node is a node in a 2-3-4 tree (Tree234 class).
 *
 *  DO NOT CHANGE ANYTHING IN THIS FILE.
 *  You may add helper methods and additional constructors, though.
 **/
class Tree234Node {

  /**
   *  keys is the number of keys in this node.  Always 1, 2, or 3.
   *  key1 through key3 are the keys of this node.  If keys == 1, the value
   *    of key2 doesn't matter.  If keys < 3, the value of key3 doesn't matter.
   *  parent is this node's parent; null if this is the root.
   *  child1 through child4 are the children of this node.  If this is a leaf
   *    node, they must all be set to null.  If this node has no third and/or
   *    fourth child, child3 and/or child4 must be set to null.
   **/
  int keys;
  int key1;
  int key2;
  int key3;
  Tree234Node parent;
  Tree234Node child1;
  Tree234Node child2;
  Tree234Node child3;
  Tree234Node child4;

  Tree234Node(Tree234Node p, int key) {
    keys = 1;
    key1 = key;
    parent = p;
    child1 = null;
    child2 = null;
    child3 = null;
    child4 = null;
  }

  void insertKeyToLeaf (int key) {
      if (this.child1 != null) {
          System.err.println("This node is not a leaf.");
          return;
      } else if (this.keys > 2) {
          System.err.println("This node already has 3 keys.");
          return;
      } else if (this.hasKey(key)) {
          return;
      }
      if (this.keys > 1) {
          if (key < this.key1) {
              this.key3 = this.key2;
              this.key2 = this.key1;
              this.key1 = key;
          } else if (key < this.key2) {
              this.key3 = this.key2;
              this.key2 = key;
          } else {
              this.key3 = key;
          }
      } else {
          if (key < this.key1) {
              this.key2 = this.key1;
              this.key1 = key;
          } else {
              this.key2 = key;
          }
      }
      this.keys++;
  }

  boolean hasKey (int key) {
      if (this.key1 == key) {
          return true;
      } else if (this.keys > 1 && this.key2 == key) {
          return true;
      } else if (this.keys > 2 && this.key3 == key) {
          return true;
      } else {
          return false;
      }
  }
  
  // assumes key is not in node
  Tree234Node getNextNode (int key) {
      if (this.keys == 1) {
          if (key < this.key1) {
              return this.child1;
          } else {
              return this.child2;
          }
      } else if (this.keys == 2) {
          if (key < this.key1) {
              return this.child1;
          } else if (key < this.key2) {
              return this.child2;
          } else {
              return this.child3;
          }
      } else {
          if (key < this.key1) {
              return this.child1;
          } else if (key < this.key2) {
              return this.child2;
          } else if (key < this.key3) {
              return this.child3;
          } else {
              return this.child4;
          }
      }
  }
  
  // assumes this node doesn't have 3 keys
  void restructure (int key, Tree234Node split, Tree234Node new1, Tree234Node new2) {
      if (split == this.child1) {
          this.key3 = this.key2;
          this.key2 = this.key1;
          this.key1 = key;
          this.child4 = this.child3;
          this.child3 = this.child2;
          this.child2 = new2;
          this.child1 = new1;
      } else if (split == this.child2) {
          this.key3 = this.key2;
          this.key2 = key;
          this.child4 = this.child3;
          this.child3 = new2;
          this.child2 = new1;
      } else {
          this.key3 = key;
          this.child4 = new2;
          this.child3 = new1;
      }
      this.keys++;
  }

  /**
   *  toString() recursively prints this Tree234Node and its descendants as
   *  a String.  Each node is printed in the form such as (for a 3-key node)
   *
   *      (child1)key1(child2)key2(child3)key3(child4)
   *
   *  where each child is a recursive call to toString, and null children
   *  are printed as a space with no parentheses.  Here's an example.
   *      ((1)7(11 16)22(23)28(37 49))50((60)84(86 95 100))
   *
   *  DO NOT CHANGE THIS METHOD.
   **/
  public String toString() {
    String s = "";

    if (child1 != null) {
      s = "(" + child1.toString() + ")";
    }
    s = s + key1;
    if (child2 != null) {
      s = s + "(" + child2.toString() + ")";
    } else if (keys > 1) {
      s = s + " ";
    }
    if (keys > 1) {
      s = s + key2;
      if (child3 != null) {
        s = s + "(" + child3.toString() + ")";
      } else if (keys > 2) {
        s = s + " ";
      }
    }
    if (keys > 2) {
      s = s + key3;
      if (child4 != null) {
        s = s + "(" + child4.toString() + ")";
      }
    }
    return s;
  }

  /**
   *  printSubtree() recursively prints this Tree234Node and its descendants as
   *  a tree (albeit sideways).
   *
   *  You're welcome to change this method if you like.  It won't be tested.
   **/
  public void printSubtree(int spaces) {
    if (child4 != null) {
      child4.printSubtree(spaces + 5);
    }
    if (keys == 3) {
      for (int i = 0; i < spaces; i++) {
        System.out.print(" ");
      }
      System.out.println(key3);
    }

    if (child3 != null) {
      child3.printSubtree(spaces + 5);
    }
    if (keys > 1) {
      for (int i = 0; i < spaces; i++) {
        System.out.print(" ");
      }
      System.out.println(key2);
    }

    if (child2 != null) {
      child2.printSubtree(spaces + 5);
    }
    for (int i = 0; i < spaces; i++) {
      System.out.print(" ");
    }
    System.out.println(key1);

    if (child1 != null) {
      child1.printSubtree(spaces + 5);
    }
  }
}
