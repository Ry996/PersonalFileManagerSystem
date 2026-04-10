-- ============================================
-- 个人文件分类管理系统 - 数据库初始化脚本
-- ============================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS file_manager CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE file_manager;

-- ============================================
-- 删除旧表（按依赖顺序，先删子表）
-- ============================================
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS file_tags;
DROP TABLE IF EXISTS tags;
DROP TABLE IF EXISTS import_records;
DROP TABLE IF EXISTS files;
DROP TABLE IF EXISTS categories;
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 1. 分类表
-- ============================================
CREATE TABLE categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    parent_id INT DEFAULT NULL,
    is_deleted TINYINT(1) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 2. 文件表
-- ============================================
CREATE TABLE files (
    id INT AUTO_INCREMENT PRIMARY KEY,
    file_path VARCHAR(500) NOT NULL UNIQUE,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT DEFAULT 0,
    category_id INT DEFAULT NULL,
    is_deleted TINYINT(1) DEFAULT 0,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 3. 导入记录表
-- ============================================
CREATE TABLE import_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    file_path VARCHAR(500) NOT NULL,
    imported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 插入默认分类数据
-- ============================================

-- 一级分类
INSERT INTO categories (id, name, parent_id) VALUES
(1, '文档', NULL),
(2, '图片', NULL),
(3, '视频', NULL),
(4, '未分类', NULL);

-- 文档 子分类
INSERT INTO categories (id, name, parent_id) VALUES
(5, '工作文档', 1),
(6, '个人文档', 1);

-- 图片 子分类
INSERT INTO categories (id, name, parent_id) VALUES
(7, '照片', 2),
(8, '截图', 2);

-- 视频 子分类
INSERT INTO categories (id, name, parent_id) VALUES
(9, '电影', 3),
(10, '教程', 3);

-- ============================================
-- 插入样例文件数据
-- ============================================

-- 文档 主分类文件
INSERT INTO files (file_path, file_name, file_size, category_id) VALUES
('C:/Users/pc/Documents/report_2024.pdf', 'report_2024.pdf', 2048000, 1),
('C:/Users/pc/Documents/notes.txt', 'notes.txt', 102400, 1),
('C:/Users/pc/Documents/manual.pdf', 'manual.pdf', 5242880, 1);

-- 工作文档 文件
INSERT INTO files (file_path, file_name, file_size, category_id) VALUES
('C:/Users/pc/Documents/Work/project_plan.docx', 'project_plan.docx', 512000, 5),
('C:/Users/pc/Documents/Work/meeting_notes.docx', 'meeting_notes.docx', 256000, 5),
('C:/Users/pc/Documents/Work/budget.xlsx', 'budget.xlsx', 348160, 5),
('C:/Users/pc/Documents/Work/presentation.pptx', 'presentation.pptx', 1048576, 5);

-- 个人文档 文件
INSERT INTO files (file_path, file_name, file_size, category_id) VALUES
('C:/Users/pc/Documents/Personal/diary.md', 'diary.md', 65536, 6),
('C:/Users/pc/Documents/Personal/recipes.txt', 'recipes.txt', 81920, 6),
('C:/Users/pc/Documents/Personal/travel_plan.pdf', 'travel_plan.pdf', 1536000, 6);

-- 图片 主分类文件
INSERT INTO files (file_path, file_name, file_size, category_id) VALUES
('C:/Users/pc/Images/wallpaper.jpg', 'wallpaper.jpg', 3145728, 2),
('C:/Users/pc/Images/icon.png', 'icon.png', 102400, 2);

-- 照片 文件
INSERT INTO files (file_path, file_name, file_size, category_id) VALUES
('C:/Users/pc/Images/Photos/vacation_001.jpg', 'vacation_001.jpg', 4194304, 7),
('C:/Users/pc/Images/Photos/vacation_002.jpg', 'vacation_002.jpg', 3670016, 7),
('C:/Users/pc/Images/Photos/family_reunion.jpg', 'family_reunion.jpg', 5242880, 7),
('C:/Users/pc/Images/Photos/birthday_party.jpg', 'birthday_party.jpg', 4718592, 7),
('C:/Users/pc/Images/Photos/sunset_beach.jpg', 'sunset_beach.jpg', 6291456, 7);

-- 截图 文件
INSERT INTO files (file_path, file_name, file_size, category_id) VALUES
('C:/Users/pc/Images/Screenshots/desktop_2024.png', 'desktop_2024.png', 2097152, 8),
('C:/Users/pc/Images/Screenshots/error_dialog.png', 'error_dialog.png', 512000, 8),
('C:/Users/pc/Images/Screenshots/stats_graph.png', 'stats_graph.png', 768000, 8),
('C:/Users/pc/Images/Screenshots/code_snippet.png', 'code_snippet.png', 614400, 8);

-- 视频 主分类文件
INSERT INTO files (file_path, file_name, file_size, category_id) VALUES
('C:/Users/pc/Videos/intro.mp4', 'intro.mp4', 15728640, 3);

-- 电影 文件
INSERT INTO files (file_path, file_name, file_size, category_id) VALUES
('C:/Users/pc/Videos/Movies/action_film.mp4', 'action_film.mp4', 1073741824, 9),
('C:/Users/pc/Videos/Movies/comedy_2024.mp4', 'comedy_2024.mp4', 858993459, 9),
('C:/Users/pc/Videos/Movies/documentary.mp4', 'documentary.mp4', 536870912, 9);

-- 教程 文件
INSERT INTO files (file_path, file_name, file_size, category_id) VALUES
('C:/Users/pc/Videos/Tutorials/java_tutorial_part1.mp4', 'java_tutorial_part1.mp4', 314572800, 10),
('C:/Users/pc/Videos/Tutorials/java_tutorial_part2.mp4', 'java_tutorial_part2.mp4', 293601280, 10),
('C:/Users/pc/Videos/Tutorials/web_dev_course.mp4', 'web_dev_course.mp4', 524288000, 10),
('C:/Users/pc/Videos/Tutorials/excel_tips.mp4', 'excel_tips.mp4', 209715200, 10);

-- Uncategorized 文件
INSERT INTO files (file_path, file_name, file_size, category_id) VALUES
('C:/Users/pc/Downloads/unknown_file.zip', 'unknown_file.zip', 10485760, 4),
('C:/Users/pc/Downloads/temp.dat', 'temp.dat', 512000, 4),
('C:/Users/pc/Desktop/random.txt', 'random.txt', 2048, 4);

-- 插入导入记录
INSERT INTO import_records (file_path) VALUES
('C:/Users/pc/Documents/'),
('C:/Users/pc/Images/'),
('C:/Users/pc/Videos/'),
('C:/Users/pc/Downloads/');

-- ============================================
-- 验证数据
-- ============================================
SELECT '=== 分类统计 ===' AS info;
SELECT 
    c.id,
    c.name,
    c.parent_id,
    COUNT(f.id) AS file_count,
    SUM(f.file_size) AS total_size
FROM categories c
LEFT JOIN files f ON c.id = f.category_id AND f.is_deleted = 0
GROUP BY c.id, c.name, c.parent_id
ORDER BY c.id;

SELECT '=== 文件统计 ===' AS info;
SELECT COUNT(*) AS total_files, 
       SUM(file_size) AS total_size,
       SUM(file_size) / 1024 / 1024 AS size_mb
FROM files WHERE is_deleted = 0;
