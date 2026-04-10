package org.example.filemanager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 数据库连接工具类
 */
public class DBUtil {
    
    // 数据库配置
    private static final String URL = "jdbc:mysql://localhost:3306/file_manager?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8";
    private static final String USER = "root";
    private static final String PASSWORD = "123456";
    
    // 驱动加载标记
    private static boolean driverLoaded = false;
    
    /**
     * 加载数据库驱动
     */
    public static void loadDriver() {
        if (!driverLoaded) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                driverLoaded = true;
                System.out.println("数据库驱动加载成功！");
            } catch (ClassNotFoundException e) {
                System.err.println("数据库驱动加载失败：" + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 获取数据库连接
     * @return 数据库连接对象
     */
    public static Connection getConnection() throws SQLException {
        loadDriver();
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    
    /**
     * 关闭数据库连接
     * @param conn 要关闭的连接
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("关闭数据库连接时出错：" + e.getMessage());
            }
        }
    }
    
    /**
     * 测试数据库连接
     * @return 连接是否成功
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("数据库连接测试成功！");
            return true;
        } catch (SQLException e) {
            System.err.println("数据库连接测试失败：" + e.getMessage());
            return false;
        }
    }
}
