package pl.kryptografia.rabin.calculation;

import pl.kryptografia.rabin.bignum.BigNum;

public class PrimeGenerator {

    /**
     * Number of iterations of Miller-Rabin test.
     */
    private final static int MILLER_RABIN_CONSTANT = 15;

    /**
     * Private constructor for singleton pattern purpose.
     */
    private PrimeGenerator() {
    }

    /**
     * For given big odd number returns the closest prime which is not greater
     * and gives the same remainder modulo 4.
     *
     * @param x Initial number.
     * @return The greatest prime which is less than or equal to given number.
     */
    public BigNum findNotGreaterPrimeModulo4(BigNum x) {
        // get a copy of the original number
        BigNum a = new BigNum(x);

        BigNum four = new BigNum();
        four.setBit(BigNum.BITS - 3, 1);

        // subtract 4 until you find a prime number
        while (!isPrime(a, MILLER_RABIN_CONSTANT)) {
            a.subtract(four);
        }

        return a;
    }

    /**
     * Checks if given odd number is prime using Miller-Rabin test.
     *
     * This method assumes that the initial value x is generated in the
     * following way:
     * <code>
     * BigNum x = new BigNum();
     * x.randomize(BigNum.BLOCKS / 4);
     * </code>
     *
     * There is a chance of at most 4^(-accuracy) that a complex
     * number is considered prime.
     *
     * @param x Big odd number to test.
     * @param accuracy Determines the chance of test to say that a complex
     * number is prime.
     * @return True if given number is probably prime.
     */
    public boolean isPrime(BigNum x, int accuracy) {
        // one is not a prime number and test does not work for it
        if (x.equals(BigNum.ONE)) {
            return false;
        }

        BigNum d = new BigNum(x);
        d.setBit(BigNum.BITS - 1, 0);
        // d is the original number minus one (so it is for sure an even number)

        BigNum xMinusOne = new BigNum(d);

        // make d * 2^s = x - 1 (find maximum s)
        int s = 0;
        while (d.getBit(BigNum.BITS - 1) == 0) {
            d.shiftRight(1);
            ++s;
        }

        // we get random numbers by randomizing some blocks (half of the blocks
        // - see method description)
        // however x can have leading zeros so we count it not to put ones 
        // there in our random numbers because they need to be less than x
        int xZeroBits = 0;
        while (x.getBit(BigNum.BITS / 2 + xZeroBits) == 0) {
            ++xZeroBits;
        }

        for (int iteration = 0; iteration < accuracy; ++iteration) {
            // get random a from the range [1, x - 1]
            BigNum a = new BigNum();
            do {
                a.randomize(BigNum.BLOCKS / 2);
                // put leading zeros if x has leading zeros
                for (int i = 0; i < xZeroBits; ++i) {
                    a.setBit(BigNum.BITS / 2 + i, 0);
                }
            } while (a.absGreaterOrEqualTo(x) || a.equals(BigNum.ZERO));
            // now we have a from the range [1, x - 1]

            a.powerModulo(d, x);
            if (!a.equals(BigNum.ONE)) {
                boolean ok = true;
                for (int r = 0; r < s && ok; ++r) {
                    if (a.equals(xMinusOne)) {
                        ok = false;
                    } else {
                        a.multiply(a);
                        a.modulo(x);
                    }
                }

                if (ok) {
                    return false;
                }
            }
        }

        return true;
    }

    public static PrimeGenerator getInstance() {
        return PrimeGeneratorHolder.INSTANCE;
    }

    private static class PrimeGeneratorHolder {

        private static final PrimeGenerator INSTANCE = new PrimeGenerator();
    }
}
