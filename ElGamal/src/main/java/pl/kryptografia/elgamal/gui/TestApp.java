package pl.kryptografia.elgamal.gui;

import java.math.BigInteger;
import pl.kryptografia.elgamal.bignum.BigNum;
import pl.kryptografia.elgamal.signature.ElGamalSignatureScheme;

/**
 *
 * @author Wojciech Szałapski
 */
public class TestApp {

    public static void main(String[] args) {
//        http://tools.ietf.org/html/rfc3526#section-3
//        BigInteger x = new BigInteger("FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1"
//                + "29024E088A67CC74020BBEA63B139B22514A08798E3404DD"
//                + "EF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245"
//                + "E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7ED"
//                + "EE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3D"
//                + "C2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F"
//                + "83655D23DCA3AD961C62F356208552BB9ED529077096966D"
//                + "670C354E4ABC9804F1746C08CA18217C32905E462E36CE3B"
//                + "E39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9"
//                + "DE2BCBF6955817183995497CEA956AE515D2261898FA0510"
//                + "15728E5A8AACAA68FFFFFFFFFFFFFFFF", 16);
//        
//        for (int i = 0; i < 2048; ++i) {
//            System.out.print("0");
//        }
//        System.out.println(x.toString(2));

        BigNum p = new BigNum();
        ElGamalSignatureScheme scheme = new ElGamalSignatureScheme();

        byte[] signature = scheme.sign(null);
        System.out.println(scheme.verify(null, signature));
    }
}
