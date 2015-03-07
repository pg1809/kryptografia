/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
