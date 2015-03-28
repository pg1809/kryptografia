/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.kryptografia.rabin.ui;

import pl.kryptografia.rabin.bignum.BigNum;

/**
 *
 * @author Wojciech Szałapski
 */
public class App {

    public static void main(String[] args) {
        // p and q are factors of the public key
        BigNum p = new BigNum();
        BigNum q = new BigNum();
        
        // p and q are random 128-bit numbers
        p.randomize(4);
        q.randomize(4);
        
        // p and q now gives 3 modulo 4
        p.setBit(510, 1);
        p.setBit(511, 1);
        q.setBit(510, 1);
        q.setBit(511, 1);
        
        System.out.println(p);
        System.out.println(q);
        
        // the public key is a product of p and q
        BigNum publicKey = BigNum.multiply(p, q);
        System.out.println(publicKey);
    }
}
