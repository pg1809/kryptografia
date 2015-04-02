package pl.kryptografia.rabin.ui;

import java.math.BigInteger;
import java.util.Random;
import pl.kryptografia.rabin.bignum.BigNum;
import pl.kryptografia.rabin.calculation.EuclideanSolver;
import pl.kryptografia.rabin.calculation.Pair;
import pl.kryptografia.rabin.input.BytesToBigNumsConverter;

public class App {

    public static BigInteger bi(BigNum b) {
        BigInteger result = new BigInteger(b.toString(), 2);
        if (b.getSign() == -1) {
            result = result.negate();
        }
        return result;
    }

    private static final Random generator = new Random();

    public static void main(String[] args) {

        // p and q are factors of the public key
        BigNum p = new BigNum();
        BigNum q = new BigNum();

        // p and q are big distinct random numbers
        p.randomize(BigNum.BLOCKS / 4);
        do {
            q.randomize(BigNum.BLOCKS / 4);
        } while (p.equals(q));

        // p and q now gives 3 modulo 4
        p.setBit(BigNum.BITS - 2, 1);
        p.setBit(BigNum.BITS - 1, 1);
        q.setBit(BigNum.BITS - 2, 1);
        q.setBit(BigNum.BITS - 1, 1);

        // p and q are now prime numbers
//        p = PrimeGenerator.getInstance().findNotGreaterPrimeModulo4(p);
//        q = PrimeGenerator.getInstance().findNotGreaterPrimeModulo4(q);
//        
//        System.out.println("primes generated");
        String pPattern = "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000101010001000010010110011111111010110001110001011110011001100011";
        String qPattern = "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001001011011110101100100011111101011111011000001010100110110101011";
        for (int i = 0; i < pPattern.length(); ++i) {
            if (pPattern.charAt(i) == '0') {
                p.setBit(i, 0);
            } else {
                p.setBit(i, 1);
            }

            if (qPattern.charAt(i) == '0') {
                q.setBit(i, 0);
            } else {
                q.setBit(i, 1);
            }
        }

//        p = new BigNum(7, BigNum.BLOCKS - 2);
//        q = new BigNum(11, BigNum.BLOCKS - 2);
        // the public key is a product of p and q
        BigNum publicKey = new BigNum(p);
        publicKey.multiply(q);

        // Generate random input and split it into BigNum chunks
        byte[] bytes = new byte[300];
        generator.nextBytes(bytes);

        BytesToBigNumsConverter converter = new BytesToBigNumsConverter(bytes);
        BigNum[] plainText = converter.convert();
//        BigNum[] plainText = new BigNum[]{new BigNum(20, BigNum.BLOCKS - 2)};

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

        // extended Euclidean algorithm
        // we search for yP and yQ such that:
        // yP * p + yQ * q = 1
        Pair solution = EuclideanSolver.getInstance().solve(p, q);
        BigNum yP = solution.first;
        BigNum yQ = solution.second;

        // we calculate the remainder modulo public key because yP and yQ are
        // used only as a factor in equations calculated modulo public key
        // that way we make sure that all partial result will not overflow
        yP.modulo(publicKey);
        yQ.modulo(publicKey);

        BigNum[] decryptedText = new BigNum[cipherText.length];
        int counter = 0;
        for (BigNum encryptedCharacter : cipherText) {
            BigNum squareP = new BigNum(encryptedCharacter);
            squareP.powerModulo(exponentP, p);

            BigNum squareQ = new BigNum(encryptedCharacter);
            squareQ.powerModulo(exponentQ, q);

            BigNum tempP = new BigNum(p);
            tempP.multiply(squareQ);
            tempP.modulo(publicKey);
            tempP.multiply(yP);
            tempP.modulo(publicKey);

            BigNum tempQ = new BigNum(q);
            tempQ.multiply(squareP);
            tempQ.modulo(publicKey);
            tempQ.multiply(yQ);
            tempQ.modulo(publicKey);

            // there are 4 possible solutions of decryption
            BigNum[] possibleText = new BigNum[4];

            possibleText[0] = new BigNum(tempP);
            possibleText[0].add(tempQ);
            possibleText[0].modulo(publicKey);

            possibleText[1] = new BigNum(tempP);
            possibleText[1].subtract(tempQ);
            possibleText[1].modulo(publicKey);

            possibleText[2] = new BigNum(publicKey);
            possibleText[2].subtract(possibleText[0]);

            possibleText[3] = new BigNum(publicKey);
            possibleText[3].subtract(possibleText[1]);

            System.out.println(counter + " : ");
            BigInteger current = bi(plainText[counter++]);
            for (int i = 0; i < 4; ++i) {
                if (bi(possibleText[i]).equals(current)) {
                    System.out.println(i);
                }
            }
            System.out.println("");
        }
    }
}
