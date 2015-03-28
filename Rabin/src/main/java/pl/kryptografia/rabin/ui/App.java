package pl.kryptografia.rabin.ui;

import java.util.Random;
import pl.kryptografia.rabin.bignum.BigNum;
import pl.kryptografia.rabin.input.BytesToBigNumsConverter;

public class App {

    private static final Random generator = new Random();

    public static void main(String[] args) {
        // p and q are factors of the public key
        BigNum p = new BigNum();
        BigNum q = new BigNum();

        // p and q are random 128-bit numbers
        p.randomize(4);
        q.randomize(4);

        // p and q now gives 3 modulo 4
        p.setBit(254, 1);
        p.setBit(255, 1);
        q.setBit(254, 1);
        q.setBit(255, 1);

        // the public key is a product of p and q
        BigNum publicKey = new BigNum(p);
        publicKey.multiply(q);

        // Generate random input and split it into BigNum chunks
        byte[] bytes = new byte[111];
        generator.nextBytes(bytes);

        BytesToBigNumsConverter converter = new BytesToBigNumsConverter(bytes);
        BigNum[] plainText = converter.convert();

        // Encrypt plain text
        BigNum[] cipherText = new BigNum[plainText.length];
        for (BigNum plainCharacter : plainText) {
            BigNum x = new BigNum(plainCharacter);
            x.multiply(x);
            x.modulo(publicKey);
        }

        // Decrypt ciphertext
        BigNum one = new BigNum(1, BigNum.BLOCKS - 2);

        BigNum exponentP = new BigNum(p);
        exponentP.add(one);
        exponentP.shiftRight(2);

        BigNum exponentQ = new BigNum(q);
        exponentQ.add(one);
        exponentQ.shiftRight(2);

        BigNum[] decryptedText = new BigNum[cipherText.length];
        for (BigNum encryptedCharacter : cipherText) {

        }
    }
}
