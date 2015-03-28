package pl.kryptografia.rabin.input;

import pl.kryptografia.rabin.bignum.BigNum;

/**
 *
 * @author Wojciech Szałapski
 */
public class BytesToBigNumsConverter {

    /**
     * Bytes per single block of BigNum.
     */
    private final static int BYTES_PER_BLOCK = BigNum.BLOCK_SIZE / 8;
    
    /**
     * Bytes needed to create one chunk of input data.
     */
    private final static int BYTES_PER_CHUNK = BYTES_PER_BLOCK * BigNum.BLOCKS;

    /**
     * Bytes to convert (padded with zeros if needed).
     */
    private final byte[] bytes;

    /**
     * Creates a converter for given input bytes.
     *
     * Constructor adds trailing zeros to the input copy if needed.
     *
     * @param input Input bytes.
     */
    public BytesToBigNumsConverter(byte[] input) {
        int k = input.length;
        int newBytes = BYTES_PER_CHUNK - k % BYTES_PER_CHUNK;
        
        bytes = new byte[k + newBytes];
        for (int i = 0; i < k; ++i) {
            bytes[i] = input[i];
        }
        for (int i = 0; i < newBytes; ++i) {
            bytes[k + i] = 0;
        }
    }

    /**
     * Converts input bytes to an array of big integers.
     *
     * @return Input data chunked as an array of big integers.
     */
    public BigNum[] convert() {
        int k = bytes.length;
        
        BigNum[] result = new BigNum[k / BYTES_PER_CHUNK];
        for (int i = 0; i < k / BYTES_PER_CHUNK; ++i) {
            result[i] = convertToSingleBigNum(i);
        }
        
        return result;
    }

    /**
     * Converts bytes to a single chunk of data.
     *
     * @param index Number of chunk to convert (numbered from 0).
     * @return Chunk of data in the form of a BigNum.
     */
    private BigNum convertToSingleBigNum(int index) {
        BigNum result = new BigNum();
        
        int currentByte = index * BYTES_PER_CHUNK;
        
        for (int i = 0; i < BigNum.BLOCKS; ++i) {
            long block = 0;
            for (int j = 0; j < BYTES_PER_BLOCK; ++j) {
                block |= (bytes[currentByte] & 0xFF) << (24 - 8 * j);
                ++currentByte;
            }
            result.replaceBlock(i, block);
        }
        
        return result;
    }
}
