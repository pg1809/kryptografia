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
    public static final int BLOCK_SIZE = 32;

    /**
     * Number of 32 bit blocks.
     *
     * 8 blocks = 256 bits
     */
    public static final int BLOCKS = 8;

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
     * Creates a big number identical to given big number.
     *
     * @param pattern Big number to be duplicated.
     */
    public BigNum(BigNum pattern) {
        fillFromBinaryRepresentation(pattern.binaryRepresentation());
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
     * Decreases a big number by given value (works only if the result is
     * non-negative.
     *
     * @param x Value to subtract from this number.
     */
    public void subtract(BigNum x) {
        byte[] minuend = binaryRepresentation();
        byte[] subtrahent = x.binaryRepresentation();
        byte[] result = new byte[minuend.length];

        byte borrowed = 0;
        for (int i = minuend.length - 1; i >= 0; --i) {
            minuend[i] -= borrowed;
            minuend[i] -= subtrahent[i];

            switch (minuend[i]) {
                case -2:
                    result[i] = 0;
                    borrowed = 1;
                    break;
                case -1:
                    result[i] = 1;
                    borrowed = 1;
                    break;
                default:
                    result[i] = minuend[i];
                    borrowed = 0;
                    break;
            }
        }

        fillFromBinaryRepresentation(result);
    }

    /**
     * Divides this number modulo given big number.
     *
     * @param modulus Modulus.
     */
    public void modulo(BigNum modulus) {
        BigNum x = new BigNum(modulus);

        while (greaterOrEqualTo(x)) {
            int shift = findMaximumLeftShift(x);
            x.shiftLeft(shift);

            subtract(x);
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
     * Returns bit value on given position.
     *
     * @param position Bit position (numbered from 0).
     * @return Bit value.
     */
    private byte getBit(int position) {
        int block = position / 32;
        int positionInBlock = position % 32;

        if ((number[block] & (1 << (BLOCK_SIZE - positionInBlock - 1))) != 0) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Finds maximum left shift of x so that it does not exceed this number.
     *
     * @param x The number to be shifted.
     * @return Maximum shift (or -1 if x is greater than this number).
     */
    private int findMaximumLeftShift(BigNum x) {
        int shift = -1;

        BigNum xCopy = new BigNum(x);

        while (greaterOrEqualTo(xCopy)) {
            ++shift;

            if (xCopy.getBit(0) == 1) {
                return shift;
            }
            xCopy.shiftLeft(1);
        }

        return shift;
    }

    /**
     * Shifts this number left by given number of bits.
     *
     * @param bias Number of bits by which this number should be shifted.
     */
    private void shiftLeft(int bias) {
        byte[] binaryRepresentation = binaryRepresentation();

        for (int i = 0; i < binaryRepresentation.length - bias; ++i) {
            binaryRepresentation[i] = binaryRepresentation[i + bias];
        }

        for (int i = binaryRepresentation.length - bias; i < binaryRepresentation.length; ++i) {
            binaryRepresentation[i] = 0;
        }

        fillFromBinaryRepresentation(binaryRepresentation);
    }

    /**
     * Fills this number content with given binary representation.
     *
     * @param binaryRepresentation Binary representation of the BigNum.
     */
    private void fillFromBinaryRepresentation(byte[] binaryRepresentation) {
        for (int i = 0; i < binaryRepresentation.length; ++i) {
            setBit(i, binaryRepresentation[i]);
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

    /**
     * Returns a binary representation of BigNum as an array of bytes.
     *
     * @return Binary representation of BigNum in the form of an array of bytes.
     */
    private byte[] binaryRepresentation() {
        byte[] result = new byte[BLOCKS * BLOCK_SIZE];

        int counter = 0;
        for (int i = 0; i < BLOCKS; ++i) {
            for (int j = BLOCK_SIZE - 1; j >= 0; --j) {
                if ((number[i] & (1 << j)) != 0) {
                    result[counter++] = 1;
                } else {
                    result[counter++] = 0;
                }
            }
        }

        return result;
    }

    /**
     * Checks if this number is greater or equal to given number.
     *
     * @param x Number to compare with.
     * @return True if and only if this number is not less than given number.
     */
    private boolean greaterOrEqualTo(BigNum x) {
        byte[] me = binaryRepresentation();
        byte[] other = x.binaryRepresentation();

        for (int i = 0; i < me.length; ++i) {
            if (me[i] < other[i]) {
                return false;
            } else if (me[i] > other[i]) {
                return true;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        byte[] binaryRepresentation = binaryRepresentation();
        for (byte b : binaryRepresentation) {
            builder.append(b);
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
        return Arrays.equals(this.number, other.number);
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

    /**
     * Replaces a block with given value.
     *
     * @param blockNumber Number of block to replace (numbered from 0).
     * @param value New value of the block.
     */
    public void replaceBlock(int blockNumber, long value) {
        number[blockNumber] = value;
    }
}
