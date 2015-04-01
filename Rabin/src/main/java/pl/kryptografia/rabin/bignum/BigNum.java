package pl.kryptografia.rabin.bignum;

import java.security.SecureRandom;
import java.util.Arrays;

public class BigNum {

    /**
     * Block size in bits.
     */
    public static final int BLOCK_SIZE = 32;

    /**
     * Number of 32 bit blocks.
     *
     * 4 blocks = 128 bits
     * 8 blocks = 256 bits
     * 16 blocks = 512 bits
     */
    public static final int BLOCKS = 4;

    /**
     * Number of bits in one BigNum.
     */
    public static final int BITS = BLOCKS * BLOCK_SIZE;

    /**
     * BigNum representing 0.
     */
    public static final BigNum ZERO = new BigNum();

    /**
     * BigNum representing 1.
     */
    public static final BigNum ONE = new BigNum(1, BLOCKS - 2);

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
     * Number's sign (+1 or -1).
     */
    private int sign = 1;

    /**
     * Creates a big number equal to 0.
     */
    public BigNum() {
        fillWithZeros();
    }

    /**
     * Creates a big number with initial 64 bits on given positions.
     *
     * 64 bits need to be split into two blocks because the number is
     * represented only by least significant 32 bits in each block.
     *
     * @param initialValue Initial 64 bits.
     * @param firstBlock Number of the first block of initial value.
     */
    public BigNum(long initialValue, int firstBlock) {
        number[firstBlock + 1] = extractLast32Bits(initialValue);
        number[firstBlock] = (initialValue >>> 32);
    }

    /**
     * Creates a big number identical to given big number.
     *
     * @param pattern Big number to be duplicated.
     */
    public BigNum(BigNum pattern) {
        sign = pattern.sign;
        fillFromBinaryRepresentation(pattern.binaryRepresentation());
    }

    /**
     * Multiplies two big numbers with half a maximum least significant bits.
     *
     * The above constraint is equivalent to the following: each multiplied
     * number has to have only zeros on first half positions. It guarantees that
     * the result fits into the format. If there are any ones where should be
     * zeros, these ones are ignored.
     *
     * @param x Multiplier.
     */
    public void multiply(BigNum x) {
        BigNum result = new BigNum();

        // multiply each block from this number by each block from number x
        // only non-zero blocks are considered (see method description)
        for (int i = BLOCKS / 2; i < BLOCKS; ++i) {
            for (int j = BLOCKS / 2; j < BLOCKS; ++j) {
                long product = number[i] * x.number[j];
                // when two blocks are multiplied their positions need to be 
                // considered to shift the result left properly
                int position = i + j - BLOCKS;

                // add the result of this multiplication to the global result
                BigNum y = new BigNum(product, position);
                result.add(y);
            }
        }

        fillFromBinaryRepresentation(result.binaryRepresentation());
        sign = sign * x.sign;
    }

    /**
     * Adds given value to this big number.
     *
     * @param x Value to add to this number.
     */
    public void add(BigNum x) {
        if (sign == x.sign) {
            // when both numbers are positive or negative it is just a simple 
            // bitwise sum
            long sum = 0;
            for (int i = BLOCKS - 1; i >= 0; --i) {
                sum += number[i] + x.number[i];
                // put least significant 32 bits into the result
                number[i] = extractLast32Bits(sum);
                // the result may overflow 32 bits so the next result is
                // calculated using the rest of the sum
                sum >>>= 32;
            }
        } else {
            // when one number is positive and the other is negative we need to
            // constitute which one is greater as per its absolute value
            BigNum a = new BigNum(this);
            BigNum b = new BigNum(x);
            boolean thisIsGreater = true;
            if (!absGreaterOrEqualTo(x)) {
                a = new BigNum(x);
                b = new BigNum(this);
                thisIsGreater = false;
            }
            // now we are sure that |a| >= |b|

            // we subtract absolute values of the numbers
            a.absSubtract(b);
            // absolute value of the result is equal to absolute value of above
            // subtraction
            fillFromBinaryRepresentation(a.binaryRepresentation());

            // now we need to check if the result is positive or negative
            // |this| >= |x| # this >= 0 # result >= 0
            //        0      #     0     #    1
            //        0      #     1     #    0
            //        1      #     0     #    0
            //        1      #     1     #    1
            // the conclusion is that the result is non-negative if and only if
            // these two logical values are equal
            if (thisIsGreater == (sign == 1)) {
                sign = 1;
            } else {
                sign = -1;
            }
        }
    }

    /**
     * Subtract given big number from this number.
     *
     * @param x Subtrahent.
     */
    public void subtract(BigNum x) {
        BigNum y = new BigNum(x);

        // add function handles all combinations of positive and negative 
        // numbers so we replace subtraction with addition
        y.setSign(-y.sign);
        add(y);
    }

    /**
     * Decreases a big number by given value (considers only absolute values and
     * works for non-negative result).
     *
     * @param x Value to subtract from this number.
     */
    public void absSubtract(BigNum x) {
        // minuend - subtrahent = result
        byte[] minuend = binaryRepresentation();
        byte[] subtrahent = x.binaryRepresentation();
        byte[] result = new byte[minuend.length];

        byte borrowed = 0;
        // we start from the least significant bit and perform bitwise
        // subtraction
        for (int i = minuend.length - 1; i >= 0; --i) {
            // if we 'borrowed' 1 in the last step we need to pay now
            minuend[i] -= borrowed;
            // simple subtraction
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
     * This method considers only absolute values of the numbers.
     *
     * @param modulus Modulus.
     */
    public void modulo(BigNum modulus) {

        // we subtract multiples of modulus until we get only the reminder
        while (absGreaterOrEqualTo(modulus)) {
            // get a copy of modulus
            BigNum x = new BigNum(modulus);
            // shift modulus left as much as you can
            // this operation is equivalent to finding modulus * 2^k with the
            // greatest k possible
            int shift = findMaximumLeftShift(x);
            x.shiftLeft(shift);

            // x is now some multiple of modulus so we can subtract it
            absSubtract(x);
        }

        // if this number is negative we need to correct the remainder
        if (sign == -1) {
            sign = 1;
            add(modulus);
        }
    }

    /**
     * Divides this number by given number and gives up the remainder.
     *
     * @param divisor Number to divide by.
     */
    public void divide(BigNum divisor) {
        BigNum result = new BigNum();

        // we subtract multiples of divisor from the initial number and remember
        // how many times divisor we subtracted
        while (absGreaterOrEqualTo(divisor)) {
            // get a copy of divisor
            BigNum x = new BigNum(divisor);
            // shift divisor left as much as you can
            // this operation is equivalent to finding divisor * 2^k with the
            // greatest k possible
            int shift = findMaximumLeftShift(x);
            x.shiftLeft(shift);

            // shift shows how many times divisor was subtracted
            // we add this value to the result
            // e. g. if shift was 3 it means we can perform:
            // this -= divisor * 2^3
            // we should add 2^3 = 8 to the result and it is the same as setting
            // an appropriate bit in the result
            // shift is different in every iteration of the loop so we can set
            // bit instead of adding
            result.setBit(BigNum.BITS - shift - 1, 1);

            // x is now some multiple of divisor so we can subtract it
            absSubtract(x);
        }

        sign *= divisor.sign;
        fillFromBinaryRepresentation(result.binaryRepresentation());
    }

    /**
     * Raises this number to given power and divides the result modulo another
     * given number.
     *
     * Signs of the numbers are ignored.
     *
     * @param exponent Exponent.
     * @param modulus Modulus.
     */
    public void powerModulo(BigNum exponent, BigNum modulus) {
        BigNum factor = new BigNum(this);
        factor.modulo(modulus);

        BigNum result = new BigNum(BigNum.ONE);

        // fast modular exponentation is used
        // we multiply the result on 1 bits of the exponent
        for (int i = BITS - 1; i >= 0; --i) {
            if (exponent.getBit(i) == 1) {
                result.multiply(factor);
                result.modulo(modulus);
            }

            // factor is squared in each step
            factor.multiply(factor);
            factor.modulo(modulus);
        }

        fillFromBinaryRepresentation(result.binaryRepresentation());
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
            number[i] = extractLast32Bits(generator.nextLong());
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
            number[block] |= (1L << (BLOCK_SIZE - positionInBlock - 1));
        } else if (value == 0) {
            number[block] &= ~(1L << (BLOCK_SIZE - positionInBlock - 1));
        }
    }

    /**
     * Returns bit value on given position.
     *
     * @param position Bit position (numbered from 0).
     * @return Bit value.
     */
    public byte getBit(int position) {
        int block = position / 32;
        int positionInBlock = position % 32;

        if ((number[block] & (1L << (BLOCK_SIZE - positionInBlock - 1))) != 0) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Finds maximum left shift of x so that it does not exceed this number.
     *
     * This method is equivalent to finding maximum k so that x * 2^k does not
     * exceed this number.
     *
     * @param x The number to be shifted.
     * @return Maximum shift (or -1 if x is greater than this number).
     */
    private int findMaximumLeftShift(BigNum x) {
        // if x is already greater than this number return -1
        int shift = -1;

        BigNum xCopy = new BigNum(x);

        while (absGreaterOrEqualTo(xCopy)) {
            ++shift;

            // if the first bit is 1 we cannot shift left anymore because we get 
            // overflow
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

        // when we shift left zeros appear on the right
        for (int i = binaryRepresentation.length - bias; i < binaryRepresentation.length; ++i) {
            binaryRepresentation[i] = 0;
        }

        fillFromBinaryRepresentation(binaryRepresentation);
    }

    /**
     * Shifts the number right by given number of bits (unsigned shift).
     *
     * Unsigned shift means we do not care if the number is positive or not.
     *
     * @param bias Number of bits by which this number should be shifted.
     */
    public void shiftRight(int bias) {
        byte[] binaryRepresentation = binaryRepresentation();

        for (int i = binaryRepresentation.length - 1; i >= bias; --i) {
            binaryRepresentation[i] = binaryRepresentation[i - bias];
        }

        // when we perform unsigned right shift zeros appear on the left
        for (int i = 0; i < bias; ++i) {
            binaryRepresentation[i] = 0;
        }

        fillFromBinaryRepresentation(binaryRepresentation);
    }

    /**
     * Fills this number content with given binary representation.
     *
     * This method can be used as a clone not changing the original number's
     * sign.
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
     *
     * Includes not used 32 most significant bits of each block.
     */
    private void fillWithZeros() {
        for (int i = 0; i < BLOCKS; ++i) {
            number[i] = 0;
        }
    }

    /**
     * Extracts last 32 bits from long integer.
     *
     * Unlike casting long to int this method literally cuts last 32 bits.
     *
     * @param x Original number.
     * @return Last 32 bits of the number.
     */
    private long extractLast32Bits(long x) {
        return (x << 32) >>> 32;
    }

    /**
     * Returns a binary representation of big number as an array of bytes.
     *
     * @return Binary representation of big number in the form of an array of
     * bytes with each value from {0, 1}.
     */
    private byte[] binaryRepresentation() {
        byte[] result = new byte[BLOCKS * BLOCK_SIZE];

        int counter = 0;
        for (int i = 0; i < BLOCKS; ++i) {
            for (int j = BLOCK_SIZE - 1; j >= 0; --j) {
                if ((number[i] & (1L << j)) != 0) {
                    result[counter++] = 1;
                } else {
                    result[counter++] = 0;
                }
            }
        }

        return result;
    }

    /**
     * Checks if this number is greater or equal to given number (only absolute
     * values are concerned).
     *
     * @param x Number to compare with.
     * @return True if and only if this number is not less than given number
     * (only absolute values are concerned).
     */
    public boolean absGreaterOrEqualTo(BigNum x) {
        return absGreaterParametrized(x, false);
    }

    /**
     * Checks if this number is greater than given number (only absolute values
     * are concerned).
     *
     * @param x Number to compare with.
     * @return True if and only if this number is not less than given number
     * (only absolute values are concerned).
     */
    public boolean absGreaterThan(BigNum x) {
        return absGreaterParametrized(x, true);
    }

    /**
     * Checks if this number is greater than (or equal if not strict) than given
     * big number.
     *
     * @param x Number to compare with.
     * @param strict A flag indicating if comparison should be strict ('>'
     * rather than '>=').
     * @return True if and only if this number compares not less or greater
     * (only absolute values are concerned).
     */
    private boolean absGreaterParametrized(BigNum x, boolean strict) {
        byte[] me = binaryRepresentation();
        byte[] other = x.binaryRepresentation();

        for (int i = 0; i < me.length; ++i) {
            if (me[i] < other[i]) {
                return false;
            } else if (me[i] > other[i]) {
                return true;
            }
        }

        return !strict;
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

    /**
     * Converts big number to its binary representation with separation marks
     * between each 32 bits.
     *
     * @return Pretty binary representation of this big number.
     */
    public String toPrettyString() {
        StringBuilder builder = new StringBuilder();

        if (sign == 1) {
            builder.append('+');
        } else if (sign == -1) {
            builder.append('-');
        } else {
            builder.append('?');
        }

        byte[] binaryRepresentation = binaryRepresentation();
        int counter = 0;
        for (byte b : binaryRepresentation) {
            if (counter != 0 && (counter % BLOCK_SIZE == 0)) {
                builder.append('-');
            }
            builder.append(b);
            ++counter;
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
        if (this.sign != other.sign) {
            return false;
        }
        return Arrays.equals(this.number, other.number);
    }

    /**
     * Checks if this number is non-negative.
     *
     * @return True if and only if this number's sign is +1.
     */
    public boolean isNonNegative() {
        return sign == 1;
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

    public int getSign() {
        return sign;
    }

    public void setSign(int sign) {
        this.sign = sign;
    }
}
