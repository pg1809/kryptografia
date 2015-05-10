package pl.kryptografia.rabin.input;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import pl.kryptografia.rabin.bignum.BigNum;

/**
 *
 * @author Lukasz Cyran
 */
public class BigNumsToBytesConverter {

    public static int paddedBytes;

    public static byte[] bigNumArrayToBytes(BigNum[] num) {
        List<byte[]> fragments = new ArrayList<>(num.length);
        for (int i = 0; i < num.length; ++i) {
            for (int j = BigNum.BLOCKS - BytesToBigNumsConverter.BLOCKS_PER_CHUNK - BytesToBigNumsConverter.HASH_BLOCKS; j < BigNum.BLOCKS; ++j) {
                fragments.add(longToBytes(num[i].getBlock(j)));
            }
        }

        byte[] array = new byte[fragments.size() * 4];
        for (int i = 0; i < fragments.size(); ++i) {
            for (int j = 0; j < 4; ++j) {
                array[i * j] = fragments.get(i)[j];
            }
        }
        
        return array;
    }

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
        // whole chunk blocks padded with zeros (only possible in last chunk)
        int actualPaddedBlocks = 0;
        // padded bytes in last data block (only possible in last chunk)
        int actualPaddedBytes = 0;

        if (lastChunk) {
            actualPaddedBlocks = paddedBytes / BytesToBigNumsConverter.BYTES_PER_BLOCK;
            actualPaddedBytes = paddedBytes % BytesToBigNumsConverter.BYTES_PER_BLOCK;
        }

        for (int i = BigNum.BLOCKS - chunkBlocksWithHash; i < BigNum.BLOCKS - BytesToBigNumsConverter.HASH_BLOCKS - actualPaddedBlocks; i++) {
            oneBlockBytes = longToBytes(input.getBlock(i));
            // data bytes without padded zeros
            int notPaddedDataBytesInBlock = oneBlockBytes.length;

            if (lastChunk && i == BigNum.BLOCKS - BytesToBigNumsConverter.HASH_BLOCKS - actualPaddedBlocks - 1) {
                notPaddedDataBytesInBlock = oneBlockBytes.length - actualPaddedBytes;
            }

            for (int j = 0; j < notPaddedDataBytesInBlock; j++) {
                listBytes.add(oneBlockBytes[j]);
            }
        }

        byte[] bytes = new byte[listBytes.size()];
        for (int i = 0; i < listBytes.size(); i++) {
            bytes[i] = listBytes.get(i);
        }
        return bytes;
    }

}
