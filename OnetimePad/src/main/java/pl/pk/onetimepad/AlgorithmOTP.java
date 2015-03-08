/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.pk.onetimepad;

import java.security.SecureRandom;

/**
 *
 * @author Lukasz Cyran
 */
public class AlgorithmOTP implements IAlgorithm {

    private byte[] key;
    
    /**
     * Metoda szyfrujaca tablice bajtow. Generowany jest losowy klucz o dlugosci
     * tekstu wejsciowego. Nastepnie dla kazdego bitu wejscia i klucza wykonywana
     * jest operacja XOR.
     * @param input - wejsciowa tablica bajtow
     * @return - zaszyfrowana tablica bajtow
     */

    @Override
    public byte[] encrypt(byte[] input) {
        key = new byte[input.length];
        new SecureRandom().nextBytes(key);
        byte[] output = new byte[input.length];
        for (int i = 0; i < input.length; i++) {
            output[i] = (byte) (input[i] ^ key[i]);
        }
        return output;
    }

    @Override
    public byte[] decrypt(byte[] input) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public byte[] getKey() {
        return key;
    }

    @Override
    public void setKey(byte[] key) {
        this.key = key;
    }

}
