package pl.kryptografia.rabin.input;

import pl.kryptografia.rabin.bignum.BigNum;
import static pl.kryptografia.rabin.bignum.BigNum.BLOCKS;

public class BytesToBigNumsConverter {

    /**
     * Bytes per single block of BigNum.
     */
    private final static int BYTES_PER_BLOCK = BigNum.BLOCK_SIZE / 8;

    /**
     * BigNum blocks of hash.
     */
    public final static int HASH_BLOCKS = 2;

    /**
     * BigNum blocks per one data chunk.
     */
    public final static int BLOCKS_PER_CHUNK = BigNum.BLOCKS / 2 - HASH_BLOCKS;

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
        BigNumsToBytesConverter.paddedBytes = newBytes;
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

        for (int i = BigNum.BLOCKS - (BLOCKS_PER_CHUNK + HASH_BLOCKS); i < BigNum.BLOCKS - HASH_BLOCKS; ++i) {
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
     * Calculates hash of input blocks.
     *
     * @param input BigNum to calculate hash.
     * @param dataSizeInBlocks Size of data to hash in blocks.
     * @return Hash of blocks.
     */
    public static long calculateHash(BigNum input, int dataSizeInBlocks) {
        long hashCode = 0;
        int startingDataBlock = BLOCKS - (BLOCKS_PER_CHUNK + HASH_BLOCKS);
        for (int i = 0; i < dataSizeInBlocks; i++) {
            hashCode = 31 * hashCode + (input.getBlock(startingDataBlock + i) & 0xffffffffL);
        }
        return hashCode * input.getSign();
    }

    /**
     * For given big number inserts into the last two block hash of blocks
     * 80-111.
     *
     * This method assumes that the initial BigNum consists of blocks 80-111 and
     * adds hash of them in last two blocks.
     *
     * @param input BigNum without hash at last two blocks.
     * @return BigNum with added hash at last two blocks.
     */
    private BigNum addHashToBigNum(BigNum input) {
        long hash = calculateHash(input, BLOCKS_PER_CHUNK);
        long firstHashBlock = hash >>> BigNum.BLOCK_SIZE;
        long secondHashBlock = (hash << BigNum.BLOCK_SIZE) >>> BigNum.BLOCK_SIZE;

        input.replaceBlock(BigNum.BLOCKS - 2, firstHashBlock);
        input.replaceBlock(BigNum.BLOCKS - 1, secondHashBlock);
        return input;
    }
}
