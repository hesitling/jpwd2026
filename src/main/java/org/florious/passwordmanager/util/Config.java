package org.florious.passwordmanager.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * 配置管理类
 * 负责加载、保存和管理应用程序配置
 */
public class Config {
    private static final Logger logger = LoggerFactory.getLogger(Config.class);
    private static final String CONFIG_FILE = "config.properties";
    private static final String APP_DIR = ".config/jpwd";
    private static Properties properties;
    private static Path configPath;

    static {
        // 配置文件路径：用户目录/.password-manager/config.properties
        String userHome = System.getProperty("user.home");
        configPath = Paths.get(userHome, APP_DIR, CONFIG_FILE);
        load();
    }

    /**
     * 加载配置文件
     */
    public static void load() {
        properties = new Properties();

        // 确保配置目录存在
        try {
            Path parentDir = configPath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
        } catch (IOException e) {
            logger.warn("无法创建配置目录: {}", configPath.getParent(), e);
        }

        // 首先尝试从类路径加载默认配置
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            logger.warn("无法加载默认配置文件", e);
        }

        // 然后尝试从文件系统加载配置（覆盖默认值）
        if (Files.exists(configPath)) {
            try (InputStream input = Files.newInputStream(configPath)) {
                properties.load(input);
            } catch (IOException e) {
                logger.warn("无法加载配置文件: {}", configPath, e);
            }
        }
    }

    /**
     * 保存配置到文件
     */
    public static void save() {
        try {
            // 确保父目录存在
            Path parentDir = configPath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
            try (OutputStream output = Files.newOutputStream(configPath)) {
                properties.store(output, "密码管理器配置文件");
            }
        } catch (IOException e) {
            logger.error("无法保存配置文件: {}", configPath, e);
        }
    }

    /**
     * 获取字符串配置值
     * @param key 配置键
     * @return 配置值，如果不存在返回null
     */
    public static String get(String key) {
        return properties.getProperty(key);
    }

    /**
     * 获取字符串配置值，如果不存在返回默认值
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值或默认值
     */
    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * 获取整数配置值
     * @param key 配置键
     * @return 配置值，如果不存在或格式错误返回0
     */
    public static int getInt(String key) {
        return getInt(key, 0);
    }

    /**
     * 获取整数配置值，如果不存在返回默认值
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值或默认值
     */
    public static int getInt(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                logger.warn("配置项 {} 不是有效的整数: {}", key, value);
            }
        }
        return defaultValue;
    }

    /**
     * 获取布尔配置值
     * @param key 配置键
     * @return 配置值，如果不存在返回false
     */
    public static boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    /**
     * 获取布尔配置值，如果不存在返回默认值
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值或默认值
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            return Boolean.parseBoolean(value.trim());
        }
        return defaultValue;
    }

    /**
     * 设置配置值
     * @param key 配置键
     * @param value 配置值
     */
    public static void set(String key, String value) {
        properties.setProperty(key, value);
    }

    /**
     * 设置整数配置值
     * @param key 配置键
     * @param value 配置值
     */
    public static void setInt(String key, int value) {
        properties.setProperty(key, String.valueOf(value));
    }

    /**
     * 设置布尔配置值
     * @param key 配置键
     * @param value 配置值
     */
    public static void setBoolean(String key, boolean value) {
        properties.setProperty(key, String.valueOf(value));
    }

    /**
     * 获取数据库文件路径
     * @return 数据库文件完整路径
     */
    public static String getDatabasePath() {
        String path = get("database.path", "./data");
        String name = get("database.name", "password_manager.db");
        return Paths.get(path, name).toString();
    }

    /**
     * 获取会话超时时间（毫秒）
     * @return 超时时间（毫秒）
     */
    public static long getSessionTimeoutMillis() {
        int minutes = getInt("session.timeout", 5);
        return minutes * 60 * 1000L;
    }

    /**
     * 获取剪贴板清除超时时间（毫秒）
     * @return 超时时间（毫秒）
     */
    public static long getClipboardClearTimeoutMillis() {
        int seconds = getInt("clipboard.clear.timeout", 30);
        return seconds * 1000L;
    }

    /**
     * 获取剪贴板清除延迟时间（秒）
     * @return 延迟时间（秒）
     */
    public static int getClipboardClearDelaySeconds() {
        return getInt("clipboard.clear.timeout", 30);
    }
}