package pl.kryptografia.rabin.bignum;

import java.security.SecureRandom;
import java.util.Arrays;

/**
 *
 * @author Wojciech Szałapski
 */
public class BigNum {

    /**
     * Block size in bits.
     */
    private static final int BLOCK_SIZE = 32;

    /**
     * Number of 32 bit blocks.
     *
     * 16 blocks = 512 bits
     */
    private static final int BLOCKS = 16;

    /**
     * Binary representation of the number.
     *
     * Each block consists of 64 bits but only least significant 32 of each are
     * relevant.
     */
    private final long[] number = new long[BLOCKS];

    /**
     * Random bits generator.
     */
    private final SecureRandom generator = new SecureRandom();

    /**
     * Creates a big number equal to 0.
     */
    public BigNum() {
        fillWithZeros();
    }

    /**
     * Creates a big number with initial 64 bits on given positions.
     *
     * @param initialValue Initial 64 bits.
     * @param firstBlock Number of the first block of initial value.
     */
    public BigNum(long initialValue, int firstBlock) {
        number[firstBlock] = (initialValue >>> 32);
        number[firstBlock + 1] = extractLastBits(initialValue);
    }

    /**
     * Multiplies two numbers with half a maximum least significant bits.
     *
     * @param a Multiplicand.
     * @param b Multiplier.
     * @return Result of the multiplication.
     */
    public static BigNum multiply(BigNum a, BigNum b) {
        BigNum result = new BigNum();

        for (int i = BLOCKS / 2; i < BLOCKS; ++i) {
            for (int j = BLOCKS / 2; j < BLOCKS; ++j) {
                long product = a.number[i] * b.number[j];
                int position = i + j - BLOCKS;

                BigNum c = new BigNum(product, position);
                result.add(c);
            }
        }

        return result;
    }

    /**
     * Increases a big number by given value.
     *
     * @param x Value to add to this number.
     */
    public void add(BigNum x) {
        long sum = 0;
        for (int i = BLOCKS - 1; i >= 0; --i) {
            sum += number[i] + x.number[i];
            number[i] = extractLastBits(sum);
            sum >>>= 32;
        }
    }

    /**
     * Fills given number of least significant blocks with random bits and the
     * rest of blocks with zeros.
     *
     * @param n Number of least significant blocks to fill with random bits.
     */
    public void randomize(int n) {
        for (int i = 0; i < BLOCKS - n; ++i) {
            number[i] = 0;
        }
        for (int i = BLOCKS - n; i < BLOCKS; ++i) {
            number[i] = extractLastBits(generator.nextLong());
        }
    }

    /**
     * Sets bit on given position.
     *
     * @param position Position of the bit (numbered from 0).
     * @param value New value of the bit.
     */
    public void setBit(int position, int value) {
        int block = position / 32;
        int positionInBlock = position % 32;
        
        if (value == 1) {
            number[block] |= (1 << (BLOCK_SIZE - positionInBlock - 1));
        } else if (value == 0) {
            number[block] &= ~(1 << (BLOCK_SIZE - positionInBlock - 1));
        }
    }

    /**
     * Fills all bit with zeros.
     */
    private void fillWithZeros() {
        for (int i = 0; i < BLOCKS; ++i) {
            number[i] = 0;
        }
    }

    /**
     * Extracts last 32 bits from long integer.
     *
     * @param x Original number.
     * @return Last 32 bits of the number.
     */
    private long extractLastBits(long x) {
        return (x << 32) >>> 32;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < BLOCKS; ++i) {
            for (int j = BLOCK_SIZE - 1; j >= 0; --j) {
                if ((number[i] & (1 << j)) != 0) {
                    builder.append('1');
                } else {
                    builder.append('0');
                }
            }
        }

        return builder.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Arrays.hashCode(this.number);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BigNum other = (BigNum) obj;
        if (!Arrays.equals(this.number, other.number)) {
            return false;
        }
        return true;
    }

    /**
     * Returns block of given number (from 0 to BLOCKS - 1).
     *
     * @param blockNumber Number of the block.
     * @return Block with given number.
     */
    public long getBlock(int blockNumber) {
        return number[blockNumber];
    }
}
