package pl.kryptografia.rabin.ui;

import java.math.BigInteger;
import java.util.Random;
import pl.kryptografia.rabin.bignum.BigNum;
import pl.kryptografia.rabin.calculation.EuclideanSolver;
import pl.kryptografia.rabin.calculation.Pair;
import pl.kryptografia.rabin.input.BytesToBigNumsConverter;

public class App {

    private static final Random generator = new Random();

    public static void main(String[] args) {
        
//        BigNum a = new BigNum();
//        a.randomize(4);
//        a.setBit(4096 - 4 * 32 + 1, 1);
//        System.out.println(a.toPrettyString());
//        a.shiftLeft(2);
//        System.out.println(a.toPrettyString());
//        
//        if (1 == 1) {
//            return;
//        }
        
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
//        BigInteger pPrime;
//        do {
//            pPrime = BigInteger.probablePrime(BigNum.BLOCK_SIZE * BigNum.BLOCKS / 4, generator);
//        } while (!pPrime.mod(new BigInteger("4")).equals(new BigInteger("3")));
//        
//        System.err.println("p generated");
//        
//        BigInteger qPrime;
//        do {
//            qPrime = BigInteger.probablePrime(BigNum.BLOCK_SIZE * BigNum.BLOCKS / 4, generator);
//        } while (!qPrime.mod(new BigInteger("4")).equals(new BigInteger("3")));
//        
//        System.err.println("Primes generated");
//        
//        System.out.println(pPrime.toString(2));
//        System.out.println(qPrime.toString(2));
//        
//        System.exit(0);
        String pPattern = "1100001110100010010100101101011101001100100101001011100010001011111111101111111100110101110010011110101111001101101000100010101010000011111011010110101011011101010011001001011010101101001011110000101011010011001001011101011111010001000001100100111111001010001010011001011110101000011001000011000111010000101001110000100010100010010101111001011001110011000011010111111010010111010111110100001100000100111010010110001111111001000100011010110010000110111001111011101000000110100110101101101111100101110100101000100001011010011110100100100000100001110011110100101101111001010110100010101001101011010000100001010011101111111100010100100001001100101011001110100000101101100011101011000110111010110011100101101011000001011100011000110110000100101110010000111000111001111011010001111000001000011111101110111001010011101110111101010110110000000111100001011001011100100001001110101111001000111111101010101001110000000001101100110011010101010000100110011110011011010010110001110000011110011110010010011100110111101010011001011100000011";
        String qPattern = "1000001101110101101001001000011110011101100011010100101100010110000010110000000000111100001101000101010111011101101101100101010101101011111111100000111001011011110000111111010000010100001010110100101110011110000101100101111011000011100101111011011001001001111110111000010011011010100010011110000100100001101001011011101000100101010111100101110011001100010011111000011111101010001000001000101101011000101110010101101001001111010111000001111101010000111001111110011010010001001101101111000111101010010100010011110010110001100100100100110000101001010101011010010101100011111111111100111000010111110001100101111011011000100100101110111110101111011101111010101110001101101001011100110111000000100011000110111011011100000011010010111110000111101100101101001101010001101000010100110100100101111100000111001000000111101001001011001111110100111011100011001000110001000100110010101011101010100011110110110101000001100001000101110100110010111011000110111001000000001101100000110110101010110111111011101010110010100100101011110001111111";
        for (int i = 0; i < pPattern.length(); ++i) {
            if (pPattern.charAt(i) == '0') {
                p.setBit(3 * 1024 + i, 0);
            } else {
                p.setBit(3 * 1024 + i, 1);
            }

            if (qPattern.charAt(i) == '0') {
                q.setBit(3 * 1024 + i, 0);
            } else {
                q.setBit(3 * 1024 + i, 1);
            }
        }

        // the public key is a product of p and q
        BigNum publicKey = new BigNum(p);
        publicKey.multiply(q);

        // Generate random input and split it into BigNum chunks
        byte[] bytes = new byte[100];
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

        System.out.println("exponentP: " + exponentP.toPrettyString());
        System.out.println("p: " + p.toPrettyString());

        BigNum exponentQ = new BigNum(q);
        exponentQ.add(BigNum.ONE);
        exponentQ.shiftRight(2);

        // extended Euclidean algorithm
        // we search for yP and yQ such that:
        // yP * p + yQ * q = 1
        Pair solution = EuclideanSolver.getInstance().solve(p, q);
        BigNum yP = solution.first;
        BigNum yQ = solution.second;

        System.err.println("After euclidean solver");

        // we calculate the remainder modulo public key because yP and yQ are
        // used only as a factor in equations calculated modulo public key
        // that way we make sure that all partial result will not overflow
        yP.modulo(publicKey);
        yQ.modulo(publicKey);

        System.err.println("After computing modulos");

        BigNum[] decryptedText = new BigNum[cipherText.length];
        int counter = 0;
        for (BigNum encryptedCharacter : cipherText) {
            BigNum squareP = new BigNum(encryptedCharacter);
//            System.err.println("BEFORE--------------squareP = " + squareP);
            squareP.powerModulo(exponentP, p);

            BigInteger result = new BigInteger(encryptedCharacter.toString(), 2);
            result = result.modPow(new BigInteger(exponentP.toString(), 2), new BigInteger(p.toString(), 2));
            System.err.println("AFTER---------------squareP = " + squareP);
            System.err.println("AFTER---------------result = " + result.toString(2));

            BigNum squareQ = new BigNum(encryptedCharacter);
            squareQ.powerModulo(exponentQ, q);

            result = new BigInteger(encryptedCharacter.toString(), 2);
            result = result.modPow(new BigInteger(exponentQ.toString(), 2), new BigInteger(q.toString(), 2));
            System.err.println("squareQ = " + squareQ);
            System.err.println("result = " + result.toString(2));

            BigNum tempP = new BigNum(p);
            tempP.multiply(squareQ);
            tempP.modulo(publicKey);
            tempP.multiply(yP);
            tempP.modulo(publicKey);

            result = new BigInteger(p.toString(), 2);
            result = result.multiply(new BigInteger(squareQ.toString(), 2));
            result = result.mod(new BigInteger(publicKey.toString(), 2));
            result = result.multiply(new BigInteger(yP.toString(), 2));
            result = result.mod(new BigInteger(publicKey.toString(), 2));

            System.err.println("tempP = " + tempP);
            System.err.println("result = " + result.toString(2));

            BigNum tempQ = new BigNum(q);
            tempQ.multiply(squareP);
            tempQ.modulo(publicKey);
            tempQ.multiply(yQ);
            tempQ.modulo(publicKey);

            result = new BigInteger(q.toString(), 2);
            result = result.multiply(new BigInteger(squareP.toString(), 2));
            result = result.mod(new BigInteger(publicKey.toString(), 2));
            result = result.multiply(new BigInteger(yQ.toString(), 2));
            result = result.mod(new BigInteger(publicKey.toString(), 2));

            System.err.println("tempQ = " + tempQ);
            System.err.println("result = " + result.toString(2));

            decryptedText[counter++] = checkPossibleTexts(publicKey, tempP, tempQ);
        }
    }

    /**
     * Checks which one of 4 possible results of decryption is the correct one.
     *
     * @param publicKey Public key.
     * @param tempP Coefficient of P to possible solutions.
     * @param tempQ Coefficient of Q to possible solutions.
     * @return Correct decrypted chunk of data.
     */
    private static BigNum checkPossibleTexts(BigNum publicKey, BigNum tempP, BigNum tempQ) {
        // there are 4 possible solutions of decryption
        BigNum[] possibleText = new BigNum[4];

        possibleText[0] = new BigNum(tempP);
        possibleText[0].add(tempQ);
        possibleText[0].modulo(publicKey);

//        System.err.println("tempP " + tempP);
//        System.err.println("tempQ " + tempQ);
        for (int i = 0; i < 4; ++i) {
            long hash = BytesToBigNumsConverter.calculateHash(possibleText[i], BytesToBigNumsConverter.BLOCKS_PER_CHUNK);
            long firstHashBlock = hash >>> BigNum.BLOCK_SIZE;
            long secondHashBlock = (hash << BigNum.BLOCK_SIZE) >>> BigNum.BLOCK_SIZE;

            if (possibleText[i].getBlock(BigNum.BLOCKS - 2) == firstHashBlock
                    && possibleText[i].getBlock(BigNum.BLOCKS - 1) == secondHashBlock) {
                return possibleText[i];
            }
        }

        return BigNum.ZERO;
    }
}
