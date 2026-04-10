package org.example.filemanager;

/**
 * 数据模型类 - 包含所有数据结构
 */
public class Models {
    
    /**
     * 分类模型
     */
    public static class Category {
        private int id;
        private String name;
        private Integer parentId;
        private boolean isDeleted;
        private java.sql.Timestamp createdAt;
        private int fileCount;
        
        public Category() {}
        
        public Category(int id, String name, Integer parentId) {
            this.id = id;
            this.name = name;
            this.parentId = parentId;
            this.isDeleted = false;
        }
        
        // Getters and Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Integer getParentId() { return parentId; }
        public void setParentId(Integer parentId) { this.parentId = parentId; }
        
        public boolean isDeleted() { return isDeleted; }
        public void setDeleted(boolean deleted) { isDeleted = deleted; }
        
        public java.sql.Timestamp getCreatedAt() { return createdAt; }
        public void setCreatedAt(java.sql.Timestamp createdAt) { this.createdAt = createdAt; }
        
        public int getFileCount() { return fileCount; }
        public void setFileCount(int fileCount) { this.fileCount = fileCount; }
        
        @Override
        public String toString() {
            return name + " (" + fileCount + ")";
        }
    }
    
    /**
     * 文件信息模型
     */
    public static class FileInfo {
        private int id;
        private String filePath;
        private String fileName;
        private long fileSize;
        private Integer categoryId;
        private String categoryName;
        private boolean isDeleted;
        private java.sql.Timestamp deletedAt;
        private java.sql.Timestamp createdAt;
        
        public FileInfo() {}
        
        public FileInfo(int id, String filePath, String fileName, long fileSize, 
                        Integer categoryId, boolean isDeleted) {
            this.id = id;
            this.filePath = filePath;
            this.fileName = fileName;
            this.fileSize = fileSize;
            this.categoryId = categoryId;
            this.isDeleted = isDeleted;
        }
        
        // Getters and Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public long getFileSize() { return fileSize; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }
        
        public Integer getCategoryId() { return categoryId; }
        public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
        
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        
        public boolean isDeleted() { return isDeleted; }
        public void setDeleted(boolean deleted) { isDeleted = deleted; }
        
        public java.sql.Timestamp getDeletedAt() { return deletedAt; }
        public void setDeletedAt(java.sql.Timestamp deletedAt) { this.deletedAt = deletedAt; }
        
        public java.sql.Timestamp getCreatedAt() { return createdAt; }
        public void setCreatedAt(java.sql.Timestamp createdAt) { this.createdAt = createdAt; }
        
        /**
         * 格式化文件大小
         */
        public String getFormattedSize() {
            if (fileSize < 1024) {
                return fileSize + " B";
            } else if (fileSize < 1024 * 1024) {
                return String.format("%.1f KB", fileSize / 1024.0);
            } else if (fileSize < 1024 * 1024 * 1024) {
                return String.format("%.1f MB", fileSize / (1024.0 * 1024));
            } else {
                return String.format("%.2f GB", fileSize / (1024.0 * 1024 * 1024));
            }
        }
    }
    
    /**
     * 导入记录模型
     */
    public static class ImportRecord {
        private int id;
        private String filePath;
        private java.sql.Timestamp importedAt;
        
        public ImportRecord() {}
        
        public ImportRecord(int id, String filePath, java.sql.Timestamp importedAt) {
            this.id = id;
            this.filePath = filePath;
            this.importedAt = importedAt;
        }
        
        // Getters and Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        
        public java.sql.Timestamp getImportedAt() { return importedAt; }
        public void setImportedAt(java.sql.Timestamp importedAt) { this.importedAt = importedAt; }
    }
}
