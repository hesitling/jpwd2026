package org.florious.passwordmanager.repository;

import org.florious.passwordmanager.util.Config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 数据库管理类
 * 负责数据库连接、初始化和表结构管理
 */
public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;
    private final String dbPath;
    private final Object txLock = new Object();

    private DatabaseManager() {
        this.dbPath = Config.getDatabasePath();
        initializeDatabase();
    }

    /**
     * 获取DatabaseManager单例
     * @return DatabaseManager实例
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * 初始化数据库
     * 创建数据库文件和表结构
     */
    private void initializeDatabase() {
        try {
            // 确保数据库目录存在
            Path dbDir = Paths.get(dbPath).getParent();
            if (dbDir != null && !Files.exists(dbDir)) {
                Files.createDirectories(dbDir);
            }

            // 加载SQLite JDBC驱动
            Class.forName("org.sqlite.JDBC");

            // 建立数据库连接
            String url = "jdbc:sqlite:" + dbPath;
            connection = DriverManager.getConnection(url);

            // 启用外键约束
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }

            // 创建表结构
            createTables();

            System.out.println("数据库初始化成功: " + dbPath);

        } catch (ClassNotFoundException e) {
            System.err.println("错误: 未找到SQLite JDBC驱动: " + e.getMessage());
            throw new RuntimeException("数据库驱动加载失败", e);
        } catch (SQLException e) {
            System.err.println("错误: 数据库连接失败: " + e.getMessage());
            throw new RuntimeException("数据库连接失败", e);
        } catch (IOException e) {
            System.err.println("错误: 无法创建数据库目录: " + e.getMessage());
            throw new RuntimeException("数据库目录创建失败", e);
        }
    }

    /**
     * 创建数据库表结构
     */
    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // 创建用户表
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    salt TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    last_login TIMESTAMP
                )
            """);

            // 创建分类表
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS categories (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    color TEXT,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                    UNIQUE(user_id, name)
                )
            """);

            // 创建密码条目表
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS password_entries (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    title TEXT NOT NULL,
                    username TEXT,
                    encrypted_password TEXT NOT NULL,
                    iv TEXT NOT NULL,
                    url TEXT,
                    notes TEXT,
                    category_id INTEGER,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
                )
            """);

            // 创建索引以提高查询性能
            stmt.execute("""
                CREATE INDEX IF NOT EXISTS idx_password_entries_user_id 
                ON password_entries(user_id)
            """);

            stmt.execute("""
                CREATE INDEX IF NOT EXISTS idx_password_entries_category_id 
                ON password_entries(category_id)
            """);

            stmt.execute("""
                CREATE INDEX IF NOT EXISTS idx_categories_user_id 
                ON categories(user_id)
            """);
        }
    }

    /**
     * 获取数据库连接
     * @return 数据库连接
     * @throws SQLException 如果连接失败
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            String url = "jdbc:sqlite:" + dbPath;
            connection = DriverManager.getConnection(url);
            
            // 重新启用外键约束
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
        }
        return connection;
    }

    /**
     * 关闭数据库连接
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("警告: 关闭数据库连接时出错: " + e.getMessage());
            }
        }
    }

    /**
     * 开始事务
     * @throws SQLException 如果事务开始失败
     */
    public void beginTransaction() throws SQLException {
        synchronized (txLock) {
            getConnection().setAutoCommit(false);
        }
    }

    /**
     * 提交事务
     * @throws SQLException 如果提交失败
     */
    public void commitTransaction() throws SQLException {
        synchronized (txLock) {
            try {
                getConnection().commit();
            } finally {
                getConnection().setAutoCommit(true);
            }
        }
    }

    /**
     * 回滚事务
     * @throws SQLException 如果回滚失败
     */
    public void rollbackTransaction() throws SQLException {
        synchronized (txLock) {
            try {
                getConnection().rollback();
            } finally {
                getConnection().setAutoCommit(true);
            }
        }
    }

    /**
     * 检查数据库是否已初始化
     * @return 如果数据库已初始化返回true
     */
    public boolean isInitialized() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * 获取数据库文件路径
     * @return 数据库文件路径
     */
    public String getDbPath() {
        return dbPath;
    }

    /**
     * 删除数据库文件（用于测试）
     * @return 如果删除成功返回true
     */
    public boolean deleteDatabase() {
        closeConnection();
        try {
            Path path = Paths.get(dbPath);
            if (Files.exists(path)) {
                Files.delete(path);
                return true;
            }
            return false;
        } catch (IOException e) {
            System.err.println("警告: 无法删除数据库文件: " + e.getMessage());
            return false;
        }
    }
}