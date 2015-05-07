package list;

class DListNode {

  Object item;
  DListNode prev;
  DListNode next;

  DListNode(Object i, DListNode p, DListNode n) {
    this.item = i;
    this.prev = p;
    this.next = n;
  }

  void reset () {
      this.item = null;
      this.prev = null;
      this.next = null;
  }
}
