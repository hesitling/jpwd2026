package org.florious.passwordmanager.ui;

import org.florious.passwordmanager.service.AuthService;
import org.florious.passwordmanager.service.SessionManager;

import javax.swing.*;
import java.awt.*;

/**
 * 登录面板
 * 提供用户登录界面
 */
public class LoginPanel extends JPanel {
    private final MainFrame mainFrame;
    private final AuthService authService;
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel statusLabel;
    
    public LoginPanel(MainFrame mainFrame) {
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
        JLabel titleLabel = new JLabel("密码管理器 - 登录");
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
        
        // 按钮面板
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        loginButton = new JButton("登录");
        registerButton = new JButton("注册");
        
        loginButton.addActionListener(e -> performLogin());
        registerButton.addActionListener(e -> mainFrame.showRegisterPanel());
        
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        add(buttonPanel, gbc);
        
        // 状态标签
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        add(statusLabel, gbc);
        
        // 设置默认按钮
        SwingUtilities.invokeLater(() -> {
            getRootPane().setDefaultButton(loginButton);
            usernameField.requestFocusInWindow();
        });
    }
    
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty()) {
            showError("请输入用户名");
            return;
        }
        
        if (password.isEmpty()) {
            showError("请输入密码");
            return;
        }
        
        try {
            setEnabled(false);
            statusLabel.setText("正在登录...");
            
            authService.login(username, password);
            
            // 登录成功
            clearFields();
            mainFrame.showVaultPanel();
            
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
        statusLabel.setText(" ");
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        usernameField.setEnabled(enabled);
        passwordField.setEnabled(enabled);
        loginButton.setEnabled(enabled);
        registerButton.setEnabled(enabled);
    }
}