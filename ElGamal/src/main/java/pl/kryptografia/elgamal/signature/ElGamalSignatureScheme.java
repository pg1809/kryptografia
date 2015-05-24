package pl.kryptografia.elgamal.signature;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import pl.kryptografia.elgamal.bignum.BigNum;
import pl.kryptografia.elgamal.calculation.EuclideanSolver;
import pl.kryptografia.elgamal.io.BigNumsToBytesConverter;
import pl.kryptografia.elgamal.io.BytesToBigNumsConverter;

/**
 *
 */
public class ElGamalSignatureScheme implements SignatureScheme {

//    private final static PrimeGenerator primeGenerator = PrimeGenerator.getInstance();
    private final static EuclideanSolver euclideanSolver = EuclideanSolver.getInstance();

    private final static BigNumsToBytesConverter toBytesConverter = new BigNumsToBytesConverter();

    private final static BytesToBigNumsConverter toBigNumsConverter = new BytesToBigNumsConverter();

    private final PublicKey publicKey = new PublicKey();

    private final BigNum primeMinusOne;

    private final BigNum privateKey = new BigNum();

    public static BigInteger bi(BigNum x) {
        BigInteger result = new BigInteger(x.toString(), 2);
        if (x.getSign() == -1) {
            return result.negate();
        }
        return result;
    }

    public ElGamalSignatureScheme() {
//        BigNum q = new BigNum();
//        q.randomize(BigNum.BLOCKS / 2);
//        // p = 2 * q + 1
//        // that means q should have the first bit equal to zero not to overflow
//        q.setBit(BigNum.BITS / 2 - 1, 0);
//        // q should be odd because this is required by primeGenerator
//        q.setBit(BigNum.BITS - 1, 1);
//
//        Pair safePrimeWithGenerator = primeGenerator.generateSafePrimeWithGenerator(q);
//        publicKey.prime = safePrimeWithGenerator.first;
//        publicKey.generator = safePrimeWithGenerator.second;

        String prime = "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000011111111111111111111111111111111111111111111111111111111111111111100100100001111110110101010001000100001011010001100001000110100110001001100011001100010100010111000000011011100000111001101000100101001000000100100111000001000100010100110011111001100011101000000001000001011101111101010011000111011000100111001101100100010010100010100101000001000011110011000111000110100000001001101110111101111100101010001100110110011110011010011101001000011000110110011000000101011000010100110110111110010010111110001010000110111010011111110000100110101011011010110110101010001110000100100010111100100100001011011010101110110011000100101111001111110110001101111010001001100010000101110100110100110001101111110110101101011000010111111111101011100101101101111010000000110101101111110110111101110001110000110101111111011010110101000100110011111101001011010111010011111001001000001000101111100010010110001111111100110010010010010100001100110010100011110110011100100010110110011110111000010000000000111110010111000101000010110001110111111000001011001100011011010010010000011011000011100010101011101001110011010011010010001011000111111101010001111110100100100110011110101111110000011011001010101110100100011110111001010001110101101100101100001110001100010111100110101011000100000100001010101001010111011100111101101010100101001000001110111000010010110100101100110110101100111000011000011010101001110010010101011110010011000000001001111000101110100011011000000100011001010000110000010000101111100001100101001000001011110010001100010111000110110110011100011101111100011100111100111011100101100000110000000111010000110000000111001101100100111100000111010001011101100000001111010001010001111101101011100010101011101111100000110111101001100010100101100100111011110001010111100101111110110100101010101100000010111000110000011100110010101010010010111110011101010100101010110101011100101000101011101001000100110000110001001100011111010000001010001000000010101011100101000111001011010100010101010110010101010011010001111111111111111111111111111111111111111111111111111111111111111";
        for (int i = 0; i < prime.length(); ++i) {
            if (prime.charAt(i) == '0') {
                publicKey.prime.setBit(i, 0);
            } else {
                publicKey.prime.setBit(i, 1);
            }
        }
        primeMinusOne = new BigNum(publicKey.prime);
        primeMinusOne.subtract(BigNum.ONE);

        publicKey.generator = new BigNum(BigNum.TWO);

        do {
            privateKey.randomize(BigNum.BLOCKS / 2);
        } while (!primeMinusOne.absGreaterThan(privateKey) || !privateKey.absGreaterThan(BigNum.ONE));

        publicKey.y = new BigNum(publicKey.generator);
        publicKey.y.powerModulo(privateKey, publicKey.prime);
    }

    @Override
    public byte[] sign(byte[] originalMessage) {
        BigNum k = new BigNum();
        BigNum divisor;
        do {
            k.randomize(BigNum.BLOCKS / 2);
            divisor = new BigNum(k);
            divisor.gcd(primeMinusOne);
        } while (!divisor.equals(BigNum.ONE) || !primeMinusOne.absGreaterThan(k)
                || !k.absGreaterThan(BigNum.ONE));

        BigNum r = new BigNum(publicKey.generator);
        r.powerModulo(k, publicKey.prime);

        BigNum kInverse = euclideanSolver.inverseModulo(k, primeMinusOne);

        BigNum digest = hash(originalMessage);

        BigNum s = new BigNum(privateKey);
        s.multiply(r);
        s.modulo(primeMinusOne);
        s.setSign(-1);
        s.add(digest);
        s.modulo(primeMinusOne);
        s.multiply(kInverse);
        s.modulo(primeMinusOne);

        BigNum[] signature = new BigNum[]{r, s};
        return toBytesConverter.bigNumArrayToBytes(signature);
    }

    @Override
    public boolean verify(byte[] originalMessage, byte[] signature) {
        BigNum[] decodedSignature = toBigNumsConverter.convert(signature);
        BigNum r = decodedSignature[0];
        BigNum s = decodedSignature[1];

        if (!r.absGreaterOrEqualTo(BigNum.ONE) || !primeMinusOne.absGreaterOrEqualTo(r)) {
            return false;
        }

        BigNum exponentY = new BigNum(publicKey.y);
        exponentY.powerModulo(r, publicKey.prime);

        BigNum exponentR = new BigNum(r);
        exponentR.powerModulo(s, publicKey.prime);

        exponentY.multiply(exponentR);
        exponentY.modulo(publicKey.prime);

        BigNum digest = hash(originalMessage);
        BigNum pattern = new BigNum(publicKey.generator);
        pattern.powerModulo(digest, publicKey.prime);

        return (exponentY.equals(pattern));
    }

    private BigNum hash(byte[] originalMessage) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(originalMessage);
            byte[] paddedDigest = new byte[BytesToBigNumsConverter.BYTES_PER_BIGNUM];
            for (int i = 0; i < digest.length; ++i) {
                paddedDigest[paddedDigest.length - digest.length + i] = digest[i];
            }
            return toBigNumsConverter.convert(paddedDigest)[0];
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ElGamalSignatureScheme.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
}
