package pl.kryptografia.rabin.bignum;

import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Custom implementation of big integers.
 */
public class BigNum {

    /**
     * Block size in bits.
     */
    public static final int BLOCK_SIZE = 32;

    /**
     * Number of 32 bit blocks.
     *
     * 4 blocks = 128 bits, 8 blocks = 256 bits, 16 blocks = 512 bits, 32 blocks
     * = 1024 bits, 64 blocks = 2048 bits, 128 blocks = 4096 bits, 256 blocks =
     * 8192 bits
     */
    public static final int BLOCKS = 128;

    /**
     * Number of bits in one BigNum.
     */
    public static final int BITS = BLOCKS * BLOCK_SIZE;

    /**
     * Binary representation of the number.
     *
     * Each block consists of 64 bits but only least significant 32 of each are
     * relevant.
     */
    private final long[] number = new long[BLOCKS];

    /**
     * Number's sign (+1 or -1).
     */
    private int sign = 1;

    /**
     * BigNum representing 0.
     */
    public static final BigNum ZERO = new BigNum();

    /**
     * BigNum representing 1.
     */
    public static final BigNum ONE = new BigNum(1, BLOCKS - 2);

    /**
     * Random bits generator.
     */
    private final static SecureRandom generator = new SecureRandom();

    /**
     * Pool used for creating temporary big integers.
     */
    private final static BigNumPool pool = BigNumPool.getInstance();

    // beginMask[i] can be used to extract first i bits from long
    private final static long beginMask[] = new long[BLOCK_SIZE + 1];

    // endMask[i] can be used to extract last i bits from long
    private final static long endMask[] = new long[BLOCK_SIZE + 1];

    static {
        long eMask = 0;
        long bMask = 0;
        for (int i = 0; i < BLOCK_SIZE + 1; ++i) {
            beginMask[i] = bMask;
            endMask[i] = eMask;

            eMask |= (1L << i);
            bMask |= (1L << (BLOCK_SIZE - i - 1));
        }
    }

    /**
     * Creates a big number equal to 0.
     */
    public BigNum() {
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
        initializeFromLong(initialValue, firstBlock);
    }

    /**
     * Creates a big number identical to given big number.
     *
     * @param pattern Big number to be duplicated.
     */
    public BigNum(BigNum pattern) {
        initializeFromBigNum(pattern);
    }

    public BigNum(int initialValue) {
        initializeFromInt(initialValue);
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
        pool.open();

        BigNum result = pool.get();
        result.initializeFromBigNum(BigNum.ZERO);

        // multiply each block from this number by each block from number x
        // only non-zero blocks are considered (see method description)
        for (int i = BLOCKS / 2; i < BLOCKS; ++i) {
            for (int j = BLOCKS / 2; j < BLOCKS; ++j) {
                long product = number[i] * x.number[j];

                if (product == 0) {
                    continue;
                }

                // when two blocks are multiplied their positions need to be 
                // considered to shift the result left properly
                int position = i + j - BLOCKS;

                // add the result of this multiplication to the global result
                result.addLongOnPosition(product, position);
            }
        }

        copyBlockwise(result);
        sign = sign * x.sign;

        pool.close();
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
                a.initializeFromBigNum(x);
                b.initializeFromBigNum(this);
                thisIsGreater = false;
            }
            // now we are sure that |a| >= |b|

            // we subtract absolute values of the numbers
            a.absSubtract(b);
            // absolute value of the result is equal to absolute value of above
            // subtraction
            copyBlockwise(a);

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
     * Adds to this number a long number starting at given block.
     *
     * @param x Non-negative Long number to add.
     * @param position First block where x should be added.
     */
    private void addLongOnPosition(long x, int position) {
        // split long number into two 32 bit blocks
        long firstBlock = (x >>> 32);
        long secondBlock = extractLast32Bits(x);

        // add least significant block
        number[position + 1] += secondBlock;
        // handle possible overflow
        number[position] += (number[position + 1] >>> 32);
        number[position + 1] = extractLast32Bits(number[position + 1]);

        // add most significant block
        number[position] += firstBlock;

        // handle overflows as long as they occur
        int current = position;
        while (current > 0 && (number[current] >>> 32) != 0) {
            // move overflowing 1 bit to more significant block
            number[current - 1] += 1;
            // remove overflowing 1 bit from current block
            number[current] = extractLast32Bits(number[current]);

            --current;
        }
    }

    /**
     * Subtract given big number from this number.
     *
     * @param x Subtrahent.
     */
    public void subtract(BigNum x) {
        pool.open();

        BigNum y = pool.get();
        y.initializeFromBigNum(x);

        // add function handles all combinations of positive and negative 
        // numbers so we replace subtraction with addition
        y.setSign(-y.sign);
        add(y);

        pool.close();
    }

    /**
     * Decreases a big number by given value (considers only absolute values and
     * works for non-negative result).
     *
     * @param x Value to subtract from this number.
     */
    public void absSubtract(BigNum x) {

        // the value of the least significant bit borrowed from more significant
        // block
        long borrow = (1L << 32);

        // we subtract block by block, starting from the least significant ones
        for (int i = BLOCKS - 1; i >= 0; --i) {
            // if our block is lesser than corresponding block from x, we need 
            // to borrow one bit
            if (number[i] < x.number[i]) {
                // least significant bit in the previous block is worth 1
                --number[i - 1];
                // for us it is worth 2^33 because it is taken from more 
                // significant block
                number[i] += borrow;
            }
            // now we are sure that our block is greater then corresponding 
            // block in x
            number[i] -= x.number[i];
        }
    }

    /**
     * Divides this number modulo given big number.
     *
     * This method considers only absolute values of the numbers.
     *
     * @param modulus Modulus.
     */
    public void modulo(BigNum modulus) {
        pool.open();
        BigNum x = pool.get();

        // we subtract multiples of modulus until we get only the reminder
        while (absGreaterOrEqualTo(modulus)) {
            // get a copy of modulus
            x.initializeFromBigNum(modulus);

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
            add(modulus);
            sign = 1;
        }

        pool.close();
    }
    
    public boolean isDivisible(BigNum divisor) {
        pool.open();
        // copy of this number not to modify the original
        BigNum thisCopy = pool.get();
        thisCopy.initializeFromBigNum(this);
        // preallocated number for a copy of divisor
        BigNum x = pool.get();

        // we subtract multiples of divisor until we get only the reminder
        while (thisCopy.absGreaterOrEqualTo(divisor)) {
            // get a copy of divisor
            x.initializeFromBigNum(divisor);

            // shift divisor left as much as you can
            // this operation is equivalent to finding divisor * 2^k with the
            // greatest k possible
            int shift = thisCopy.findMaximumLeftShift(x);
            x.shiftLeft(shift);

            // x is now some multiple of divisor so we can subtract it
            thisCopy.absSubtract(x);
        }

        // if the number somehow became zero with minus sign, we need to adjust
        // the sign to perform equals method
        sign = 1;
        boolean result = thisCopy.equals(BigNum.ZERO);
        
        pool.close();
        
        return result;
    }

    /**
     * Divides this number by given number and gives up the remainder.
     *
     * @param divisor Number to divide by.
     */
    public void divide(BigNum divisor) {
        pool.open();

        BigNum result = pool.get();
        result.initializeFromBigNum(BigNum.ZERO);

        // we subtract multiples of divisor from the initial number and remember
        // how many times divisor we subtracted
        while (absGreaterOrEqualTo(divisor)) {
            // get a copy of divisor
            BigNum x = pool.get();
            x.initializeFromBigNum(divisor);
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
        copyBlockwise(result);

        pool.close();
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

        // factor is squared in each step
        // however we count how many times we need to multiply it and do the 
        // multiplication only when needed
        int factorMultiplications = 0;

        // fast modular exponentation is used
        // we multiply the result on 1 bits of the exponent
        for (int i = BITS - 1; i >= 0; --i) {
            if (exponent.getBit(i) == 1) {

                // perform factor squaring
                if (factorMultiplications > 0) {
                    for (int j = 0; j < factorMultiplications; ++j) {
                        factor.multiply(factor);
                        factor.modulo(modulus);
                    }

                    factorMultiplications = 0;
                }

                result.multiply(factor);
                result.modulo(modulus);
            }

            ++factorMultiplications;
        }

        copyBlockwise(result);
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

        if (!absGreaterOrEqualTo(x)) {
            return shift;
        }

        shift = 0;

        // create a copy of x not to shift the original
        pool.open();
        BigNum xCopy = pool.get();
        xCopy.initializeFromBigNum(x);

        // count leading zeros to make the initial shift
        int myLeadingZeros = countLeadingZeros();
        int xLeadingZeros = xCopy.countLeadingZeros();

        // if this condition is true we can safely shift x and still be sure
        // that it is less than this number
        if (xLeadingZeros > myLeadingZeros + 1) {
            shift = xLeadingZeros - myLeadingZeros - 1;
            xCopy.shiftLeft(shift);
        }

        // in the most significant bit is 1 we cannot shift left anymore
        if (xCopy.getBit(0) == 1) {
            pool.close();
            return shift;
        }

        // check if we can make one more shift
        xCopy.shiftLeft(1);

        if (absGreaterOrEqualTo(xCopy)) {
            ++shift;
        }

        pool.close();
        return shift;
    }

    /**
     * Shifts this number left by given number of bits.
     *
     * @param bias Number of bits by which this number should be shifted.
     */
    public void shiftLeft(int bias) {
        // how many whole blocks we shift
        int shiftBlocks = bias / BLOCK_SIZE;
        // how many bits remains to shift
        int innerShift = bias % BLOCK_SIZE;

        // shiftBlocks lets us now from which two blocks we need to extract bits
        // innerShift gives us information how many bits to take from each block
        // last shiftBlocks are filled with zeros
        // block with number (BLOCKS - shiftBlocks - 1) is partially filled with
        // zeros
        // that is why we took such boundaries for the loop below
        // every other block can be really an effect of the shift
        for (int i = 0; i < BLOCKS - shiftBlocks - 1; ++i) {
            number[i] = ((number[i + shiftBlocks] & endMask[BLOCK_SIZE - innerShift]) << innerShift)
                    | ((number[i + shiftBlocks + 1] & beginMask[innerShift]) >>> (BLOCK_SIZE - innerShift));
        }

        // block which is partially filled with zeros
        number[BLOCKS - shiftBlocks - 1] = (number[BLOCKS - 1] & endMask[BLOCK_SIZE - innerShift]) << innerShift;

        // blocks totally filled with zeros
        for (int i = BLOCKS - shiftBlocks; i < BLOCKS; ++i) {
            number[i] = 0;
        }
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
     * Copies given number's absolute value into this number blocks.
     *
     * This method can be used as a clone not changing the original number's
     * sign.
     *
     * @param pattern Number to copy.
     */
    private void copyBlockwise(BigNum pattern) {
        for (int i = 0; i < BLOCKS; ++i) {
            number[i] = pattern.number[i];
        }
    }

    /**
     * Puts given long number into two blocks of given position and nullify
     * other blocks.
     *
     * @param initialValue Long value to put.
     * @param firstBlock First block to put long into.
     */
    private void initializeFromLong(long initialValue, int firstBlock) {
        copyBlockwise(BigNum.ZERO);
        number[firstBlock + 1] = extractLast32Bits(initialValue);
        number[firstBlock] = (initialValue >>> 32);
    }

    /**
     * Puts given positive integer into last block of this big number.
     *
     * @param initialValue Positive initial value of the least significant
     * block.
     */
    private void initializeFromInt(int initialValue) {
        copyBlockwise(BigNum.ZERO);
        number[BLOCKS - 1] = initialValue;
    }

    /**
     * This big integer becomes a copy of a given big integer.
     *
     * @param pattern Big integer to copy.
     */
    private void initializeFromBigNum(BigNum pattern) {
        sign = pattern.sign;
        copyBlockwise(pattern);
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

        for (int i = 0; i < BLOCKS; ++i) {
            if (number[i] < x.number[i]) {
                return false;
            } else if (number[i] > x.number[i]) {
                return true;
            }
        }

        return !strict;
    }

    /**
     * Returns the number of leading empty blocks in this big integer.
     *
     * @return Number of leading empty blocks in this big integer.
     */
    private int countLeadingEmptyBlocks() {
        int emptyBlocks = 0;

        while (emptyBlocks < BLOCKS && number[emptyBlocks] == 0) {
            ++emptyBlocks;
        }

        return emptyBlocks;
    }

    /**
     * Returns the number of leading zeros in this big integer.
     *
     * @return Number of leading zeros in this big integer.
     */
    private int countLeadingZeros() {
        // count empty leading blocks
        int emptyBlocks = countLeadingEmptyBlocks();
        int counter = BLOCK_SIZE * emptyBlocks;

        // count leading zeros in the first non-empty block if it exists
        if (emptyBlocks < BLOCKS) {
            long mask = (1L << 31);
            while ((number[emptyBlocks] & mask) == 0) {
                mask >>>= 1;
                ++counter;
            }
        }

        return counter;
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
