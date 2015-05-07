package pl.kryptografia.rabin.calculation;

import java.util.ArrayList;
import java.util.List;
import pl.kryptografia.rabin.bignum.BigNum;

/**
 *
 * @author Wojciech Szałapski
 */
public class PrimeSieve {

    /**
     * Maximum prime to generate by Eratostenes sieve.
     */
    private final static int MAX_GENERATED_PRIME = 20000;

    private PrimeSieve() {
    }

    /**
     * From given array of big numbers removes all which are divisible by any of
     * given small prime numbers.
     *
     * @param smallPrimes Small primes used to sieve out complex numbers.
     * @param bignums Big numbers to be tested.
     * @return All numbers from given array that has not been qualified as
     * complex.
     */
    public List<BigNum> sieveOutComplexNumbers(int[] smallPrimes, BigNum[] bignums) {
        int count = bignums.length;
        
        // initially we assume all our big numbers are valid candidates for 
        // prime numbers
        boolean[] isPrime = new boolean[count];
        for (int i = 0; i < count; ++i) {
            isPrime[i] = true;
        }
        
        for (int prime : smallPrimes) {
            // create a big number with current prime value
            BigNum boxedPrime = new BigNum(prime);
            
            for (int i = 0; i < count; ++i) {
                // we do not need to check numbers which have already been
                // sieved out
                if (isPrime[i]) {
                    // check if our candidate is divisible by current small
                    // prime number
                    if (bignums[i].isDivisible(boxedPrime)) {
                        isPrime[i] = false;
                    }
                }
            }
        }
        
        // get all numbers which still can be prime
        List<BigNum> result = new ArrayList<>();
        for (int i = 0; i < count; ++i) {
            if (isPrime[i]) {
                result.add(bignums[i]);
            }
        }
        
        return result;
    }

    /**
     * Generates all odd primes not greater than MAX_GENERATED_PRIME constant.
     *
     * @return All generated odd primes in increasing order.
     */
    public int[] generateOddPrimes() {
        // array tab holds all numbers up to MAX_GENERATED_PRIME constant
        boolean tab[] = new boolean[MAX_GENERATED_PRIME + 1];

        // for each number the value in array indicates if it is a prime number
        tab[0] = false;
        tab[1] = false;
        tab[2] = true;

        // initially we assume that every odd number greater than 2 is prime
        // as algorithm iterates we sieve out complex numbers
        for (int i = 3; i <= MAX_GENERATED_PRIME; ++i) {
            tab[i] = ((i % 2) == 1);
        }

        // we only need to iterate up to square root from the maximum number
        // to sieve out all complex numbers
        // in every iteration we increase i by 2 because even numbers for sure
        // are complex
        for (int i = 3; i * i <= MAX_GENERATED_PRIME; i += 2) {
            if (tab[i]) {
                // every multiple of i must be a complex number
                for (int j = i + i; j <= MAX_GENERATED_PRIME; j += i) {
                    tab[j] = false;
                }
            }
        }

        // count all generated all primes to allocate an array of appropriate
        // size
        int primesCount = 0;
        for (int i = 3; i <= MAX_GENERATED_PRIME; ++i) {
            if (tab[i]) {
                ++primesCount;
            }
        }

        // an array for all generated odd primes
        int result[] = new int[primesCount];
        int position = 0;
        for (int i = 3; i <= MAX_GENERATED_PRIME; ++i) {
            if (tab[i]) {
                result[position] = i;
                ++position;
            }
        }

        return result;
    }

    public static PrimeSieve getInstance() {
        return PrimeSieveHolder.INSTANCE;
    }

    private static class PrimeSieveHolder {

        private static final PrimeSieve INSTANCE = new PrimeSieve();
    }
}
