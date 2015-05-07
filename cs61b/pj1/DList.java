import java.util.*;

public class DList implements Iterable<Run> {

  public final static int DLISTINDEX = 0;
  public final static int RUNINDEX = 1;
  public final static int INDEXOUTOFBOUNDS = -1;

  private DListNode nodeMarker;
  public DListNode head;
  public int size;

  public DList() {
    this.head = new DListNode();
    this.head.run = null;
    this.head.next = this.head;
    this.head.prev = this.head;
    this.nodeMarker = this.head;
    this.size = 0;
  }

  public DList(Run r) {
    this.head = new DListNode();
    this.head.run = null;
    this.head.next = new DListNode();
    this.head.next.run = r;
    this.head.prev = this.head.next;
    this.head.next.prev = this.head;
    this.head.prev.next = this.head;
    this.nodeMarker = this.head;
    this.size = 1;
  }

  public void insertEnd(Run r) {
      DListNode end = new DListNode(r);
      end.prev = this.head.prev;
      end.next = this.head;
      this.head.prev = end;
      end.prev.next = end;
      this.size++;
  }

  public void removeEnd() {
      assert this.size > 0 : "Nothing to remove from Dlist.";
      if (this.nodeMarker.next.equals(this.head)) {
          this.nodeMarker = this.head.prev.prev;
      }
      this.head.prev = this.head.prev.prev;
      this.head.prev.next = this.head;
      this.size--;
  }

  public void insert (int index, Run r) {
      assert index < this.size : "DList index out of bounds.";
      DListNode afterNode = this.head.next, newNode = new DListNode(r);
      for (int j = 0; j < index; j++) {
          afterNode = afterNode.next;
      }
      afterNode.prev.next = newNode;
      newNode.prev = afterNode.prev;
      afterNode.prev = newNode;
      newNode.next = afterNode;
      this.size++;
  }

  private void linkNodes (DListNode[] nodes) {
      assert nodes.length > 1 : "Can't merge less than 2 nodes.";
      int i;
      nodes[0].next = nodes[1];
      for (i = 1; i < nodes.length - 1; i++) {
          nodes[i].prev = nodes[i-1];
          nodes[i].next = nodes[i+1];
      }
      nodes[i].prev = nodes[i-1];

      for (DListNode node : nodes) {
          if ((! node.equals(this.head)) && node.run.isEmptyLength()) {
              node.prev.next = node.next;
              node.next.prev = node.prev;
          }
      }
  }

  private void mergeNodes (DListNode start, DListNode end) {
      if (start.equals(this.head)) {
          this.mergeNodes(start.next, end);
      } else if (end.equals(this.head)) {
          this.mergeNodes(start, end.prev);
      } else if (start.equals(end)) {
          return;
      } else {
          boolean noNulls;
          do {
              noNulls = (! start.equals(this.head)) &&
                        (! start.next.equals(this.head)); 
              if (noNulls && start.run.equalParams(start.next.run)) {
                  Run combo = new Run(start.run.length + start.next.run.length,
                                      start.run.value,
                                      start.run.hunger);
                  start.run = combo;
                  start.next = start.next.next;
                  start.next.prev = start;
                  end = end.next;
              } else {
                  start = start.next;
              }
          } while (! start.equals(end));
      }
  }

  public void deepReplace (int dIndex, int rIndex, Run r) {
      assert dIndex < this.size : "DList index out of bounds.";
      DListNode node = this.head.next;
      for (int j = 0; j < dIndex; j++) {
          node = node.next;
      }
      Run orig = node.run;
      
      assert rIndex < orig.length : "Run index out of bounds.";
      DListNode leftNode = new DListNode(
              new Run(rIndex, orig.value, orig.hunger));
      DListNode rightNode = new DListNode(
              new Run(orig.length - 1 - rIndex, orig.value, orig.hunger));
      DListNode centerNode = new DListNode(r);
      DListNode[] nodesToLink =
              {node.prev, leftNode, centerNode, rightNode, node.next};
      this.linkNodes(nodesToLink);
      this.mergeNodes(node.prev, node.next);
  }

  public void remove (int index) {
      assert index < this.size : "DList index out of bounds.";
      DListNode trashNode = this.head.next;
      for (int j = 0; j < index; j++) {
          trashNode = trashNode.next;
      }
      if (this.nodeMarker.equals(trashNode)) {
          this.nodeMarker = trashNode.prev;
      }
      trashNode.prev.next = trashNode.next;
      trashNode.next.prev = trashNode.prev;
      this.size--;
  }

  public Run getRun (int index) {
      assert index < this.size : "DList index out of bounds.";
      DListNode node = this.head.next;
      for (int j = 0; j < index; j++) {
          node = node.next;
      }
      return node.run;
  }

  public void restartRuns () {
      this.nodeMarker = this.head;
  }

  public Run nextRun () {
      if (this.nodeMarker.next.equals(this.head)) {
          return null;
      } else {
          this.nodeMarker = this.nodeMarker.next;
          return this.nodeMarker.run;
      }
  }

  public DList (int[] values, int[] hungers) {
      this();
      Run curr = new Run(1, values[0], hungers[0]);
      for (int i = 1; i < values.length; i++) {
          if (curr.equalParams(values[i], hungers[i])) {
              curr.length++;
          } else {
              this.insertEnd(curr);
              curr = new Run(1, values[i], hungers[i]);
          }
      }
      this.insertEnd(curr);
  }

  private static Cell[] concatCellArrays (Cell[] a1, Cell[] a2) {
      if (a1 == null) {
          return a2;
      } else if (a2 == null) {
          return a1;
      }
      Cell[] result = new Cell[a1.length + a2.length];
      for (int i = 0; i < a1.length; i++) {
          result[i] = a1[i];
      }
      for (int i = 0; i < a2.length; i++) {
          result[i + a1.length] = a2[i];
      }
      return result;
  }

  private Run[] toRunArray () {
      if (this.size > 0) {
          Run[] result = new Run[this.size];
          DListNode node = this.head;
          for (int i = 0; i < this.size; i++) {
              node = node.next;
              result[i] = node.run;
          }
          return result;
      } else {
          return null;
      }
  }

  public Cell[] toCellArray () {
      Cell[] result = new Cell[0];
      Run[] runArray = this.toRunArray();
      for (Run r : runArray) {
          result = DList.concatCellArrays(result, r.toCellArray());
      }
      return result;  
  }

  public String toString() {
    String result = "[  ";
    DListNode current = this.head.next;
    while (current != this.head) {
      result = result + current.run.toString() + "  ";
      current = current.next;
    }
    return result + "]";
  }

  class DListIterator implements Iterator<Run> {

      private DList dlist;
      private DListNode node;

      public DListIterator (DList d) {
          this.dlist = d;
          this.node = d.head;
      }

      public boolean hasNext () {
          return (! node.next.equals(dlist.head));
      }

      public Run next () {
        if (this.hasNext()) {
            node = node.next;
            return node.run;
        } else {
            throw new NoSuchElementException();
        }
      }

      public void remove () {
      }
  }

  public Iterator<Run> iterator () {
      return new DListIterator(this);
  }


  public int[] indicesOf (int rawIndex) {
      int runLength, total = 0, dlistIndex = 0;
      int[] result = new int[2];
      for (Run run : this) {
          runLength = run.length;
          total += runLength;
          if (total > rawIndex) {
              result[DList.DLISTINDEX] = dlistIndex;
              result[DList.RUNINDEX] = rawIndex - total + runLength;
              return result;
          } else {
              dlistIndex++;
          }
      }
      int[] error = {DList.INDEXOUTOFBOUNDS, DList.INDEXOUTOFBOUNDS};
      return error;
  }

  // FOR TESTING
  public static void main (String[] args) {
      DList dlist = new DList();
      dlist.insertEnd(new Run(3,0,-1));
      dlist.insertEnd(new Run(5,1,2));
      dlist.insertEnd(new Run(10,2,-1));
      System.out.println(dlist);
      dlist.insert(2, new Run(7,1,1));
      System.out.println(dlist);
      dlist.remove(1);
      System.out.println(dlist);
      System.out.println("indices of raw index 8 are [1, 5]: " +
                         Arrays.toString(dlist.indicesOf(8)));
      System.out.println("indices of raw index 19 are [2, 9]: " +
                         Arrays.toString(dlist.indicesOf(19)));
      dlist.deepReplace(1, 5, new Run(1, 0, -1));
      System.out.println(dlist);
      dlist.deepReplace(4, 9, new Run(1, 1, 2));
      System.out.println(dlist);
      dlist.deepReplace(3, 0, new Run(6, 2, -1));
      System.out.println(dlist);
      dlist.deepReplace(0, 0, new Run(0, 2, -1));
      System.out.println(dlist);
      dlist.deepReplace(0, 0, new Run(12, 1, 1));
      System.out.println(dlist);
      dlist.deepReplace(1, 0, new Run(1, 1, 1));
      System.out.println(dlist);
      dlist.deepReplace(3, 0, new Run(1, 2, -1));
      System.out.println(dlist);

  }
}
