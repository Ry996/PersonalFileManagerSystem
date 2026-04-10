# 个人文件分类管理系统

📁 一个简洁易用的 Java Swing 桌面文件分类管理工具。

## 项目简介

这是一款基于 Java Swing + MySQL 的桌面应用程序，帮助用户高效管理和组织个人文件。支持多级分类、文件导入、重命名、移动、软删除（回收站）等核心功能，界面采用现代化的深蓝色主题设计。

## 功能特性

### 📂 分类管理
- 支持多级分类结构（顶级分类 → 子分类）
- 默认分类：文档、图片、视频、未分类
- 子分类：工作文档、个人文档、照片、截图、电影、教程
- 动态创建、编辑、删除分类
- 分类树实时显示文件数量

### 📄 文件管理
- 批量导入文件/文件夹
- 支持文件重命名（自动同步文件系统）
- 移动文件到指定分类
- 软删除机制（移入回收站）
- 彻底删除（从磁盘移除）
- 恢复误删文件
- 快速搜索文件名

### 🎨 界面特色
- 深蓝色专业主题
- 统一的菜单栏、工具栏、状态栏样式
- 斑马纹表格设计
- 自定义表头样式
- 右键快捷菜单
- 悬停按钮效果
- 文件大小智能格式化

## 技术栈

| 技术 | 说明 |
|------|------|
| Java | JDK 17 |
| Swing | GUI 框架 |
| MySQL | 数据库存储 |
| Maven | 项目构建 |
| JDBC | 数据库连接 |

## 项目结构

```
PersonalFileManagerSystem/
├── src/main/java/org/example/filemanager/
│   ├── FileManagerApp.java    # 主窗口应用
│   ├── DatabaseManager.java   # 数据库操作
│   ├── FileService.java       # 文件处理服务
│   ├── DBUtil.java           # 数据库连接工具
│   └── Models.java            # 数据模型
├── src/main/resources/        # 资源目录
├── init_database.sql          # 数据库初始化脚本
├── pom.xml                    # Maven 配置
└── README.md                  # 项目文档
```

## 快速开始

### 环境要求
- JDK 17 或更高版本
- MySQL 8.0 或更高版本
- Maven 3.6+

### 数据库配置

1. 登录 MySQL：
```bash
mysql -u root -p
```

2. 创建数据库：
```sql
CREATE DATABASE file_manager CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. 修改 `src/main/java/org/example/filemanager/DBUtil.java` 中的数据库配置：
```java
private static final String URL = "jdbc:mysql://localhost:3306/file_manager?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8";
private static final String USER = "root";
private static final String PASSWORD = "your_password";  // 修改为你的密码
```

### 编译运行

```bash
# 编译项目
mvn compile

# 直接运行
mvn exec:java

# 打包为可执行 JAR
mvn package

# 运行打包后的 JAR
java -jar target/filemanager-1.0-SNAPSHOT.jar
```

## 使用指南

### 基本操作

| 操作 | 快捷键 | 说明 |
|------|--------|------|
| 导入文件 | `Ctrl+I` | 选择文件或文件夹导入 |
| 新建分类 | `Ctrl+N` | 创建新的分类 |
| 重命名 | `F2` | 重命名选中的文件 |
| 移动文件 | `Ctrl+M` | 将文件移动到其他分类 |
| 删除 | `Delete` | 将文件移至回收站 |
| 搜索 | `Ctrl+F` | 按文件名搜索文件 |
| 刷新 | `F5` | 刷新文件列表 |
| 回收站 | `Ctrl+R` | 打开回收站 |

### 分类管理
- **添加子分类**：右键分类树节点 → "添加子分类"
- **重命名分类**：右键分类树节点 → "重命名"
- **删除分类**：右键分类树节点 → "删除"（文件将变为未分类）

### 文件操作
- **双击文件**：使用系统默认程序打开文件
- **右键菜单**：打开、重命名、移动、删除

## 数据库表结构

### categories（分类表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT | 主键 |
| name | VARCHAR(100) | 分类名称 |
| parent_id | INT | 父分类ID |
| is_deleted | TINYINT | 是否删除 |
| created_at | TIMESTAMP | 创建时间 |

### files（文件表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT | 主键 |
| file_path | VARCHAR(500) | 文件路径 |
| file_name | VARCHAR(255) | 文件名 |
| file_size | BIGINT | 文件大小 |
| category_id | INT | 所属分类 |
| is_deleted | TINYINT | 是否删除 |
| deleted_at | TIMESTAMP | 删除时间 |
| created_at | TIMESTAMP | 导入时间 |

### import_records（导入记录表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT | 主键 |
| file_path | VARCHAR(500) | 导入路径 |
| imported_at | TIMESTAMP | 导入时间 |

## 界面预览

```
┌──────────────────────────────────────────────────────────────┐
│ 📁 个人文件分类管理系统                                         │
├──────────────────────────────────────────────────────────────┤
│ [导入] [新建分类] [删除] [重命名] [回收站]    [🔍 搜索________] [搜索] │
├─────────────┬────────────────────────────────────────────────┤
│ 📁 分类目录   │ 📋 文件列表                                    │
│              ├────────────┬──────────────┬───────┬──────────┤
│ ▸ 全部文件(32)│ 文件名       │ 路径          │ 大小   │ 分类      │
│   ▸ 文档(8)   ├────────────┼──────────────┼───────┼──────────┤
│     ├工作文档 │ report.pdf  │ C:/Docs/...  │ 2.0 MB│ 工作文档  │
│     └个人文档 │ notes.txt   │ C:/Docs/...  │ 100 KB│ 个人文档  │
│   ▸ 图片(9)   │ wallpaper.. │ C:/Images/.. │ 3.0 MB│ 图片     │
│     ├照片     │ vacation_..  │ C:/Photos/.. │ 4.0 MB│ 照片     │
│     └截图     │ screenshot.. │ C:/Screensh..│ 2.0 MB│ 截图     │
│   ▸ 视频(14)  │             │              │       │          │
│     ├电影     │             │              │       │          │
│     └教程     │             │              │       │          │
│   未分类(1)   │             │              │       │          │
├─────────────┴────────────────────────────────────────────────┤
│ 就绪                    文件数: 32    存储: 1.5 GB    分类: 全部 │
└──────────────────────────────────────────────────────────────┘
```

## 开发说明

### 配色方案
```java
PRIMARY_COLOR = #2C3E50  // 深蓝色（主色调）
ACCENT_COLOR  = #3498DB  // 天蓝色（强调色）
SUCCESS_COLOR = #27AE60  // 绿色（成功）
WARNING_COLOR = #E74C3C  // 红色（警告/删除）
BG_COLOR      = #ECF0F1  // 浅灰色（背景）
CARD_COLOR    = #FFFFFF  // 白色（卡片）
```

### 字体配置
- 标题/菜单：`微软雅黑`, 粗体, 13-14px
- 正文/表格：`微软雅黑`, 常规, 12px

## 注意事项

1. **数据安全**：删除文件时采用软删除机制，文件会先移入回收站
2. **彻底删除**：从回收站删除才会真正从磁盘移除文件
3. **分类删除**：删除分类后，该分类下的文件会变为"未分类"状态
4. **数据库备份**：重要数据请定期备份 MySQL 数据库

## License

MIT License
