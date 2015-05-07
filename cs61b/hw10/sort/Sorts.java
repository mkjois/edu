/* Sorts.java */

package sort;

public class Sorts {

  /**
   *  Place any final static fields you would like to have here.
   **/

  private static final int HEXDEC = 16,
                           HEXMASK = 0x0000000f,
                           HEXSHIFT = 4, // 4 bits per hex digit
                           MAXHEXDIGITS = 8;

  /**
   *  countingSort() sorts an array of int keys according to the
   *  values of _one_ of the base-16 digits of each key.  "whichDigit"
   *  indicates which digit is the sort key.  A zero means sort on the least
   *  significant (ones) digit; a one means sort on the second least
   *  significant (sixteens) digit; and so on, up to a seven, which means
   *  sort on the most significant digit.
   *  @param key is an array of ints.  Assume no key is negative.
   *  @param whichDigit is a number in 0...7 specifying which base-16 digit
   *    is the sort key.
   *  @return an array of type int, having the same length as "keys"
   *    and containing the same keys sorted according to the chosen digit.
   *
   *    Note:  Return a _newly_ created array.  DO NOT CHANGE THE ARRAY keys.
   **/
  public static int[] countingSort(int[] keys, int whichDigit) {
      if (whichDigit < 0 || whichDigit >= MAXHEXDIGITS) {
          return null;
      }

      int total = 0, temp, i;

      // extract significant digits
      int[] digits = new int[keys.length];
      for (i = 0; i < keys.length; i++) {
          digits[i] = (keys[i] >> (whichDigit * HEXSHIFT)) & HEXMASK;
      }

      // tally digit counts
      int[] counts = new int[HEXDEC];
      for (int j : digits) {
          counts[j]++;
      }

      // make counts array cumulative
      for (i = 0; i < counts.length; i++) {
          temp = counts[i];
          counts[i] = total;
          total += temp;
      }
      
      // move input elements to right buckets
      int[] result = new int[keys.length];
      for (i = 0; i < keys.length; i++) {
          result[counts[digits[i]]] = keys[i];
          counts[digits[i]]++;
      }

      return result;
  }

  /**
   *  radixSort() sorts an array of int keys (using all 32 bits
   *  of each key to determine the ordering).
   *  @param key is an array of ints.  Assume no key is negative.
   *  @return an array of type int, having the same length as "keys"
   *    and containing the same keys in sorted order.
   *
   *    Note:  Return a _newly_ created array.  DO NOT CHANGE THE ARRAY keys.
   **/
  public static int[] radixSort(int[] keys) {
      int[] result = new int[keys.length];
      for (int i = 0; i < keys.length; i++) {
          result[i] = keys[i];
      }
      for (int j = 0; j < MAXHEXDIGITS; j++) {
          result = countingSort(result, j);
      }
      return result;
  }

  /**
   *  yell() prints an array of int keys.  Each key is printed in hexadecimal
   *  (base 16).
   *  @param key is an array of ints.
   **/
  public static void yell(int[] keys) {
    System.out.print("keys are [ ");
    for (int i = 0; i < keys.length; i++) {
      System.out.print(Integer.toString(keys[i], 16) + " ");
    }
    System.out.println("]");
  }

  private static int[] randomArray (int size) {
      int[] result = new int[size];
      for (int i = 0; i < size; i++) {
          result[i] = (int) (Integer.MAX_VALUE * Math.random());
      }
      return result;
  }

  private static boolean checkSort (int[] array) {
      if (array.length < 2) {
          return true;
      }
      for (int i = 0; i < array.length - 1; i++) {
          if (array[i+1] < array[i]) {
              return false;
          }
      }
      return true;
  }

  /**
   *  main() creates and sorts a sample array.
   *  We recommend you add more tests of your own.
   *  Your test code will not be graded.
   **/
  public static void main(String[] args) {
    int[] keys = { Integer.parseInt("60013879", 16),
                   Integer.parseInt("11111119", 16),
                   Integer.parseInt("2c735010", 16),
                   Integer.parseInt("2c732010", 16),
                   Integer.parseInt("7fffffff", 16),
                   Integer.parseInt("4001387c", 16),
                   Integer.parseInt("10111119", 16),
                   Integer.parseInt("529a7385", 16),
                   Integer.parseInt("1e635010", 16),
                   Integer.parseInt("28905879", 16),
                   Integer.parseInt("00011119", 16),
                   Integer.parseInt("00000000", 16),
                   Integer.parseInt("7c725010", 16),
                   Integer.parseInt("1e630010", 16),
                   Integer.parseInt("111111e5", 16),
                   Integer.parseInt("61feed0c", 16),
                   Integer.parseInt("3bba7387", 16),
                   Integer.parseInt("52953fdb", 16),
                   Integer.parseInt("40013879", 16) };
    int i;
    yell(keys);

    /*
    for (i = 0; i < 8; i++) {
        keys = countingSort(keys, i);
        yell(keys);
    }
    */

    keys = radixSort(keys);
    yell(keys);

    boolean correct = true;
    for (i = 0; i < 1000; i++) {
        keys = randomArray(10000);
        keys = radixSort(keys);
        if (! checkSort(keys)) {
            correct = false;
            break;
        }
    }

    if (correct) {
        System.out.println("Good radix sort!");
    } else {
        System.out.println("Faulty radix sort.");
    }
  }
}
