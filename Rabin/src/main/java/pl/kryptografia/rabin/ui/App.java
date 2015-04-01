package pl.kryptografia.rabin.ui;

import java.util.Random;
import pl.kryptografia.rabin.bignum.BigNum;
import pl.kryptografia.rabin.calculation.EuclideanSolver;
import pl.kryptografia.rabin.calculation.Pair;
import pl.kryptografia.rabin.calculation.PrimeGenerator;
import pl.kryptografia.rabin.input.BytesToBigNumsConverter;

public class App {

    private static final Random generator = new Random();

    public static void main(String[] args) {

        // p and q are factors of the public key
        BigNum p = new BigNum();
        BigNum q = new BigNum();

        // p and q are big distinct random numbers
        p.randomize(BigNum.BLOCKS / 2);
        do {
            q.randomize(BigNum.BLOCKS / 2);
        } while (p.equals(q));

        // p and q now gives 3 modulo 4
        p.setBit(BigNum.BITS - 2, 1);
        p.setBit(BigNum.BITS - 1, 1);
        q.setBit(BigNum.BITS - 2, 1);
        q.setBit(BigNum.BITS - 1, 1);

        // p and q are now prime numbers
        p = PrimeGenerator.getInstance().findNotGreaterPrimeModulo4(p);
        q = PrimeGenerator.getInstance().findNotGreaterPrimeModulo4(q);

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
        for (int i = 0; i < plainText.length; ++i) {
            cipherText[i] = new BigNum(plainText[i]);
            cipherText[i].multiply(cipherText[i]);
            cipherText[i].modulo(publicKey);
        }

        // Decrypt ciphertext
        BigNum exponentP = new BigNum(p);
        exponentP.add(BigNum.ONE);
        exponentP.shiftRight(2);

        BigNum exponentQ = new BigNum(q);
        exponentQ.add(BigNum.ONE);
        exponentQ.shiftRight(2);

        BigNum[] decryptedText = new BigNum[cipherText.length];
        for (BigNum encryptedCharacter : cipherText) {
            BigNum squareP = new BigNum(encryptedCharacter);
            squareP.powerModulo(exponentP, p);

            BigNum squareQ = new BigNum(encryptedCharacter);
            squareQ.powerModulo(exponentQ, q);
            
            Pair solution = EuclideanSolver.getInstance().solve(p, q);
            BigNum yP = solution.first;
            BigNum yQ = solution.second;
        }
    }
}
