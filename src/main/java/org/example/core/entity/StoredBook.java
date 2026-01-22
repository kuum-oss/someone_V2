package org.example.core.entity;

/**
 * Сущность для хранения данных о книге в базе данных.
 */
public class StoredBook {
    private Integer id;
    private Integer userId;
    private String title;
    private String filePath;
    private String originalName;
    private long fileSize;
    private byte[] fileContent;
    private boolean isPublic;

    private String author;
    private String genre;
    private String year;
    private String series;
    private Integer seriesIndex;
    private String language;
    private String description;
    private byte[] cover;
    private byte[] authorPhoto;
    private String format; // <--- ДОБАВЛЕНО: Поле для формата (PDF, EPUB и т.д.)

    public StoredBook(Integer id, Integer userId, String title, String filePath, String originalName, long fileSize) {
        this(id, userId, title, filePath, originalName, fileSize, null, false);
    }

    public StoredBook(Integer id, Integer userId, String title, String filePath, String originalName, long fileSize, byte[] fileContent) {
        this(id, userId, title, filePath, originalName, fileSize, fileContent, false);
    }

    public StoredBook(Integer id, Integer userId, String title, String filePath, String originalName, long fileSize, byte[] fileContent, boolean isPublic) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.filePath = filePath;
        this.originalName = originalName;
        this.fileSize = fileSize;
        this.fileContent = fileContent;
        this.isPublic = isPublic;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer id;
        private Integer userId;
        private String title;
        private String filePath;
        private String originalName;
        private long fileSize;
        private byte[] fileContent;
        private boolean isPublic;
        private String author;
        private String genre;
        private String year;
        private String series;
        private Integer seriesIndex;
        private String language;
        private String description;
        private byte[] cover;
        private byte[] authorPhoto;
        private String format; // <--- ДОБАВЛЕНО

        public Builder id(Integer id) { this.id = id; return this; }
        public Builder userId(Integer userId) { this.userId = userId; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder filePath(String filePath) { this.filePath = filePath; return this; }
        public Builder originalName(String originalName) { this.originalName = originalName; return this; }
        public Builder fileSize(long fileSize) { this.fileSize = fileSize; return this; }
        public Builder fileContent(byte[] fileContent) { this.fileContent = fileContent; return this; }
        public Builder isPublic(boolean isPublic) { this.isPublic = isPublic; return this; }
        public Builder author(String author) { this.author = author; return this; }
        public Builder genre(String genre) { this.genre = genre; return this; }
        public Builder year(String year) { this.year = year; return this; }
        public Builder series(String series) { this.series = series; return this; }
        public Builder seriesIndex(Integer seriesIndex) { this.seriesIndex = seriesIndex; return this; }
        public Builder language(String language) { this.language = language; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder cover(byte[] cover) { this.cover = cover; return this; }
        public Builder authorPhoto(byte[] authorPhoto) { this.authorPhoto = authorPhoto; return this; }

        // Метод для установки формата
        public Builder format(String format) {
            this.format = format;
            return this;
        }

        public StoredBook build() {
            StoredBook sb = new StoredBook(id, userId, title, filePath, originalName, fileSize, fileContent, isPublic);
            sb.author = author;
            sb.genre = genre;
            sb.year = year;
            sb.series = series;
            sb.seriesIndex = seriesIndex;
            sb.language = language;
            sb.description = description;
            sb.cover = cover;
            sb.authorPhoto = authorPhoto;
            sb.format = format; // <--- ДОБАВЛЕНО: Присвоение формата при сборке
            return sb;
        }
    }

    // Геттеры
    public Integer getId() { return id; }
    public Integer getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getFilePath() { return filePath; }
    public String getOriginalName() { return originalName; }
    public long getFileSize() { return fileSize; }
    public byte[] getFileContent() { return fileContent; }
    public boolean isPublic() { return isPublic; }
    public String getAuthor() { return author; }
    public String getGenre() { return genre; }
    public String getYear() { return year; }
    public String getSeries() { return series; }
    public Integer getSeriesIndex() { return seriesIndex; }
    public String getLanguage() { return language; }
    public String getDescription() { return description; }
    public byte[] getCover() { return cover; }
    public byte[] getAuthorPhoto() { return authorPhoto; }

    // Геттер для формата
    public String getFormat() { return format; }
}