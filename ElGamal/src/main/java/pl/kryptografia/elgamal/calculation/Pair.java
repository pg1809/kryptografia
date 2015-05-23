package pl.kryptografia.elgamal.calculation;

import pl.kryptografia.elgamal.bignum.BigNum;

public class Pair {

    public Pair(BigNum first, BigNum second) {
        this.first = first;
        this.second = second;
    }
    
    public BigNum first;

    public BigNum second;
}
