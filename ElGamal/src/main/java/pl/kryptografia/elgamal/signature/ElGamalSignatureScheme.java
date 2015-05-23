package pl.kryptografia.elgamal.signature;

import java.math.BigInteger;
import pl.kryptografia.elgamal.bignum.BigNum;
import pl.kryptografia.elgamal.calculation.Pair;
import pl.kryptografia.elgamal.calculation.PrimeGenerator;

/**
 *
 */
public class ElGamalSignatureScheme implements SignatureScheme {

    private final static PrimeGenerator primeGenerator = PrimeGenerator.getInstance();
    
    private BigNum p;
    
    private BigNum generator;
    
    public static BigInteger bi(BigNum x) {
        BigInteger result = new BigInteger(x.toString(), 2);
        if (x.getSign() == -1) {
            return result.negate();
        }
        return result;
    }
    
    public ElGamalSignatureScheme() {
        BigNum q = new BigNum();
        q.randomize(BigNum.BLOCKS / 2);
        // p = 2 * q + 1
        // that means q should have the first bit equal to zero not to overflow
        q.setBit(BigNum.BITS / 2 - 1, 0);
        // q should be odd because this is required by primeGenerator
        q.setBit(BigNum.BITS - 1, 1);
        
        Pair safePrimeWithGenerator = primeGenerator.generateSafePrimeWithGenerator(q);
        p = safePrimeWithGenerator.first;
        generator = safePrimeWithGenerator.second;
        
        System.out.println(p.toPrettyString());
        System.out.println(bi(p).isProbablePrime(40));
        
        System.out.println(generator.toPrettyString());
    }
    
    @Override
    public byte[] sign(byte[] originalMessage) {
        return null;
    }

    @Override
    public boolean verify(byte[] originalMessage, byte[] signature) {
        return false;
    }
}
