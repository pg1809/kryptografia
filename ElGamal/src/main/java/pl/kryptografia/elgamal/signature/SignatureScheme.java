package pl.kryptografia.elgamal.signature;

/**
 *
 * @author Wojciech Szałapski
 */
public interface SignatureScheme {

    byte[] sign(byte[] originalMessage);
    
    boolean verify(byte[] originalMessage, byte[] signature);
}
