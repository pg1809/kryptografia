package pl.kryptografia.elgamal.signature;

/**
 *
 */
public interface SignatureScheme {

    byte[] sign(byte[] originalMessage);
    
    boolean verify(byte[] originalMessage, byte[] signature);
}
