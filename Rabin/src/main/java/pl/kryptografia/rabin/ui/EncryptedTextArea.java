package pl.kryptografia.rabin.ui;

import javax.swing.JTextArea;

/**
 *
 * @author Wojciech Szałapski
 */
public class EncryptedTextArea extends JTextArea {

    private byte[] internalBuffer;

    public byte[] getInternalBuffer() {
        return internalBuffer;
    }

    public void setInternalBuffer(byte[] internalBuffer) {
        this.internalBuffer = internalBuffer;
    }
}
