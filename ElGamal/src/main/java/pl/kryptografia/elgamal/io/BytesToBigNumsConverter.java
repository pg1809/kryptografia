package pl.kryptografia.elgamal.io;

import pl.kryptografia.elgamal.bignum.BigNum;

/**
 *
 */
public class BytesToBigNumsConverter {

    private final static int BLOCKS_PER_BIGNUM = BigNum.BLOCKS / 2;
    
    private final static int BYTES_PER_BLOCK = 4;
    
    public final static int BYTES_PER_BIGNUM = BLOCKS_PER_BIGNUM * BYTES_PER_BLOCK;
    
    /**
     * Converts input bytes to an array of big integers.
     *
     * @param bytes Original binary stream.
     * @return Input data chunked as an array of big integers.
     */
    public BigNum[] convert(byte[] bytes) {
        int k = bytes.length;

        BigNum[] result = new BigNum[k / BYTES_PER_BIGNUM];
        for (int i = 0; i < k / BYTES_PER_BIGNUM; ++i) {
            result[i] = convertToSingleBigNum(i, bytes);
        }

        return result;
    }

    /**
     * Converts bytes to a single chunk of data.
     *
     * @param index Number of chunk to convert (numbered from 0).
     * @param bytes Original binary stream.
     * @return Chunk of data in the form of a BigNum.
     */
    private BigNum convertToSingleBigNum(int index, byte[] bytes) {
        BigNum result = new BigNum();

        int currentByte = index * BYTES_PER_BIGNUM;

        for (int i = BigNum.BLOCKS - (BLOCKS_PER_BIGNUM); i < BigNum.BLOCKS; ++i) {
            long block = 0;
            for (int j = 0; j < BYTES_PER_BLOCK; ++j) {
                // 0xFF sorcery lets us treat this byte as really unsigned
                block |= ((bytes[currentByte] & 0xFF) << (24 - 8 * j));
                ++currentByte;
            }
            // if our long becomes padded with leading ones we need to extract
            // last 32 bits
            result.replaceBlock(i, ((block << 32) >>> 32));
        }

        return result;
    }
}
