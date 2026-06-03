package org.florious.passwordmanager;

import org.florious.passwordmanager.repository.DatabaseManager;
import org.florious.passwordmanager.util.Config;

/**
 * 密码管理器应用程序入口
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=== 密码管理器启动 ===");
        
        // 测试配置加载
        System.out.println("1. 加载配置...");
        System.out.println("   数据库路径: " + Config.getDatabasePath());
        System.out.println("   会话超时: " + Config.getInt("session.timeout") + " 分钟");
        System.out.println("   剪贴板清除: " + Config.getInt("clipboard.clear.timeout") + " 秒");
        
        // 测试数据库初始化
        System.out.println("\n2. 初始化数据库...");
        try {
            DatabaseManager dbManager = DatabaseManager.getInstance();
            System.out.println("   数据库初始化成功");
            System.out.println("   数据库文件: " + dbManager.getDbPath());
            
            // 检查连接
            if (dbManager.isInitialized()) {
                System.out.println("   数据库连接正常");
            }
            
            // 清理资源
            dbManager.closeConnection();
            System.out.println("   数据库连接已关闭");
            
        } catch (Exception e) {
            System.err.println("   数据库初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n=== 密码管理器启动完成 ===");
    }
}