package list;

public class LockDList extends DList {

    private static boolean isLockNode (DListNode node) {
        return (node instanceof LockDListNode);
    }

    protected DListNode newNode (Object item, DListNode prev, DListNode next) {
        return new LockDListNode(item, prev, next);
    }

    public void lockNode (DListNode node) {
        if (isLockNode(node)) {
            LockDListNode lockNode = (LockDListNode) node;
            lockNode.locked = true;
        } else {
            return;
        }
    }

    public void insertFront (Object item) {
        super.insertFront(item);
        this.head.next = (LockDListNode) this.head.next;
    }

    public void insertBack (Object item) {
        super.insertBack(item);
        this.head.prev = (LockDListNode) this.head.prev;
    }

    public void insertBefore (Object item, DListNode node) {
        super.insertBefore(item, node);
        node.prev = (LockDListNode) node.prev;
    }

    public void insertAfter (Object item, DListNode node) {
        super.insertAfter(item, node);
        node.next = (LockDListNode) node.next;
    }

    public void remove (DListNode node) {
        if (isLockNode(node)) {
            LockDListNode lockNode = (LockDListNode) node;
            if (! lockNode.locked) {
                super.remove(node);
            } else {
                return;
            }
        } else {
            super.remove(node);
        }
    }
    
    // TESTING
    private DListNode[] toArray () {
        DListNode[] array = new DListNode[this.size];
        DListNode node = this.head;
        for (int i = 0; i < this.size; i++) {
            node = node.next;
            array[i] = node;
        }
        return array;
    }

    // TESTING
    public static void main (String[] args) {
        LockDList l = new LockDList();
        boolean headLock = (isLockNode(l.head));
        System.out.println(headLock);
        l.insertBack(3);
        l.insertBack(4);
        l.insertFront(2);
        l.insertFront(1);
        System.out.println(l); // [1 2 3 4]
        for (DListNode n : l.toArray()) {
            System.out.println(isLockNode(n));
        }
        DListNode n;
        n = l.back().prev.prev;
        l.insertAfter(5, n);
        l.insertBefore(6, n);
        l.insertAfter(7, n.next.next.next.next);
        l.insertBefore(8, n.prev.prev.prev.prev);
        System.out.println(l); // [7 1 6 2 5 3 4 8]
        for (DListNode n2 : l.toArray()) {
            System.out.println(isLockNode(n2));
        }
        l.lockNode(n.prev.prev.prev);
        l.lockNode(n);
        l.lockNode(n.next.next.next);
        l.lockNode(l.head.prev);
        l.remove(l.head);
        for (DListNode n3 : l.toArray()) {
            l.remove(n3);
        }
        System.out.println(l); // [7 2 4 8]
    }
}
