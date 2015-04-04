package pl.kryptografia.rabin.input;

import pl.kryptografia.rabin.bignum.BigNum;

public class BytesToBigNumsConverter {

    /**
     * Bytes per single block of BigNum.
     */
    private final static int BYTES_PER_BLOCK = BigNum.BLOCK_SIZE / 8;

    /**
     * BigNum blocks per one data chunk.
     */
    private final static int BLOCKS_PER_CHUNK = BigNum.BLOCKS / 4;

    /**
     * Bytes needed to create one chunk of input data.
     */
    private final static int BYTES_PER_CHUNK = BYTES_PER_BLOCK * BLOCKS_PER_CHUNK;

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
        if (newBytes == BYTES_PER_CHUNK) {
            newBytes = 0;
        }

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
            addHashToBigNum(result[i]);
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

        for (int i = BigNum.BLOCKS - BLOCKS_PER_CHUNK - 1; i < BigNum.BLOCKS - 1; ++i) {
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

    /**
     * For given big number inserts into the 7-th block hash of blocks 5 and 6.
     *
     * This method assumes that the initial BigNum consists of two blocks (block
     * number 5 and 6) and adds hash of them in block 7.
     *
     * @param input BigNum without hash at last block.
     * @return BigNum with added hash at last block.
     */
    private BigNum addHashToBigNum(BigNum input) {
        long hash = input.calculateHash();
        System.out.println("H: " + hash);
        input.replaceBlock(7, hash);
        return input;
    }
}
