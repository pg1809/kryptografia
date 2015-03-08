package pl.pk.onetimepad;

/**
 *
 * @author Lukasz Cyran
 */
public interface IAlgorithm {   
    
    public byte[] encrypt(byte[] input);
    
    public byte[] decrypt(byte[] input);
    
    public byte[] getKey();
    
    public void setKey(byte[] key);
}
