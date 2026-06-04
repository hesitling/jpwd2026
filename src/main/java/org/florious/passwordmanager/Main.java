package org.florious.passwordmanager;

import org.florious.passwordmanager.repository.DatabaseManager;
import org.florious.passwordmanager.ui.MainFrame;
import org.florious.passwordmanager.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 密码管理器应用程序入口
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("=== 密码管理器启动 ===");
        
        // 测试配置加载
        logger.info("1. 加载配置...");
        logger.info("   数据库路径: {}", Config.getDatabasePath());
        logger.info("   会话超时: {} 分钟", Config.getInt("session.timeout"));
        logger.info("   剪贴板清除: {} 秒", Config.getInt("clipboard.clear.timeout"));
        
        // 测试数据库初始化
        logger.info("2. 初始化数据库...");
        DatabaseManager dbManager = null;
        try {
            dbManager = DatabaseManager.getInstance();
            logger.info("   数据库初始化成功");
            logger.info("   数据库文件: {}", dbManager.getDbPath());
            
            // 检查连接
            if (dbManager.isInitialized()) {
                logger.info("   数据库连接正常");
            }
            
        } catch (Exception e) {
            logger.error("   数据库初始化失败: {}", e.getMessage(), e);
        } finally {
            // 确保资源被清理
            if (dbManager != null) {
                dbManager.closeConnection();
                logger.info("   数据库连接已关闭");
            }
        }
        
        logger.info("=== 密码管理器启动完成 ===");
        
        // 启动图形界面
        logger.info("3. 启动图形界面...");
        MainFrame.main(args);
    }
}