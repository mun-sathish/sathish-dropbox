package in.sathish.dropbox.main;

import java.io.Serializable;

/**
 * Created by sathish on 23/9/15.
 */
public class BrowseContentDTO implements Serializable {

    private String fileName;
    private String fileModified;
    private String fileSize;
    private Boolean isFolder;
    private String filePath;
    private int fileImage;

    public String getFileName() { return fileName; }
    public String getFileModified() { return fileModified; }
    public String getFileSize() { return fileSize; }
    public Boolean getIsFolder() { return isFolder; }
    public String getFilePath() { return filePath; }
    public int getFileImage() { return fileImage; }


    public void setFileName(String fileName){ this.fileName = fileName; }
    public void setFileModified(String fileModified){ this.fileModified = fileModified; }
    public void setFileSize(String fileSize){ this.fileSize = fileSize; }
    public void setIsFolder(Boolean isFolder){ this.isFolder = isFolder; }
    public void setFilePath(String filePath){ this.filePath = filePath; }
    public void setFileImage(int fileImage){ this.fileImage = fileImage; }
}
