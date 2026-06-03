package org.florious.passwordmanager.repository;

import org.florious.passwordmanager.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static DatabaseManager instance;
    private Connection connection;
    private final String dbPath;
    private final Object txLock = new Object();

    private DatabaseManager() {
        this.dbPath = Config.getDatabasePath();
        initializeDatabase();
    }

    /**
     * 使用指定路径创建 DatabaseManager（用于测试）
     * @param dbPath 数据库路径，使用 ":memory:" 创建内存数据库
     */
    private DatabaseManager(String dbPath) {
        this.dbPath = dbPath;
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
     * 获取或创建指定路径的 DatabaseManager（用于测试）
     * 注意：会重置单例实例
     * @param dbPath 数据库路径，使用 ":memory:" 创建内存数据库
     * @return DatabaseManager实例
     */
    public static synchronized DatabaseManager getInstance(String dbPath) {
        if (instance == null || !instance.dbPath.equals(dbPath)) {
            if (instance != null) {
                instance.closeConnection();
            }
            instance = new DatabaseManager(dbPath);
        }
        return instance;
    }

    /**
     * 重置单例实例（用于测试清理）
     */
    public static synchronized void resetInstance() {
        if (instance != null) {
            instance.closeConnection();
            instance = null;
        }
    }

    /**
     * 初始化数据库
     * 创建数据库文件和表结构
     */
    private void initializeDatabase() {
        try {
            // 确保数据库目录存在（内存数据库跳过）
            if (!":memory:".equals(dbPath)) {
                Path dbDir = Paths.get(dbPath).getParent();
                if (dbDir != null && !Files.exists(dbDir)) {
                    Files.createDirectories(dbDir);
                }
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

            logger.info("数据库初始化成功: {}", dbPath);

        } catch (ClassNotFoundException e) {
            logger.error("未找到SQLite JDBC驱动", e);
            throw new RuntimeException("数据库驱动加载失败", e);
        } catch (SQLException e) {
            logger.error("数据库连接失败: {}", dbPath, e);
            throw new RuntimeException("数据库连接失败", e);
        } catch (IOException e) {
            logger.error("无法创建数据库目录: {}", dbPath, e);
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
                    vault_salt TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    last_login TIMESTAMP
                )
            """);
            
            // 为旧表添加 vault_salt 列（如果不存在）
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN vault_salt TEXT");
            } catch (SQLException e) {
                // 列已存在，忽略
            }

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
                logger.warn("关闭数据库连接时出错", e);
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