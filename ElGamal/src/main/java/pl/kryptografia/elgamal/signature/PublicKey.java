package pl.kryptografia.elgamal.signature;

import pl.kryptografia.elgamal.bignum.BigNum;

/**
 *
 */
public class PublicKey {

    public BigNum prime = new BigNum();

    public BigNum generator = new BigNum();

    public BigNum y = new BigNum();

    @Override
    public String toString() {
        return prime.toPrettyString() + "\n" + generator.toPrettyString()
                + "\n" + y.toPrettyString();
    }
}
