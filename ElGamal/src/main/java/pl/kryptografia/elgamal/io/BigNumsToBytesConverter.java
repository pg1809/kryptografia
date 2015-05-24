package pl.kryptografia.elgamal.io;

import java.util.ArrayList;
import java.util.List;
import pl.kryptografia.elgamal.bignum.BigNum;

/**
 *
 */
public class BigNumsToBytesConverter {

    /**
     * Converts an array of big integers to an array of bytes.
     * 
     * Only the least significant half of the blocks is used in each BigNum.
     * 
     * @param numbers An array of BigNums.
     * @return Corresponding array of bytes.
     */
    public byte[] bigNumArrayToBytes(BigNum[] numbers) {
        List<byte[]> fragments = new ArrayList<>(numbers.length);
        
        for (int i = 0; i < numbers.length; ++i) {
            for (int j = BigNum.BLOCKS / 2; j < BigNum.BLOCKS; ++j) {
                fragments.add(longToBytes(numbers[i].getBlock(j)));
            }
        }
        
        byte[] array = new byte[fragments.size() * 4];
        for (int i = 0; i < fragments.size(); ++i) {
            for (int j = 0; j < 4; ++j) {
                array[4 * i + j] = fragments.get(i)[j];
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
    public byte[] longToBytes(long value) {
        byte[] result = new byte[4];
        for (int i = 3; i >= 0; i--) {
            result[i] = (byte) (value & 0xFF);
            value >>= 8;
        }
        return result;
    }
}
