/* ListSorts.java */

import list.*;

public class ListSorts {

  private final static int SORTSIZE = 1000;

  /**
   *  makeQueueOfQueues() makes a queue of queues, each containing one item
   *  of q.  Upon completion of this method, q is empty.
   *  @param q is a LinkedQueue of objects.
   *  @return a LinkedQueue containing LinkedQueue objects, each of which
   *    contains one object from q.
   **/
  public static LinkedQueue makeQueueOfQueues(LinkedQueue q) {
      try {
          LinkedQueue superQueue = new LinkedQueue(), subQueue;
          while (! q.isEmpty()) {
              subQueue = new LinkedQueue();
              subQueue.enqueue(q.dequeue());
              superQueue.enqueue(subQueue);
          }
          return superQueue;
      } catch (QueueEmptyException e) {
          System.out.println(e);
          return new LinkedQueue();
      }
  }

  /**
   *  mergeSortedQueues() merges two sorted queues into a third.  On completion
   *  of this method, q1 and q2 are empty, and their items have been merged
   *  into the returned queue.
   *  @param q1 is LinkedQueue of Comparable objects, sorted from smallest 
   *    to largest.
   *  @param q2 is LinkedQueue of Comparable objects, sorted from smallest 
   *    to largest.
   *  @return a LinkedQueue containing all the Comparable objects from q1 
   *   and q2 (and nothing else), sorted from smallest to largest.
   **/
  public static LinkedQueue mergeSortedQueues(LinkedQueue q1, LinkedQueue q2) {
      try {
          LinkedQueue merged = new LinkedQueue();
          Comparable c1, c2;
          Object o;
          while ((! q1.isEmpty()) && (! q2.isEmpty())) {
              c1 = (Comparable) q1.front();
              c2 = (Comparable) q2.front();
              if (c1.compareTo(c2) > 0) {
                  o = q2.dequeue();
              } else {
                  o = q1.dequeue();
              }
              merged.enqueue(o);
          }
          merged.append(q1);
          merged.append(q2);
          return merged;
      } catch (QueueEmptyException e) {
          System.out.println(e);
          return new LinkedQueue();
      }
  }

  /**
   *  partition() partitions qIn using the pivot item.  On completion of
   *  this method, qIn is empty, and its items have been moved to qSmall,
   *  qEquals, and qLarge, according to their relationship to the pivot.
   *  @param qIn is a LinkedQueue of Comparable objects.
   *  @param pivot is a Comparable item used for partitioning.
   *  @param qSmall is a LinkedQueue, in which all items less than pivot
   *    will be enqueued.
   *  @param qEquals is a LinkedQueue, in which all items equal to the pivot
   *    will be enqueued.
   *  @param qLarge is a LinkedQueue, in which all items greater than pivot
   *    will be enqueued.  
   **/   
  public static void partition(LinkedQueue qIn, Comparable pivot, 
                               LinkedQueue qSmall, LinkedQueue qEquals, 
                               LinkedQueue qLarge) {
      try {
          Comparable c;
          while (! qIn.isEmpty()) {
              c = (Comparable) qIn.dequeue();
              if (c.compareTo(pivot) < 0) {
                  qSmall.enqueue(c);
              } else if (c.compareTo(pivot) > 0) {
                  qLarge.enqueue(c);
              } else {
                  qEquals.enqueue(c);
              }
          }
      } catch (QueueEmptyException e) {
          System.out.println(e);
      }
  }

  /**
   *  mergeSort() sorts q from smallest to largest using mergesort.
   *  @param q is a LinkedQueue of Comparable objects.
   **/
  public static void mergeSort(LinkedQueue q) {
      if (q.isEmpty()) {
          return;
      }
      try {
          LinkedQueue superQueue = makeQueueOfQueues(q);
          LinkedQueue q1, q2;
          while (superQueue.size() > 1) {
              q1 = (LinkedQueue) superQueue.dequeue();
              q2 = (LinkedQueue) superQueue.dequeue();
              superQueue.enqueue(mergeSortedQueues(q1, q2));
          }
          q1 = (LinkedQueue) superQueue.dequeue();
          q.append(q1);
      } catch (QueueEmptyException e) {
          System.out.println(e);
      }
  }

  /**
   *  quickSort() sorts q from smallest to largest using quicksort.
   *  @param q is a LinkedQueue of Comparable objects.
   **/
  public static void quickSort(LinkedQueue q) {
      if (q.isEmpty()) {
          return;
      }
      int rand = randomIntFrom1(q.size());
      Comparable piv = (Comparable) q.nth(rand);
      LinkedQueue lesser = new LinkedQueue(),
                  equal = new LinkedQueue(),
                  greater = new LinkedQueue();
      partition(q, piv, lesser, equal, greater);
      quickSort(lesser);
      quickSort(greater);
      q.append(lesser);
      q.append(equal);
      q.append(greater);
  }

  private static int randomIntFrom1 (int n) {
      int result = (int) (Math.random() * n);
      if (result < n) {
          result++;
      }
      return result;
  }

  /**
   *  makeRandom() builds a LinkedQueue of the indicated size containing
   *  Integer items.  The items are randomly chosen between 0 and size - 1.
   *  @param size is the size of the resulting LinkedQueue.
   **/
  public static LinkedQueue makeRandom(int size) {
    LinkedQueue q = new LinkedQueue();
    for (int i = 0; i < size; i++) {
      q.enqueue(new Integer((int) (size * Math.random())));
    }
    return q;
  }

  /**
   *  main() performs some tests on mergesort and quicksort.  Feel free to add
   *  more tests of your own to make sure your algorithms works on boundary
   *  cases.  Your test code will not be graded.
   **/
  public static void main(String [] args) {

    LinkedQueue q;
    
    /*
    LinkedQueue q1, q2, q3;
    Comparable c;

    // makeQueueOfQueues()
    System.out.println();
    q = makeRandom(10);
    System.out.println(q);
    q = makeQueueOfQueues(q);
    System.out.println(q);
    q = makeRandom(0);
    System.out.println(q);
    q = makeQueueOfQueues(q);
    System.out.println(q);

    // mergeSortedQueues()
    System.out.println();
    q1 = new LinkedQueue();
    q2 = new LinkedQueue();
    q = mergeSortedQueues(q1, q2);
    System.out.println(q);
    q1 = makeRandom(10);
    System.out.println(q1);
    q = mergeSortedQueues(q1, q2);
    System.out.println(q);
    q2 = makeRandom(10);
    System.out.println(q2);
    q = mergeSortedQueues(q1, q2);
    System.out.println(q);
    q1 = makeRandom(7);
    q2 = makeRandom(7);
    System.out.println(q1);
    System.out.println(q2);
    q = mergeSortedQueues(q1, q2);
    System.out.println(q);
    
    // mergeSort()
    System.out.println();
    for (int i = 0; i < 11; i++) {
        q = makeRandom(i);
        System.out.println(q);
        mergeSort(q);
        System.out.println(q);
    }

    // Part I
    q = makeRandom(10);
    System.out.println(q.toString());
    mergeSort(q);
    System.out.println(q.toString());

    // partition()
    System.out.println();
    q = new LinkedQueue();
    q1 = new LinkedQueue();
    q2 = new LinkedQueue();
    q3 = new LinkedQueue();
    c = (Comparable) new Integer(4);
    System.out.println(q);
    System.out.println(q1);
    System.out.println(q2);
    System.out.println(q3);
    partition(q, c, q1, q2, q3);
    System.out.println(q);
    System.out.println(q1);
    System.out.println(q2);
    System.out.println(q3);
    q = makeRandom(10);
    System.out.println(q);
    partition(q, c, q1, q2, q3);
    System.out.println(q);
    System.out.println(q1);
    System.out.println(q2);
    System.out.println(q3);

    // quickSort()
    System.out.println();
    for (int i = 0; i < 11; i++) {
        q = makeRandom(i);
        System.out.println(q);
        quickSort(q);
        System.out.println(q);
    }

    // Part II
    q = makeRandom(10);
    System.out.println(q.toString());
    quickSort(q);
    System.out.println(q.toString());
    */

    // Part III
    Timer stopWatch = new Timer();
    for (int i = 10; i < 100001; i *= 10) {
        q = makeRandom(i);
        stopWatch.start();
        mergeSort(q);
        stopWatch.stop();
        System.out.println("Mergesort time, " + i + " Integers:  " +
                           stopWatch.elapsed() + " msec.");

        stopWatch.reset();
        q = makeRandom(i);
        stopWatch.start();
        quickSort(q);
        stopWatch.stop();
        System.out.println("Quicksort time, " + i + " Integers:  " +
                           stopWatch.elapsed() + " msec.");
        stopWatch.reset();
    }
  }
}
