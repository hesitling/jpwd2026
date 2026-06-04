package org.florious.passwordmanager.ui;

import org.florious.passwordmanager.model.Category;
import org.florious.passwordmanager.model.PasswordEntry;
import org.florious.passwordmanager.service.CategoryService;
import org.florious.passwordmanager.service.VaultService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * 密码编辑对话框
 * 用于添加和编辑密码条目
 */
public class PasswordDialog extends JDialog {
    private final MainFrame mainFrame;
    private final VaultService vaultService;
    private final CategoryService categoryService;
    
    private PasswordEntry existingEntry; // 编辑模式时使用
    
    private JTextField titleField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField urlField;
    private JTextArea notesArea;
    private JComboBox<CategoryItem> categoryCombo;
    private JButton generateButton;
    private JButton saveButton;
    private JButton cancelButton;
    private JLabel statusLabel;
    
    // 对话框模式
    public enum DialogMode {
        ADD, EDIT
    }
    
    private final DialogMode mode;
    
    public PasswordDialog(Frame owner, DialogMode mode) {
        this(owner, mode, null);
    }
    
    public PasswordDialog(Frame owner, DialogMode mode, PasswordEntry existingEntry) {
        super(owner, mode == DialogMode.ADD ? "添加密码" : "编辑密码", true);
        this.mainFrame = (MainFrame) owner;
        this.vaultService = mainFrame.getVaultService();
        this.categoryService = new CategoryService();
        this.mode = mode;
        this.existingEntry = existingEntry;
        
        initComponents();
        loadCategories();
        
        if (mode == DialogMode.EDIT && existingEntry != null) {
            populateFields();
        }
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setSize(450, 500);
        setLocationRelativeTo(getOwner());
        
        // 主面板
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 标题
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("标题:"), gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        titleField = new JTextField(25);
        mainPanel.add(titleField, gbc);
        
        // 用户名
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("用户名:"), gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        usernameField = new JTextField(25);
        mainPanel.add(usernameField, gbc);
        
        // 密码
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("密码:"), gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JPanel passwordPanel = new JPanel(new BorderLayout(5, 0));
        passwordField = new JPasswordField(20);
        passwordPanel.add(passwordField, BorderLayout.CENTER);
        
        generateButton = new JButton("生成");
        generateButton.addActionListener(e -> generatePassword());
        passwordPanel.add(generateButton, BorderLayout.EAST);
        mainPanel.add(passwordPanel, gbc);
        
        // 确认密码
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("确认密码:"), gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        confirmPasswordField = new JPasswordField(25);
        mainPanel.add(confirmPasswordField, gbc);
        
        // URL
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("URL:"), gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        urlField = new JTextField(25);
        mainPanel.add(urlField, gbc);
        
        // 分类
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("分类:"), gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        categoryCombo = new JComboBox<>();
        mainPanel.add(categoryCombo, gbc);
        
        // 备注
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        mainPanel.add(new JLabel("备注:"), gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        notesArea = new JTextArea(5, 25);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScrollPane = new JScrollPane(notesArea);
        mainPanel.add(notesScrollPane, gbc);
        
        // 状态标签
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        mainPanel.add(statusLabel, gbc);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        saveButton = new JButton("保存");
        cancelButton = new JButton("取消");
        
        saveButton.addActionListener(e -> savePassword());
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadCategories() {
        try {
            List<Category> categories = categoryService.getAllCategories();
            categoryCombo.removeAllItems();
            
            // 添加"未分类"选项
            categoryCombo.addItem(new CategoryItem(null, "未分类"));
            
            for (Category category : categories) {
                CategoryItem item = new CategoryItem(category.getId(), category.getName());
                categoryCombo.addItem(item);
            }
        } catch (Exception e) {
            showError("加载分类失败: " + e.getMessage());
        }
    }
    
    private void populateFields() {
        if (existingEntry == null) {
            return;
        }
        
        titleField.setText(existingEntry.getTitle());
        usernameField.setText(existingEntry.getUsername());
        urlField.setText(existingEntry.getUrl());
        notesArea.setText(existingEntry.getNotes());
        
        // 设置分类
        Integer categoryId = existingEntry.getCategoryId();
        if (categoryId != null) {
            for (int i = 0; i < categoryCombo.getItemCount(); i++) {
                CategoryItem item = categoryCombo.getItemAt(i);
                if (item.getId() != null && item.getId().equals(categoryId)) {
                    categoryCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
        
        // 密码字段在编辑模式下不显示原密码
        passwordField.setText("");
        confirmPasswordField.setText("");
    }
    
    private void generatePassword() {
        PasswordGeneratorDialog dialog = new PasswordGeneratorDialog(this);
        dialog.setVisible(true);
        
        String generatedPassword = dialog.getGeneratedPassword();
        if (generatedPassword != null && !generatedPassword.isEmpty()) {
            passwordField.setText(generatedPassword);
            confirmPasswordField.setText(generatedPassword);
        }
    }
    
    private void savePassword() {
        // 验证输入
        String title = titleField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String url = urlField.getText().trim();
        String notes = notesArea.getText().trim();
        
        if (title.isEmpty()) {
            showError("请输入标题");
            return;
        }
        
        if (password.isEmpty()) {
            showError("请输入密码");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showError("两次输入的密码不一致");
            return;
        }
        
        // 获取分类ID
        CategoryItem selectedCategory = (CategoryItem) categoryCombo.getSelectedItem();
        Integer categoryId = selectedCategory != null ? selectedCategory.getId() : null;
        
        try {
            setEnabled(false);
            statusLabel.setText("正在保存...");
            
            if (mode == DialogMode.ADD) {
                vaultService.addPassword(title, username, password, url, notes, categoryId);
                showSuccess("密码添加成功");
            } else {
                vaultService.updatePassword(existingEntry.getId(), title, username, password, url, notes, categoryId);
                showSuccess("密码更新成功");
            }
            
            dispose();
            
        } catch (VaultService.VaultException e) {
            showError(e.getMessage());
        } finally {
            setEnabled(true);
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
        titleField.setEnabled(enabled);
        usernameField.setEnabled(enabled);
        passwordField.setEnabled(enabled);
        confirmPasswordField.setEnabled(enabled);
        urlField.setEnabled(enabled);
        notesArea.setEnabled(enabled);
        categoryCombo.setEnabled(enabled);
        generateButton.setEnabled(enabled);
        saveButton.setEnabled(enabled);
        cancelButton.setEnabled(enabled);
    }
    
    /**
     * 分类选项内部类
     */
    private static class CategoryItem {
        private final Integer id;
        private final String name;
        
        public CategoryItem(Integer id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public Integer getId() {
            return id;
        }
        
        public String getName() {
            return name;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
}