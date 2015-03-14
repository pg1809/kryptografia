package kryptografia.pk.onetimepad;

import java.security.SecureRandom;

/**
 *
 * @author Lukasz Cyran
 * @author Wojciech Szałapski
 */
public class AlgorithmOTP implements IAlgorithm {

    private byte[] key;

    /**
     * Metoda szyfrujaca tablice bajtow. Generowany jest losowy klucz o dlugosci
     * tekstu wejsciowego. Nastepnie dla kazdego bitu wejscia i klucza
     * wykonywana jest operacja XOR.
     *
     * @param input wejsciowa tablica bajtow
     * @return zaszyfrowana tablica bajtow
     */
    @Override
    public byte[] encrypt(byte[] input) {
        key = new byte[input.length];
        new SecureRandom().nextBytes(key);
        return arrayXor(input, key);
    }

    /**
     * Metoda deszyfrująca tablicę bajtów. W celu deszyfracji wykonywana jest
     * operacja XOR kolejno na każdym bicie zaszyfrowanej wiadomości i
     * wczytanego uprzednio klucza.
     *
     * @param input zaszyfrowana wiadomość w postaci tablicy bajtów
     * @return odszyfrowana wiadomość w postaci tablicy bajtów
     */
    @Override
    public byte[] decrypt(byte[] input) {
        return arrayXor(input, key);
    }

    /**
     * Metoda przeprowadza operację XOR na kolejnych bitach danych dwóch tablic
     * bajtów.
     *
     * @param input wejściowa tablica bajtów, której długość określa długość
     * wyniku
     * @param key tablica dostarczająca odpowiednie bity dla operacji XOR
     * @return tablica bajtów będąca efektem wykonania operacji XOR na kolejnych
     * bitach
     */
    private byte[] arrayXor(byte[] input, byte[] key) {
        byte[] output = new byte[input.length];
        for (int i = 0; i < input.length; ++i) {
            output[i] = (byte) (input[i] ^ key[i]);
        }
        
        return output;
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
