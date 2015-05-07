package dict;

/**
 * A class that provides some functionality involving prime numbers.
 * Used in our hash table implementation.
 */
class PrimeGen {

    /**
     * isPrime (int) -- boolean
     *
     * Determines whether or not the given number is prime.
     *
     * @param n The number in question.
     * @return True if n is prime, false otherwise.
     */
    static boolean isPrime (int n) {
        for (int d = 2; d*d <= n; d++) {
            if (n%d == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * primes (int, int) -- int[]
     *
     * Returns an integer array of all prime numbers from a to b, inclusive.
     * Uses the Sieve of Eratosthenes algorithm.
     *
     * @param a The lower bound.
     * @param b The upper bound.
     * @return An array, possibly empty, of all prime numbers in the range [a,b]
     */
    static int[] primes (int a, int b) {
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

    /**
     * nextPrime (int) -- int
     *
     * Returns the smallest prime number p such that p is strictly greater than
     * the given number.
     *
     * @param n The number in question.
     * @return The smallest prime number strictly greater than n.
     */
    static int nextPrime (int n) {
        for (int range = 100; true; range += 100) {
            try {
                int[] primes = PrimeGen.primes(n, n + range);
                if (primes[0] == n) {
                    return primes[1];
                } else {
                    return primes[0];
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                continue;
            }
        }
    }
}
