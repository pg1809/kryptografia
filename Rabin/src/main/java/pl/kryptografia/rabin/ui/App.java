/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.kryptografia.rabin.ui;

import java.util.Arrays;
import java.util.Random;
import pl.kryptografia.rabin.bignum.BigNum;
import pl.kryptografia.rabin.input.BytesToBigNumsConverter;

/**
 *
 * @author Wojciech Szałapski
 */
public class App {

    private static final Random generator = new Random();

    public static void main(String[] args) {
        // p and q are factors of the public key
        BigNum p = new BigNum();
        BigNum q = new BigNum();

        // p and q are random 128-bit numbers
        p.randomize(4);
        q.randomize(4);

        // p and q now gives 3 modulo 4
        p.setBit(254, 1);
        p.setBit(255, 1);
        q.setBit(254, 1);
        q.setBit(255, 1);

        // the public key is a product of p and q
        BigNum publicKey = BigNum.multiply(p, q);

        // Generate random input and split it into BigNum chunks
        byte[] bytes = new byte[111];
        generator.nextBytes(bytes);

        BytesToBigNumsConverter converter = new BytesToBigNumsConverter(bytes);
        BigNum[] plainText = converter.convert();
    }
}
