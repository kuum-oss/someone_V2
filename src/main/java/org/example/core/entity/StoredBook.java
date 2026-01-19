package org.example.core.entity;

public class StoredBook {
    private Integer id;
    private Integer userId;
    private String title;
    private String filePath;
    private String originalName;
    private long fileSize;
    private byte[] fileContent;

    public StoredBook(Integer id, Integer userId, String title, String filePath, String originalName, long fileSize) {
        this(id, userId, title, filePath, originalName, fileSize, null);
    }

    public StoredBook(Integer id, Integer userId, String title, String filePath, String originalName, long fileSize, byte[] fileContent) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.filePath = filePath;
        this.originalName = originalName;
        this.fileSize = fileSize;
        this.fileContent = fileContent;
    }

    public Integer getId() { return id; }
    public Integer getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getFilePath() { return filePath; }
    public String getOriginalName() { return originalName; }
    public long getFileSize() { return fileSize; }
    public byte[] getFileContent() { return fileContent; }
}
