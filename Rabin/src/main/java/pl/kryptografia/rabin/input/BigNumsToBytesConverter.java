package pl.kryptografia.rabin.input;

import java.util.ArrayList;
import java.util.List;
import pl.kryptografia.rabin.bignum.BigNum;

/**
 *
 * @author Lukasz Cyran
 */
public class BigNumsToBytesConverter {

    public static int paddedBytes;

    /**
     * Converts long value to an array of bytes (1 long = 4 bytes).
     *
     * @param value Long value to convertChunk.
     * @return Converted byte array.
     */
    public static byte[] longToBytes(long value) {
        byte[] result = new byte[4];
        for (int i = 3; i >= 0; i--) {
            result[i] = (byte) (value & 0xFF);
            value >>= 8;
        }
        return result;
    }

    /**
     * Converts chunk (big integer) to an array of bytes. Extracts data from big
     * integer discarding hash bytes and padded bytes.
     *
     * @param input Big integer to convertChunk.
     * @param lastChunk Boolean flag to check if chunk is last in input.
     * @return Converted byte array of chunk data.
     */
    public static byte[] convertChunk(BigNum input, boolean lastChunk) {
        List<Byte> listBytes = new ArrayList<>();
        byte[] oneBlockBytes;
        int chunkBlocksWithHash = BytesToBigNumsConverter.BLOCKS_PER_CHUNK + BytesToBigNumsConverter.HASH_BLOCKS;
        int actualPaddedBlocks = 0;
        if (lastChunk) {
            actualPaddedBlocks = paddedBytes * 8 / BigNum.BLOCK_SIZE;
        }
        
        for (int i = BigNum.BLOCKS - chunkBlocksWithHash; i < BigNum.BLOCKS - BytesToBigNumsConverter.HASH_BLOCKS - actualPaddedBlocks; i++) {
            oneBlockBytes = longToBytes(input.getBlock(i));
            for (byte oneByte : oneBlockBytes) {
                listBytes.add(oneByte);
            }
        }
        
        byte[] bytes = new byte[listBytes.size()];
        for (int i = 0; i < listBytes.size(); i++) {
            bytes[i] = listBytes.get(i);
        }
        return bytes;
    }

}
