/**
 * Laboratorium, pon. godz 14.15 Zestaw nr 3
 *
 * Łukasz Cyran - 180519 Piotr Grzelak - 180553 Wojciech Szałapski - 180706
 */
package pl.kryptografia.rabin.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import pl.kryptografia.rabin.bignum.BigNum;
import pl.kryptografia.rabin.calculation.EuclideanSolver;
import pl.kryptografia.rabin.calculation.Pair;
import pl.kryptografia.rabin.calculation.PrimeSieve;
import pl.kryptografia.rabin.input.BigNumsToBytesConverter;
import pl.kryptografia.rabin.input.BytesToBigNumsConverter;

public class AlgorithmGUI extends javax.swing.JFrame {

    private BigNum p;

    private BigNum q;

    private BigNum publicKey;

    private BytesToBigNumsConverter converter;

    private int plainTextBytesLength;

    /**
     * Creates new form AlgorithmGUI
     */
    public AlgorithmGUI() {
        initComponents();
        setTitle("Rabin cryptosystem");

        jTextAreaInputText.setLineWrap(true);
        jTextAreaOutputText.setLineWrap(true);
    }

    private void initKey() {
//        KeyDialog keyDialog = new KeyDialog(this, false);
//        keyDialog.setVisible(true);
//        BigNum initialCandidate = new BigNum();
//        initialCandidate.randomize(BigNum.BLOCKS / 4);
//        initialCandidate.setBit(BigNum.BITS - 2, 1);
//        initialCandidate.setBit(BigNum.BITS - 1, 1);
//
//        PrimeSieve sieve = PrimeSieve.getInstance();
//        Pair privateKey = sieve.generateTwoPrimes(initialCandidate);
//
//        p = privateKey.first;
//        q = privateKey.second;

        p = new BigNum();
        q = new BigNum();

        String pPattern = "1100001110100010010100101101011101001100100101001011100010001011111111101111111100110101110010011110101111001101101000100010101010000011111011010110101011011101010011001001011010101101001011110000101011010011001001011101011111010001000001100100111111001010001010011001011110101000011001000011000111010000101001110000100010100010010101111001011001110011000011010111111010010111010111110100001100000100111010010110001111111001000100011010110010000110111001111011101000000110100110101101101111100101110100101000100001011010011110100100100000100001110011110100101101111001010110100010101001101011010000100001010011101111111100010100100001001100101011001110100000101101100011101011000110111010110011100101101011000001011100011000110110000100101110010000111000111001111011010001111000001000011111101110111001010011101110111101010110110000000111100001011001011100100001001110101111001000111111101010101001110000000001101100110011010101010000100110011110011011010010110001110000011110011110010010011100110111101010011001011100000011";
        String qPattern = "1000001101110101101001001000011110011101100011010100101100010110000010110000000000111100001101000101010111011101101101100101010101101011111111100000111001011011110000111111010000010100001010110100101110011110000101100101111011000011100101111011011001001001111110111000010011011010100010011110000100100001101001011011101000100101010111100101110011001100010011111000011111101010001000001000101101011000101110010101101001001111010111000001111101010000111001111110011010010001001101101111000111101010010100010011110010110001100100100100110000101001010101011010010101100011111111111100111000010111110001100101111011011000100100101110111110101111011101111010101110001101101001011100110111000000100011000110111011011100000011010010111110000111101100101101001101010001101000010100110100100101111100000111001000000111101001001011001111110100111011100011001000110001000100110010101011101010100011110110110101000001100001000101110100110010111011000110111001000000001101100000110110101010110111111011101010110010100100101011110001111111";
        for (int i = 0; i < pPattern.length(); ++i) {
            if (pPattern.charAt(i) == '0') {
                p.setBit(3 * 1024 + i, 0);
            } else {
                p.setBit(3 * 1024 + i, 1);
            }

            if (qPattern.charAt(i) == '0') {
                q.setBit(3 * 1024 + i, 0);
            } else {
                q.setBit(3 * 1024 + i, 1);
            }
        }

        // the public key is a product of p and q
        publicKey = new BigNum(p);
        publicKey.multiply(q);
//        keyDialog.setVisible(false);        
    }

    /**
     * Pozwól użytkownikowi wybrać plik i zwróć informacje o tym pliku.
     *
     * @return Informacje o pełnej nazwie pliku oraz jego zawartości.
     * @throws FileNotFoundException
     * @throws IOException
     */
    private FileContent retrieveFileContent() throws FileNotFoundException, IOException {
        FileContent result = new FileContent();

        JFileChooser chooser = new JFileChooser();
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            result.setFilePath(file.getParent());
            result.setFileName(FilenameUtils.getBaseName(file.getAbsolutePath()));
            result.setFileExtension(FilenameUtils.getExtension(file.getAbsolutePath()));

            FileInputStream fis = new FileInputStream(file);
            byte[] binaryContent = new byte[(int) file.length()];
            fis.read(binaryContent);

            result.setBinaryConent(binaryContent);
            return result;
        }

        return null;
    }

    /**
     * Pozwól użytkownikowi wybrać plik i zapisz bajty do niego.
     *
     * @param byteArray Tablica bajtów do zapisania
     * @throws IOException
     */
    private void saveByteArrayToFile(byte[] byteArray) throws IOException {
        JFileChooser chooser = new JFileChooser();
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            FileUtils.writeByteArrayToFile(file, byteArray);
            JOptionPane.showMessageDialog(this, "Pomyślnie zapisano plik");
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        encryptedTextArea1 = new pl.kryptografia.rabin.ui.EncryptedTextArea();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextAreaInputText = new javax.swing.JTextArea();
        jButtonEncryptFile = new javax.swing.JButton();
        jButtonDecryptFile = new javax.swing.JButton();
        jButtonEncryptText = new javax.swing.JButton();
        jButtonDecryptText = new javax.swing.JButton();
        jLabelOutputText = new javax.swing.JLabel();
        jLabelInputText = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextAreaOutputText = new pl.kryptografia.rabin.ui.EncryptedTextArea();

        encryptedTextArea1.setColumns(20);
        encryptedTextArea1.setRows(5);
        jScrollPane2.setViewportView(encryptedTextArea1);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTextAreaInputText.setColumns(20);
        jTextAreaInputText.setRows(5);
        jScrollPane1.setViewportView(jTextAreaInputText);

        jButtonEncryptFile.setText("Szyfruj plik");
        jButtonEncryptFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEncryptFileActionPerformed(evt);
            }
        });

        jButtonDecryptFile.setText("Deszyfruj plik");
        jButtonDecryptFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDecryptFileActionPerformed(evt);
            }
        });

        jButtonEncryptText.setText("Szyfruj tekst");
        jButtonEncryptText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEncryptTextActionPerformed(evt);
            }
        });

        jButtonDecryptText.setText("Deszyfruj tekst");
        jButtonDecryptText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDecryptTextActionPerformed(evt);
            }
        });

        jLabelOutputText.setText("Wyjście");

        jLabelInputText.setText("Wejście");

        jTextAreaOutputText.setColumns(20);
        jTextAreaOutputText.setRows(5);
        jScrollPane4.setViewportView(jTextAreaOutputText);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane4)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(0, 10, Short.MAX_VALUE)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jLabelInputText)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabelOutputText)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(27, 27, 27)
                                        .addComponent(jButtonEncryptText)
                                        .addGap(91, 91, 91)
                                        .addComponent(jButtonDecryptText)))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addComponent(jButtonEncryptFile)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonDecryptFile)
                        .addGap(50, 50, 50))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonEncryptFile)
                    .addComponent(jButtonDecryptFile))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 13, Short.MAX_VALUE)
                .addComponent(jLabelInputText)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonEncryptText)
                    .addComponent(jButtonDecryptText))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabelOutputText)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonEncryptTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEncryptTextActionPerformed
        byte[] bytesToEncrypt = jTextAreaInputText.getText().getBytes();
        byte[] encryptedBytes = cipher(bytesToEncrypt);
        
        jTextAreaOutputText.setText(new String(encryptedBytes));
        jTextAreaOutputText.setInternalBuffer(encryptedBytes);
    }//GEN-LAST:event_jButtonEncryptTextActionPerformed

    private void jButtonEncryptFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEncryptFileActionPerformed
        FileContent fileContent;
        try {
            fileContent = retrieveFileContent();
            if (fileContent == null) {
                return;
            }

            File outputFile = new File(fileContent.getFilePath() + "/"
                    + fileContent.getFileName() + "_encrypted." + fileContent.getFileExtension());

            FileUtils.writeByteArrayToFile(outputFile, cipher(fileContent.getBinaryConent()));
        } catch (IOException ex) {
            Logger.getLogger(AlgorithmGUI.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "Wystąpił błąd podczas wczytywania pliku");
        }
    }//GEN-LAST:event_jButtonEncryptFileActionPerformed

    private void jButtonDecryptTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDecryptTextActionPerformed
        byte[] bytesToDecrypt = jTextAreaOutputText.getInternalBuffer();
        jTextAreaInputText.setText(new String(decrypt(bytesToDecrypt)));
    }//GEN-LAST:event_jButtonDecryptTextActionPerformed

    private void jButtonDecryptFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDecryptFileActionPerformed
        FileContent fileContent;
        try {
            JOptionPane.showMessageDialog(this, "Proszę wskazać plik do odszyfrowania");
            fileContent = retrieveFileContent();
            if (fileContent == null) {
                return;
            }

            File outputFile = new File(fileContent.getFilePath() + "/"
                    + fileContent.getFileName().replace("_encrypted", "_decrypted.") + fileContent.getFileExtension());

            FileUtils.writeByteArrayToFile(outputFile, decrypt(fileContent.getBinaryConent()));
            JOptionPane.showMessageDialog(this, "Plik po deszyfracji: " + outputFile.getAbsolutePath());
        } catch (IOException ex) {
            Logger.getLogger(AlgorithmGUI.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "Wystąpił błąd podczas wczytywania pliku");
        }
    }//GEN-LAST:event_jButtonDecryptFileActionPerformed

    private byte[] cipher(byte[] bytesToEncrypt) {
        plainTextBytesLength = bytesToEncrypt.length;
        converter = new BytesToBigNumsConverter(bytesToEncrypt);
        BigNum[] plainText = converter.convert();

        // Encrypt plain text
        BigNum[] cipherText = new BigNum[plainText.length];
        for (int i = 0; i < plainText.length; ++i) {
            cipherText[i] = new BigNum(plainText[i]);
            cipherText[i].multiply(cipherText[i]);
            cipherText[i].modulo(publicKey);
        }

        return BigNumsToBytesConverter.bigNumArrayToBytes(cipherText);
    }

    private byte[] decrypt(byte[] toDecrypt) {
        BigNum[] cipherText = BytesToBigNumsConverter.convertCipherTextToBigNum(toDecrypt);

        // Decrypt ciphertext
        BigNum exponentP = new BigNum(p);
        exponentP.add(BigNum.ONE);
        exponentP.shiftRight(2);

        BigNum exponentQ = new BigNum(q);
        exponentQ.add(BigNum.ONE);
        exponentQ.shiftRight(2);

        // extended Euclidean algorithm
        // we search for yP and yQ such that:
        // yP * p + yQ * q = 1
        Pair solution = EuclideanSolver.getInstance().solve(p, q);
        BigNum yP = solution.first;
        BigNum yQ = solution.second;

        // we calculate the remainder modulo public key because yP and yQ are
        // used only as a factor in equations calculated modulo public key
        // that way we make sure that all partial result will not overflow
        yP.modulo(publicKey);
        yQ.modulo(publicKey);

        BigNum[] decryptedText = new BigNum[cipherText.length];
        byte[] decryptedChunkBytes;
        byte[] decryptedBytes = new byte[plainTextBytesLength];
        int counter = 0;
        int bytesCounter = 0;
        
        for (BigNum encryptedCharacter : cipherText) {
            BigNum squareP = new BigNum(encryptedCharacter);
            squareP.powerModulo(exponentP, p);

            BigNum squareQ = new BigNum(encryptedCharacter);
            squareQ.powerModulo(exponentQ, q);

            BigNum tempP = new BigNum(p);
            tempP.multiply(squareQ);
            tempP.modulo(publicKey);
            tempP.multiply(yP);
            tempP.modulo(publicKey);

            BigNum tempQ = new BigNum(q);
            tempQ.multiply(squareP);
            tempQ.modulo(publicKey);
            tempQ.multiply(yQ);
            tempQ.modulo(publicKey);

            decryptedText[counter++] = checkPossibleTexts(publicKey, tempP, tempQ);

            boolean lastChunk = false;
            if (counter - 1 == cipherText.length - 1) {
                lastChunk = true;
            }
            
            decryptedChunkBytes = BigNumsToBytesConverter.convertChunk(decryptedText[counter - 1], lastChunk);
            for (int i = 0; i < decryptedChunkBytes.length; i++) {
                decryptedBytes[bytesCounter++] = decryptedChunkBytes[i];
            }
        }

        return decryptedBytes;
    }

    private BigNum checkPossibleTexts(BigNum publicKey, BigNum tempP, BigNum tempQ) {
        // there are 4 possible solutions of decryption
        BigNum[] possibleText = new BigNum[4];

        possibleText[0] = new BigNum(tempP);
        possibleText[0].add(tempQ);
        possibleText[0].modulo(publicKey);

        possibleText[1] = new BigNum(publicKey);
        possibleText[1].subtract(possibleText[0]);

        possibleText[2] = new BigNum(tempP);
        possibleText[2].subtract(tempQ);
        possibleText[2].modulo(publicKey);

        possibleText[3] = new BigNum(publicKey);
        possibleText[3].subtract(possibleText[2]);

        for (int i = 0; i < 4; ++i) {
            long hash = BytesToBigNumsConverter.calculateHash(possibleText[i], BytesToBigNumsConverter.BLOCKS_PER_CHUNK);
            long firstHashBlock = hash >>> BigNum.BLOCK_SIZE;
            long secondHashBlock = (hash << BigNum.BLOCK_SIZE) >>> BigNum.BLOCK_SIZE;

            if (possibleText[i].getBlock(BigNum.BLOCKS - 2) == firstHashBlock
                    && possibleText[i].getBlock(BigNum.BLOCKS - 1) == secondHashBlock) {
                return possibleText[i];
            }
        }
        
        return BigNum.ZERO;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AlgorithmGUI.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AlgorithmGUI.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AlgorithmGUI.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AlgorithmGUI.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        AlgorithmGUI gui = new AlgorithmGUI();
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                gui.setVisible(true);
            }
        });
        gui.initKey();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private pl.kryptografia.rabin.ui.EncryptedTextArea encryptedTextArea1;
    private javax.swing.JButton jButtonDecryptFile;
    private javax.swing.JButton jButtonDecryptText;
    private javax.swing.JButton jButtonEncryptFile;
    private javax.swing.JButton jButtonEncryptText;
    private javax.swing.JLabel jLabelInputText;
    private javax.swing.JLabel jLabelOutputText;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTextArea jTextAreaInputText;
    private pl.kryptografia.rabin.ui.EncryptedTextArea jTextAreaOutputText;
    // End of variables declaration//GEN-END:variables
}
