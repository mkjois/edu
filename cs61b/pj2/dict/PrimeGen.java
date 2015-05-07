package dict;

import java.util.Arrays;

/**
 * Class: PrimeGen
 * Author: Manny Jois
 * Date: March 2013
 *
 * A few useful prime number related functions, all static methods
 */
public class PrimeGen {

    // Try not to use on negative numbers
    public static boolean isPrime (int n) {
        if (n < 2) { return false; }
        for (int d = 2; d*d <= n; d++) {
            if (n%d == 0) {
                return false;
            }
        }
        return true;
    }

    // Returns all primes from a to b, inclusive (empty array if none)
    // Uses the Sieve of Eratosthenes
    // Try not to ask for a huge range
    public static int[] primes (int a, int b) {
        if (b < 2) { return new int[] {}; }
        if (a < 2) { a = 2; }
        boolean[] isNotPrime = new boolean[b-a+1];
        for (int d = 2; d*d <= b; d++) {
            for (int i = 0; i < isNotPrime.length; i++) {
                if (i+a == d) {
                    continue;
                } else if ((i+a)%d == 0 || i+a == 1) {
                    isNotPrime[i] = true; // a+i is not prime
                }
            }
        }
        int numPrimes = 0;
        for (boolean bool : isNotPrime) {
            if (!bool) {
                numPrimes++; // counts # of primes in range [a,b]
            }
        }
        int[] primes = new int[numPrimes];
        int index = 0;
        for (int j = 0; j < isNotPrime.length; j++) {
            if (!isNotPrime[j]) {
                primes[index] = a+j;
                index++;
            }
        }
        return primes;
    }

    // Returns the smallest prime p such that p > n (even if n is prime)
    public static int nextPrime (int n) {
        if (n < 2) { return 2; }
        for (int start = n+1, range = 2; true; start += range, range *= 2) {
          int[] primes = PrimeGen.primes(start, start + range);
          if (primes.length > 0) {
            return primes[0];
          }
        }
    }

    private static void assertTrue(boolean condition, String message) {
      if (message == null) { message = "Assertion error"; }
      if (! condition) { throw new AssertionError(message); }
    }

    // Tests
    public static void main(String[] args) {
      assertTrue(! isPrime(0), null);
      assertTrue(! isPrime(1), null);
      assertTrue(  isPrime(2), null);
      assertTrue(  isPrime(3), null);
      assertTrue(! isPrime(4), null);
      assertTrue(  isPrime(5), null);
      assertTrue(! isPrime(6), null);
      assertTrue(  isPrime(7), null);
      assertTrue(! isPrime(8), null);
      assertTrue(! isPrime(9), null);
      assertTrue(! isPrime(10), null);
      assertTrue(nextPrime(0) == 2, null);
      assertTrue(nextPrime(1) == 2, null);
      assertTrue(nextPrime(2) == 3, null);
      assertTrue(nextPrime(3) == 5, null);
      assertTrue(nextPrime(4) == 5, null);
      assertTrue(nextPrime(5) == 7, null);
      assertTrue(nextPrime(6) == 7, null);
      assertTrue(nextPrime(7) == 11, null);
      assertTrue(nextPrime(8) == 11, null);
      assertTrue(nextPrime(9) == 11, null);
      assertTrue(nextPrime(10) == 11, null);
      assertTrue(nextPrime(122164747) == 122164969, null);
      assertTrue(Arrays.equals(primes(0, 1), new int[] {}), null);
      assertTrue(Arrays.equals(primes(0, 2), new int[] {2}), null);
      assertTrue(Arrays.equals(primes(1, 3), new int[] {2, 3}), null);
      assertTrue(Arrays.equals(primes(2, 10), new int[] {2, 3, 5, 7}), null);
      assertTrue(Arrays.equals(primes(3, 10), new int[] {3, 5, 7}), null);
      assertTrue(Arrays.equals(primes(4, 10), new int[] {5, 7}), null);
      assertTrue(Arrays.equals(primes(4, 11), new int[] {5, 7, 11}), null);
      System.out.print("one million primes() time test...");
      primes(0, 1000000);
      System.out.print("done.\n");
    }
}
