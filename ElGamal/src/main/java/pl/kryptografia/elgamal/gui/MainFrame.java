/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.kryptografia.elgamal.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import pl.kryptografia.elgamal.signature.SignatureScheme;

/**
 *
 * @author Lukasz Cyran
 */
public class MainFrame extends javax.swing.JFrame {

    private static final Logger logger = Logger.getLogger(MainFrame.class.getName());

    private SignatureScheme signatureScheme;

    /**
     * Creates new form MainFrame
     */
    public MainFrame() {
        initComponents();
        setTitle("Podpis cyfrowy - algorytm ElGamala");
        setLocationRelativeTo(null);
    }

    /**
     * Pozwól użytkownikowi wybrać plik i zwróć informacje o tym pliku.
     *
     * @return Informacje o pełnej nazwie pliku oraz jego zawartości.
     * @throws FileNotFoundException
     * @throws IOException
     */
    private FileContent retrieveFileContent(String fileChooserTitle) throws FileNotFoundException, IOException {
        FileContent result = new FileContent();

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(fileChooserTitle);
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
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButtonSignFile = new javax.swing.JButton();
        jButtonVerifyFile = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jButtonSignFile.setText("Podpisz plik");
        jButtonSignFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSignFileActionPerformed(evt);
            }
        });

        jButtonVerifyFile.setText("Zweryfikuj plik");
        jButtonVerifyFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonVerifyFileActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonSignFile)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 186, Short.MAX_VALUE)
                .addComponent(jButtonVerifyFile)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonSignFile)
                    .addComponent(jButtonVerifyFile))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonVerifyFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonVerifyFileActionPerformed
        FileContent inputFileContent;
        FileContent signatureFileContent;
        
        try {
            inputFileContent = retrieveFileContent("Wybierz plik do sprawdzenia");
            if (inputFileContent == null) {
                return;
            }

            signatureFileContent = retrieveFileContent("Wybierz plik z podpisem");
            if (signatureFileContent == null) {
                return;
            }

            byte[] originalMessage = inputFileContent.getBinaryConent();
            byte[] signature = signatureFileContent.getBinaryConent();

            if (signatureScheme.verify(originalMessage, signature)) {
                JOptionPane.showMessageDialog(this, "Weryfikacja zakończyła się powodzeniem.", "Sukces", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Weryfikacja zakończyła się niepowodzeniem.", "Niepowodzenie", JOptionPane.WARNING_MESSAGE);
            }
            
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "Wystąpił błąd podczas wczytywania pliku.", "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButtonVerifyFileActionPerformed

    private void jButtonSignFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSignFileActionPerformed
        FileContent fileContent;
        try {
            fileContent = retrieveFileContent("Wybierz plik do podpisania");
            if (fileContent == null) {
                return;
            }

            File outputFile = new File(fileContent.getFilePath() + "/"
                    + fileContent.getFileName() + "_signature." + fileContent.getFileExtension());

            FileUtils.writeByteArrayToFile(outputFile, signatureScheme.sign(fileContent.getBinaryConent()));
            
            JOptionPane.showMessageDialog(this, "Plik został pomyślnie podpisany.", "Sukces", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "Wystąpił błąd podczas wczytywania pliku.", "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButtonSignFileActionPerformed

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
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonSignFile;
    private javax.swing.JButton jButtonVerifyFile;
    // End of variables declaration//GEN-END:variables
}