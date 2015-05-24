package pl.kryptografia.elgamal.gui;

import java.util.Random;
import pl.kryptografia.elgamal.bignum.BigNum;
import pl.kryptografia.elgamal.signature.ElGamalSignatureScheme;

/**
 *
 * @author Wojciech Szałapski
 */
public class TestApp {

    public static void main(String[] args) {
        BigNum p = new BigNum();
        ElGamalSignatureScheme scheme = new ElGamalSignatureScheme();

        byte[] originalMessage = new byte[1000];
        new Random().nextBytes(originalMessage);

        byte[] signature = scheme.sign(originalMessage);
        System.out.println(scheme.verify(originalMessage, signature));
    }
}
