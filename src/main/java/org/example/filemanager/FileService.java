package org.example.filemanager;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件服务 - 处理文件导入、重命名等操作
 */
public class FileService {
    
    /**
     * 导入文件回调接口
     */
    @FunctionalInterface
    public interface ImportProgressCallback {
        void onProgress(int current, int total, String fileName);
    }
    
    /**
     * 批量导入文件
     * @param files 文件数组
     * @param categoryId 分类ID
     * @param callback 进度回调
     * @return 成功导入的文件数
     */
    public static int importFiles(File[] files, Integer categoryId, ImportProgressCallback callback) {
        int successCount = 0;
        int total = files.length;
        
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            
            if (callback != null) {
                callback.onProgress(i + 1, total, file.getName());
            }
            
            if (file.isFile()) {
                if (DatabaseManager.fileExists(file.getAbsolutePath())) {
                    System.out.println("文件已存在，跳过：" + file.getName());
                    continue;
                }
                
                long fileSize = file.length();
                int fileId = DatabaseManager.addFile(
                    file.getAbsolutePath(),
                    file.getName(),
                    fileSize,
                    categoryId
                );
                
                if (fileId > 0) {
                    DatabaseManager.addImportRecord(file.getAbsolutePath());
                    successCount++;
                }
            } else if (file.isDirectory()) {
                // 递归导入文件夹
                successCount += importDirectory(file, categoryId, callback);
            }
        }
        
        return successCount;
    }
    
    /**
     * 递归导入文件夹
     */
    private static int importDirectory(File directory, Integer categoryId, ImportProgressCallback callback) {
        int successCount = 0;
        File[] files = directory.listFiles();
        
        if (files == null || files.length == 0) {
            return 0;
        }
        
        for (File file : files) {
            if (file.isFile()) {
                if (DatabaseManager.fileExists(file.getAbsolutePath())) {
                    continue;
                }
                
                long fileSize = file.length();
                int fileId = DatabaseManager.addFile(
                    file.getAbsolutePath(),
                    file.getName(),
                    fileSize,
                    categoryId
                );
                
                if (fileId > 0) {
                    DatabaseManager.addImportRecord(file.getAbsolutePath());
                    successCount++;
                }
            } else if (file.isDirectory()) {
                successCount += importDirectory(file, categoryId, callback);
            }
        }
        
        return successCount;
    }
    
    /**
     * 重命名文件
     * @param fileId 文件ID
     * @param oldFilePath 原文件路径
     * @param newFileName 新文件名
     * @return 是否成功
     */
    public static boolean renameFile(int fileId, String oldFilePath, String newFileName) {
        File oldFile = new File(oldFilePath);
        
        if (!oldFile.exists()) {
            System.err.println("文件不存在：" + oldFilePath);
            return false;
        }
        
        // 获取新文件路径
        File parentDir = oldFile.getParentFile();
        String newFilePath = new File(parentDir, newFileName).getAbsolutePath();
        
        // 检查新文件名是否已存在
        File newFile = new File(newFilePath);
        if (newFile.exists() && !newFile.getAbsolutePath().equals(oldFilePath)) {
            System.err.println("目标文件名已存在：" + newFileName);
            return false;
        }
        
        // 重命名实际文件
        try {
            Files.move(oldFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("重命名文件失败：" + e.getMessage());
            return false;
        }
        
        // 更新数据库
        return DatabaseManager.renameFile(fileId, newFileName, newFilePath);
    }
    
    /**
     * 移动文件到新分类
     * @param fileId 文件ID
     * @param newCategoryId 新分类ID
     * @return 是否成功
     */
    public static boolean moveFileToCategory(int fileId, Integer newCategoryId) {
        return DatabaseManager.moveFileToCategory(fileId, newCategoryId);
    }
    
    /**
     * 获取文件的默认分类（根据扩展名）
     * @param fileName 文件名
     * @return 分类ID，-1表示未分类
     */
    public static int getDefaultCategoryByExtension(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        
        // 根据扩展名匹配分类
        List<String> documentExts = List.of("doc", "docx", "pdf", "txt", "xls", "xlsx", "ppt", "pptx", "odt", "rtf");
        List<String> imageExts = List.of("jpg", "jpeg", "png", "gif", "bmp", "svg", "webp", "ico", "tiff");
        List<String> videoExts = List.of("mp4", "avi", "mkv", "mov", "wmv", "flv", "webm", "m4v", "mpeg");
        
        if (documentExts.contains(extension)) {
            return DatabaseManager.getCategoryIdByName("文档");
        } else if (imageExts.contains(extension)) {
            return DatabaseManager.getCategoryIdByName("图片");
        } else if (videoExts.contains(extension)) {
            return DatabaseManager.getCategoryIdByName("视频");
        }
        
        return -1; // 未分类
    }
    
    /**
     * 获取文件扩展名
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            return fileName.substring(lastDot + 1);
        }
        return "";
    }
    
    /**
     * 验证文件名是否合法
     */
    public static boolean isValidFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        
        // 检查非法字符
        String illegalChars = "/\\:*?\"<>|";
        for (char c : illegalChars.toCharArray()) {
            if (fileName.indexOf(c) >= 0) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 删除文件（从文件系统）
     */
    public static boolean deleteFileFromSystem(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return file.delete();
        }
        return true; // 文件不存在，视为删除成功
    }
    
    /**
     * 批量软删除文件
     */
    public static int batchSoftDelete(List<Integer> fileIds) {
        int successCount = 0;
        for (Integer fileId : fileIds) {
            if (DatabaseManager.softDeleteFile(fileId)) {
                successCount++;
            }
        }
        return successCount;
    }
    
    /**
     * 批量彻底删除
     */
    public static int batchPermanentDelete(List<Models.FileInfo> files) {
        int successCount = 0;
        for (Models.FileInfo file : files) {
            // 先从文件系统删除
            deleteFileFromSystem(file.getFilePath());
            // 再从数据库删除
            if (DatabaseManager.permanentlyDeleteFile(file.getId())) {
                successCount++;
            }
        }
        return successCount;
    }
    
    /**
     * 批量恢复文件
     */
    public static int batchRestore(List<Integer> fileIds) {
        int successCount = 0;
        for (Integer fileId : fileIds) {
            if (DatabaseManager.restoreFile(fileId)) {
                successCount++;
            }
        }
        return successCount;
    }
    
    /**
     * 格式化存储大小
     */
    public static String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
}
