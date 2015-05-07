package dict;

import java.util.Arrays;

public class PrimeGen {

    public static boolean isPrime (int n) {
        for (int d = 2; d*d <= n; d++) {
            if (n%d == 0) {
                return false;
            }
        }
        return true;
    }

    // Returns all primes from a to b, inclusive
    public static int[] primes (int a, int b) {
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

    public static int nextPrime (int n) {
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
