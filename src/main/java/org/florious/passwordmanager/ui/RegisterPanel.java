package org.florious.passwordmanager.ui;

import org.florious.passwordmanager.service.AuthService;

import javax.swing.*;
import java.awt.*;

/**
 * 注册面板
 * 提供用户注册界面
 */
public class RegisterPanel extends JPanel {
    private final MainFrame mainFrame;
    private final AuthService authService;
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton registerButton;
    private JButton backButton;
    private JLabel statusLabel;
    
    public RegisterPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.authService = mainFrame.getAuthService();
        
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 标题
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JLabel titleLabel = new JLabel("密码管理器 - 注册");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        add(titleLabel, gbc);
        
        // 用户名
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("用户名:"), gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        usernameField = new JTextField(20);
        add(usernameField, gbc);
        
        // 密码
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("密码:"), gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        passwordField = new JPasswordField(20);
        add(passwordField, gbc);
        
        // 确认密码
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("确认密码:"), gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        confirmPasswordField = new JPasswordField(20);
        add(confirmPasswordField, gbc);
        
        // 按钮面板
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        registerButton = new JButton("注册");
        backButton = new JButton("返回");
        
        registerButton.addActionListener(e -> performRegistration());
        backButton.addActionListener(e -> mainFrame.showLoginPanel());
        
        buttonPanel.add(registerButton);
        buttonPanel.add(backButton);
        add(buttonPanel, gbc);
        
        // 状态标签
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        add(statusLabel, gbc);
        
        // 设置默认按钮
        SwingUtilities.invokeLater(() -> {
            getRootPane().setDefaultButton(registerButton);
            usernameField.requestFocusInWindow();
        });
    }
    
    private void performRegistration() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        // 验证输入
        if (username.isEmpty()) {
            showError("请输入用户名");
            return;
        }
        
        if (password.isEmpty()) {
            showError("请输入密码");
            return;
        }
        
        if (password.length() < 6) {
            showError("密码长度至少6位");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showError("两次输入的密码不一致");
            return;
        }
        
        try {
            setEnabled(false);
            statusLabel.setText("正在注册...");
            
            authService.register(username, password);
            
            // 注册成功
            clearFields();
            JOptionPane.showMessageDialog(this, "注册成功！请登录", "成功", JOptionPane.INFORMATION_MESSAGE);
            mainFrame.showLoginPanel();
            
        } catch (AuthService.AuthException e) {
            showError(e.getMessage());
        } finally {
            setEnabled(true);
        }
    }
    
    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(Color.RED);
    }
    
    private void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
        statusLabel.setText(" ");
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        usernameField.setEnabled(enabled);
        passwordField.setEnabled(enabled);
        confirmPasswordField.setEnabled(enabled);
        registerButton.setEnabled(enabled);
        backButton.setEnabled(enabled);
    }
}