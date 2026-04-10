package org.example.filemanager;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库管理器 - 处理所有数据库操作
 */
public class DatabaseManager {
    
    /**
     * 初始化数据库（创建表和默认分类）
     */
    public static void initializeDatabase() {
        String createCategoriesTable = """
            CREATE TABLE IF NOT EXISTS categories (
                id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                parent_id INT DEFAULT NULL,
                is_deleted TINYINT(1) DEFAULT 0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL
            )
        """;
        
        String createFilesTable = """
            CREATE TABLE IF NOT EXISTS files (
                id INT AUTO_INCREMENT PRIMARY KEY,
                file_path VARCHAR(500) NOT NULL UNIQUE,
                file_name VARCHAR(255) NOT NULL,
                file_size BIGINT DEFAULT 0,
                category_id INT DEFAULT NULL,
                is_deleted TINYINT(1) DEFAULT 0,
                deleted_at TIMESTAMP NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
            )
        """;
        
        String createImportRecordsTable = """
            CREATE TABLE IF NOT EXISTS import_records (
                id INT AUTO_INCREMENT PRIMARY KEY,
                file_path VARCHAR(500) NOT NULL,
                imported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;
        
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(createCategoriesTable);
            System.out.println("分类表创建/检查成功");
            
            stmt.execute(createFilesTable);
            System.out.println("文件表创建/检查成功");
            
            stmt.execute(createImportRecordsTable);
            System.out.println("导入记录表创建/检查成功");
            
            // 检查是否需要初始化默认分类（只在分类为空时初始化）
            if (getAllCategories().isEmpty()) {
                initializeDefaultCategories();
            }
            
        } catch (SQLException e) {
            System.err.println("数据库初始化失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 初始化默认分类
     */
    private static void initializeDefaultCategories() {
        // 顶级分类
        addCategory("文档", null);
        addCategory("图片", null);
        addCategory("视频", null);
        addCategory("未分类", null);
        
        // 文档 子分类
        int docsId = getCategoryIdByName("文档");
        if (docsId > 0) {
            addCategory("工作文档", docsId);
            addCategory("个人文档", docsId);
        }
        
        // 图片 子分类
        int imagesId = getCategoryIdByName("图片");
        if (imagesId > 0) {
            addCategory("照片", imagesId);
            addCategory("截图", imagesId);
        }
        
        // 视频 子分类
        int videosId = getCategoryIdByName("视频");
        if (videosId > 0) {
            addCategory("电影", videosId);
            addCategory("教程", videosId);
        }
        
        System.out.println("默认分类初始化完成");
    }
    
    // ==================== 分类操作 ====================
    
    /**
     * 获取所有分类（用于树形结构）
     */
    public static List<Models.Category> getAllCategories() {
        List<Models.Category> categories = new ArrayList<>();
        String sql = "SELECT c.*, COUNT(f.id) as file_count " +
                     "FROM categories c " +
                     "LEFT JOIN files f ON c.id = f.category_id AND f.is_deleted = 0 " +
                     "WHERE c.is_deleted = 0 " +
                     "GROUP BY c.id " +
                     "ORDER BY CASE WHEN c.parent_id IS NULL THEN 0 ELSE 1 END, c.parent_id, c.id";
        
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Models.Category cat = extractCategoryFromResultSet(rs);
                categories.add(cat);
            }
        } catch (SQLException e) {
            System.err.println("获取分类列表失败：" + e.getMessage());
        }
        return categories;
    }
    
    /**
     * 获取分类ID（按名称）
     */
    public static int getCategoryIdByName(String name) {
        String sql = "SELECT id FROM categories WHERE name = ? AND is_deleted = 0";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.err.println("获取分类ID失败：" + e.getMessage());
        }
        return -1;
    }
    
    /**
     * 添加分类
     */
    public static int addCategory(String name, Integer parentId) {
        String sql = "INSERT INTO categories (name, parent_id) VALUES (?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, name);
            if (parentId != null) {
                pstmt.setInt(2, parentId);
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("添加分类失败：" + e.getMessage());
        }
        return -1;
    }
    
    /**
     * 更新分类名称
     */
    public static boolean updateCategoryName(int categoryId, String newName) {
        String sql = "UPDATE categories SET name = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newName);
            pstmt.setInt(2, categoryId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("更新分类失败：" + e.getMessage());
            return false;
        }
    }
    
    /**
     * 删除分类（软删除）
     */
    public static boolean deleteCategory(int categoryId) {
        String sql = "UPDATE categories SET is_deleted = 1 WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, categoryId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("删除分类失败：" + e.getMessage());
            return false;
        }
    }
    
    /**
     * 从ResultSet提取分类对象
     */
    private static Models.Category extractCategoryFromResultSet(ResultSet rs) throws SQLException {
        Models.Category cat = new Models.Category();
        cat.setId(rs.getInt("id"));
        cat.setName(rs.getString("name"));
        int parentId = rs.getInt("parent_id");
        cat.setParentId(rs.wasNull() ? null : parentId);
        cat.setDeleted(rs.getBoolean("is_deleted"));
        cat.setCreatedAt(rs.getTimestamp("created_at"));
        cat.setFileCount(rs.getInt("file_count"));
        return cat;
    }
    
    // ==================== 文件操作 ====================
    
    /**
     * 获取所有文件
     */
    public static List<Models.FileInfo> getAllFiles() {
        List<Models.FileInfo> files = new ArrayList<>();
        String sql = """
            SELECT f.*, c.name as category_name 
            FROM files f 
            LEFT JOIN categories c ON f.category_id = c.id 
            WHERE f.is_deleted = 0 
            ORDER BY f.created_at DESC
        """;
        
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                files.add(extractFileInfoFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("获取文件列表失败：" + e.getMessage());
        }
        return files;
    }
    
    /**
     * 获取指定分类的文件
     */
    public static List<Models.FileInfo> getFilesByCategory(int categoryId) {
        List<Models.FileInfo> files = new ArrayList<>();
        String sql = """
            SELECT f.*, c.name as category_name 
            FROM files f 
            LEFT JOIN categories c ON f.category_id = c.id 
            WHERE f.category_id = ? AND f.is_deleted = 0 
            ORDER BY f.created_at DESC
        """;
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, categoryId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                files.add(extractFileInfoFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("获取分类文件失败：" + e.getMessage());
        }
        return files;
    }
    
    /**
     * 获取已删除文件（回收站）
     */
    public static List<Models.FileInfo> getDeletedFiles() {
        List<Models.FileInfo> files = new ArrayList<>();
        String sql = """
            SELECT f.*, c.name as category_name 
            FROM files f 
            LEFT JOIN categories c ON f.category_id = c.id 
            WHERE f.is_deleted = 1 
            ORDER BY f.deleted_at DESC
        """;
        
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                files.add(extractFileInfoFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("获取回收站文件失败：" + e.getMessage());
        }
        return files;
    }
    
    /**
     * 搜索文件
     */
    public static List<Models.FileInfo> searchFiles(String keyword) {
        List<Models.FileInfo> files = new ArrayList<>();
        String sql = """
            SELECT f.*, c.name as category_name 
            FROM files f 
            LEFT JOIN categories c ON f.category_id = c.id 
            WHERE f.is_deleted = 0 AND f.file_name LIKE ? 
            ORDER BY f.created_at DESC
        """;
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                files.add(extractFileInfoFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("搜索文件失败：" + e.getMessage());
        }
        return files;
    }
    
    /**
     * 添加文件记录
     */
    public static int addFile(String filePath, String fileName, long fileSize, Integer categoryId) {
        String sql = "INSERT INTO files (file_path, file_name, file_size, category_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, filePath);
            pstmt.setString(2, fileName);
            pstmt.setLong(3, fileSize);
            if (categoryId != null) {
                pstmt.setInt(4, categoryId);
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("添加文件记录失败：" + e.getMessage());
        }
        return -1;
    }
    
    /**
     * 更新文件信息
     */
    public static boolean updateFile(Models.FileInfo file) {
        String sql = "UPDATE files SET file_path = ?, file_name = ?, category_id = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, file.getFilePath());
            pstmt.setString(2, file.getFileName());
            if (file.getCategoryId() != null) {
                pstmt.setInt(3, file.getCategoryId());
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }
            pstmt.setInt(4, file.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("更新文件失败：" + e.getMessage());
            return false;
        }
    }
    
    /**
     * 重命名文件（更新数据库）
     */
    public static boolean renameFile(int fileId, String newFileName, String newFilePath) {
        String sql = "UPDATE files SET file_name = ?, file_path = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newFileName);
            pstmt.setString(2, newFilePath);
            pstmt.setInt(3, fileId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("重命名文件失败：" + e.getMessage());
            return false;
        }
    }
    
    /**
     * 移动文件到分类
     */
    public static boolean moveFileToCategory(int fileId, Integer categoryId) {
        String sql = "UPDATE files SET category_id = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            if (categoryId != null) {
                pstmt.setInt(1, categoryId);
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }
            pstmt.setInt(2, fileId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("移动文件失败：" + e.getMessage());
            return false;
        }
    }
    
    /**
     * 软删除文件
     */
    public static boolean softDeleteFile(int fileId) {
        String sql = "UPDATE files SET is_deleted = 1, deleted_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, fileId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("删除文件失败：" + e.getMessage());
            return false;
        }
    }
    
    /**
     * 恢复文件
     */
    public static boolean restoreFile(int fileId) {
        String sql = "UPDATE files SET is_deleted = 0, deleted_at = NULL WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, fileId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("恢复文件失败：" + e.getMessage());
            return false;
        }
    }
    
    /**
     * 彻底删除文件
     */
    public static boolean permanentlyDeleteFile(int fileId) {
        String sql = "DELETE FROM files WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, fileId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("彻底删除文件失败：" + e.getMessage());
            return false;
        }
    }
    
    /**
     * 从ResultSet提取文件信息
     */
    private static Models.FileInfo extractFileInfoFromResultSet(ResultSet rs) throws SQLException {
        Models.FileInfo file = new Models.FileInfo();
        file.setId(rs.getInt("id"));
        file.setFilePath(rs.getString("file_path"));
        file.setFileName(rs.getString("file_name"));
        file.setFileSize(rs.getLong("file_size"));
        int categoryId = rs.getInt("category_id");
        file.setCategoryId(rs.wasNull() ? null : categoryId);
        file.setCategoryName(rs.getString("category_name"));
        file.setDeleted(rs.getBoolean("is_deleted"));
        file.setDeletedAt(rs.getTimestamp("deleted_at"));
        file.setCreatedAt(rs.getTimestamp("created_at"));
        return file;
    }
    
    // ==================== 导入记录 ====================
    
    /**
     * 添加导入记录
     */
    public static void addImportRecord(String filePath) {
        String sql = "INSERT INTO import_records (file_path) VALUES (?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, filePath);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("添加导入记录失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取导入记录
     */
    public static List<Models.ImportRecord> getImportRecords() {
        List<Models.ImportRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM import_records ORDER BY imported_at DESC LIMIT 100";
        
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Models.ImportRecord record = new Models.ImportRecord();
                record.setId(rs.getInt("id"));
                record.setFilePath(rs.getString("file_path"));
                record.setImportedAt(rs.getTimestamp("imported_at"));
                records.add(record);
            }
        } catch (SQLException e) {
            System.err.println("获取导入记录失败：" + e.getMessage());
        }
        return records;
    }
    
    // ==================== 统计信息 ====================
    
    /**
     * 获取总文件数
     */
    public static int getTotalFileCount() {
        String sql = "SELECT COUNT(*) FROM files WHERE is_deleted = 0";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("获取文件总数失败：" + e.getMessage());
        }
        return 0;
    }
    
    /**
     * 获取总存储大小
     */
    public static long getTotalStorageSize() {
        String sql = "SELECT COALESCE(SUM(file_size), 0) FROM files WHERE is_deleted = 0";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println("获取存储大小失败：" + e.getMessage());
        }
        return 0;
    }
    
    /**
     * 清空数据库
     */
    public static void clearDatabase() {
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            stmt.execute("TRUNCATE TABLE import_records");
            stmt.execute("TRUNCATE TABLE files");
            stmt.execute("TRUNCATE TABLE categories");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
            
            System.out.println("数据库已清空");
        } catch (SQLException e) {
            System.err.println("清空数据库失败：" + e.getMessage());
        }
    }
    
    /**
     * 检查文件是否已存在
     */
    public static boolean fileExists(String filePath) {
        String sql = "SELECT COUNT(*) FROM files WHERE file_path = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, filePath);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("检查文件存在失败：" + e.getMessage());
        }
        return false;
    }
}
