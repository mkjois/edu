public class DListNode {

  public Run run;
  public DListNode prev;
  public DListNode next;

  DListNode() {
    this.run = null;
    this.prev = null;
    this.next = null;
  }

  DListNode(Run r) {
    this.run = r;
    this.prev = null;
    this.next = null;
  }
}
