package org.florious.passwordmanager.ui;

import javax.swing.*;
import java.awt.*;

/**
 * 锁定界面
 * 当会话超时或用户手动锁定时显示
 */
public class LockPanel extends JPanel {
    private JPasswordField passwordField;
    private JButton unlockButton;
    private JLabel statusLabel;
    private UnlockCallback unlockCallback;

    /**
     * 解锁回调接口
     */
    public interface UnlockCallback {
        /**
         * 验证密码并尝试解锁
         * @param password 主密码
         */
        void onUnlockAttempt(String password);

        /**
         * 解锁失败时调用
         * @param message 错误消息
         */
        void onUnlockFailure(String message);
    }

    public LockPanel() {
        initComponents();
    }

    /**
     * 初始化界面组件
     */
    private void initComponents() {
        setLayout(new GridBagLayout());
        setBackground(new Color(45, 45, 45));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // 应用图标
        JLabel iconLabel = new JLabel("🔒");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        iconLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(iconLabel, gbc);

        // 应用标题
        JLabel titleLabel = new JLabel("密码管理器");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridy = 1;
        add(titleLabel, gbc);

        // 锁定提示
        JLabel lockMessageLabel = new JLabel("应用程序已锁定");
        lockMessageLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        lockMessageLabel.setForeground(new Color(180, 180, 180));
        gbc.gridy = 2;
        add(lockMessageLabel, gbc);

        // 密码输入面板
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setOpaque(false);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JLabel passwordLabel = new JLabel("主密码:");
        passwordLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        passwordLabel.setForeground(Color.WHITE);
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        passwordField.addActionListener(e -> attemptUnlock());
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(passwordField, gbc);

        // 解锁按钮
        unlockButton = new JButton("解锁");
        unlockButton.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        unlockButton.setPreferredSize(new Dimension(120, 35));
        unlockButton.addActionListener(e -> attemptUnlock());
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        add(unlockButton, gbc);

        // 状态消息
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(255, 100, 100));
        gbc.gridy = 5;
        add(statusLabel, gbc);

        // 快捷键提示
        JLabel hintLabel = new JLabel("按 Enter 键解锁");
        hintLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
        hintLabel.setForeground(new Color(120, 120, 120));
        gbc.gridy = 6;
        add(hintLabel, gbc);
    }

    /**
     * 尝试解锁
     */
    private void attemptUnlock() {
        char[] passwordChars = passwordField.getPassword();
        if (passwordChars.length == 0) {
            statusLabel.setText("请输入主密码");
            statusLabel.setForeground(new Color(255, 100, 100));
            passwordField.requestFocus();
            return;
        }

        String password = new String(passwordChars);
        // 清除 char[] 以减少内存中的敏感数据
        java.util.Arrays.fill(passwordChars, '\0');

        // 通知回调进行解锁验证
        if (unlockCallback != null) {
            unlockCallback.onUnlockAttempt(password);
        }
    }

    /**
     * 获取输入的密码
     * @return 密码字符串
     */
    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    /**
     * 清除密码输入
     */
    public void clearPassword() {
        passwordField.setText("");
        statusLabel.setText(" ");
    }

    /**
     * 显示错误消息
     * @param message 错误消息
     */
    public void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(new Color(255, 100, 100));
        passwordField.setText("");
        passwordField.requestFocus();
    }

    /**
     * 显示成功消息
     * @param message 成功消息
     */
    public void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(new Color(100, 255, 100));
    }

    /**
     * 设置解锁回调
     * @param callback 回调对象
     */
    public void setUnlockCallback(UnlockCallback callback) {
        this.unlockCallback = callback;
    }

    /**
     * 获取密码输入框（用于设置焦点）
     * @return 密码输入框
     */
    public JPasswordField getPasswordField() {
        return passwordField;
    }

    /**
     * 请求焦点到密码输入框
     */
    public void requestPasswordFocus() {
        SwingUtilities.invokeLater(() -> {
            passwordField.requestFocusInWindow();
        });
    }
}
