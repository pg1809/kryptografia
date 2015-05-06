package pl.kryptografia.rabin.calculation;

/**
 *
 * @author Wojciech Szałapski
 */
public class PrimeSieve {

    private final static int MAX_GENERATED_PRIME = 65000;
    
    private PrimeSieve() {
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
