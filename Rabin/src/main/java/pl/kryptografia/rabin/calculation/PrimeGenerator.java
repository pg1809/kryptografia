package pl.kryptografia.rabin.calculation;

import java.util.Random;
import pl.kryptografia.rabin.bignum.BigNum;

/**
 *
 * @author Wojciech Szałapski
 */
public class PrimeGenerator {

    /**
     * Number of iterations of Miller-Rabin test.
     */
    private final static int MILLER_RABIN_CONSTANT = 50;

    /**
     * Random numbers generator.
     */
    private Random generator = new Random();

    private PrimeGenerator() {
    }

    /**
     * For given big number returns the closest prime which is not greater and
     * gives the same remainder modulo 4.
     *
     * @param x Initial number.
     * @return The greatest prime which is less than or equal to given number.
     */
    public BigNum findNotGreaterPrimeModulo4(BigNum x) {
        BigNum a = new BigNum(x);

        BigNum four = new BigNum();
        four.setBit(BigNum.BITS - 3, 1);

        while (!isPrime(a)) {
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
     * x.randomize(BigNum.BLOCKS / 2);
     * </code>
     *
     * There is a chance of 4^(-MILLER_RABIN_CONSTANT) that a complex number is
     * considered prime.
     *
     * @param x Big number to test.
     * @return True if given number is probably prime.
     */
    private boolean isPrime(BigNum x) {
        BigNum d = new BigNum(x);
        d.setBit(BigNum.BITS - 1, 0);

        BigNum xMinusOne = new BigNum(d);

        int s = 0;
        while (d.getBit(BigNum.BITS - 1) == 0) {
            d.shiftRight(1);
            ++s;
        }

        int xZeroBits = 0;
        while (x.getBit(BigNum.BITS / 2 + xZeroBits) == 0) {
            ++xZeroBits;
        }

        for (int iteration = 0; iteration < MILLER_RABIN_CONSTANT; ++iteration) {
            BigNum a = new BigNum();
            do {
                a.randomize(BigNum.BLOCKS / 2);
                for (int i = 0; i < xZeroBits; ++i) {
                    a.setBit(BigNum.BITS / 2 + i, 0);
                }
            } while (a.absGreaterOrEqualTo(x) || a.equals(BigNum.ZERO));

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
