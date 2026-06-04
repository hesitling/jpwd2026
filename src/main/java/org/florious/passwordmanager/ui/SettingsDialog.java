package org.florious.passwordmanager.ui;

import org.florious.passwordmanager.util.Config;

import javax.swing.*;
import java.awt.*;

/**
 * 设置对话框
 * 用于配置应用程序设置
 */
public class SettingsDialog extends JDialog {
    private final MainFrame mainFrame;
    
    private JSpinner sessionTimeoutSpinner;
    private JSpinner clipboardTimeoutSpinner;
    private JCheckBox showCopyConfirmCheckBox;
    private JButton saveButton;
    private JButton cancelButton;
    private JButton resetButton;
    private JLabel statusLabel;
    
    public SettingsDialog(Frame owner) {
        super(owner, "设置", true);
        this.mainFrame = (MainFrame) owner;
        
        initComponents();
        loadSettings();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setSize(400, 350);
        setLocationRelativeTo(getOwner());
        
        // 主面板
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 会话超时设置
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("会话超时时间（分钟）:"), gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        sessionTimeoutSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 60, 1));
        sessionTimeoutSpinner.setPreferredSize(new Dimension(80, 25));
        mainPanel.add(sessionTimeoutSpinner, gbc);
        
        // 剪贴板清除超时设置
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("剪贴板清除时间（秒）:"), gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        clipboardTimeoutSpinner = new JSpinner(new SpinnerNumberModel(30, 5, 300, 5));
        clipboardTimeoutSpinner.setPreferredSize(new Dimension(80, 25));
        mainPanel.add(clipboardTimeoutSpinner, gbc);
        
        // 复制确认设置
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        showCopyConfirmCheckBox = new JCheckBox("复制密码时显示确认对话框");
        mainPanel.add(showCopyConfirmCheckBox, gbc);
        
        // 状态标签
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        mainPanel.add(statusLabel, gbc);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        saveButton = new JButton("保存");
        cancelButton = new JButton("取消");
        resetButton = new JButton("恢复默认");
        
        saveButton.addActionListener(e -> saveSettings());
        cancelButton.addActionListener(e -> dispose());
        resetButton.addActionListener(e -> resetSettings());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(resetButton);
        
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadSettings() {
        try {
            // 加载会话超时时间
            int sessionTimeout = Config.getInt("session.timeout", 5);
            sessionTimeoutSpinner.setValue(sessionTimeout);
            
            // 加载剪贴板清除超时时间
            int clipboardTimeout = Config.getInt("clipboard.clear.timeout", 30);
            clipboardTimeoutSpinner.setValue(clipboardTimeout);
            
            // 加载复制确认设置
            boolean showCopyConfirm = Config.getBoolean("clipboard.show.copy.confirm", true);
            showCopyConfirmCheckBox.setSelected(showCopyConfirm);
            
        } catch (Exception e) {
            showError("加载设置失败: " + e.getMessage());
        }
    }
    
    private void saveSettings() {
        try {
            // 保存会话超时时间
            int sessionTimeout = (int) sessionTimeoutSpinner.getValue();
            Config.set("session.timeout", String.valueOf(sessionTimeout));
            
            // 保存剪贴板清除超时时间
            int clipboardTimeout = (int) clipboardTimeoutSpinner.getValue();
            Config.set("clipboard.clear.timeout", String.valueOf(clipboardTimeout));
            
            // 保存复制确认设置
            boolean showCopyConfirm = showCopyConfirmCheckBox.isSelected();
            Config.set("clipboard.show.copy.confirm", String.valueOf(showCopyConfirm));
            
            // 保存配置到文件
            Config.save();
            
            showSuccess("设置已保存");
            
            // 延迟关闭对话框
            Timer timer = new Timer(1000, e -> dispose());
            timer.setRepeats(false);
            timer.start();
            
        } catch (Exception e) {
            showError("保存设置失败: " + e.getMessage());
        }
    }
    
    private void resetSettings() {
        int choice = JOptionPane.showConfirmDialog(this, 
                "确定要恢复默认设置吗？", 
                "确认", 
                JOptionPane.YES_NO_OPTION);
        
        if (choice == JOptionPane.YES_OPTION) {
            sessionTimeoutSpinner.setValue(5);
            clipboardTimeoutSpinner.setValue(30);
            showCopyConfirmCheckBox.setSelected(true);
            showSuccess("已恢复默认设置");
        }
    }
    
    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(Color.RED);
    }
    
    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(Color.GREEN);
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        sessionTimeoutSpinner.setEnabled(enabled);
        clipboardTimeoutSpinner.setEnabled(enabled);
        showCopyConfirmCheckBox.setEnabled(enabled);
        saveButton.setEnabled(enabled);
        cancelButton.setEnabled(enabled);
        resetButton.setEnabled(enabled);
    }
}