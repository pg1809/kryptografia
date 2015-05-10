package pl.kryptografia.rabin.ui;

/**
 *
 * @author Wojciech Szałapski
 */
public class FileContent {

    private byte[] binaryConent;

    private String filePath;

    private String fileName;

    private String fileExtension;

    public byte[] getBinaryConent() {
        return binaryConent;
    }

    public void setBinaryConent(byte[] binaryConent) {
        this.binaryConent = binaryConent;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }
}
