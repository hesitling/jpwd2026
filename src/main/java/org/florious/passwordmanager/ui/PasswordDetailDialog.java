package org.florious.passwordmanager.ui;

import org.florious.passwordmanager.model.Category;
import org.florious.passwordmanager.service.CategoryService;
import org.florious.passwordmanager.service.VaultService;
import org.florious.passwordmanager.util.ClipboardUtil;
import java.awt.event.ActionEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.time.format.DateTimeFormatter;

/**
 * 密码详情对话框
 * 用于显示密码条目的完整信息（只读模式）
 */
public class PasswordDetailDialog extends JDialog {
    private final MainFrame mainFrame;
    private final VaultService vaultService;
    private final CategoryService categoryService;
    private final int entryId;

    private JTextField titleField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField urlField;
    private JTextField categoryField;
    private JTextArea notesArea;
    private JTextField createdAtField;
    private JTextField updatedAtField;

    private JButton copyTitleButton;
    private JButton copyUsernameButton;
    private JButton copyPasswordButton;
    private JButton copyUrlButton;
    private JButton togglePasswordButton;
    private JButton editButton;
    private JButton closeButton;

    private String decryptedPassword;
    private boolean passwordVisible = false;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public PasswordDetailDialog(Frame owner, int entryId) {
        super(owner, "密码详情", true);
        this.mainFrame = (MainFrame) owner;
        this.vaultService = mainFrame.getVaultService();
        this.categoryService = new CategoryService();
        this.entryId = entryId;

        initComponents();
        loadPasswordDetails();
        setupKeyBindings();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setSize(500, 550);
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
        gbc.weightx = 1.0;
        titleField = new JTextField(25);
        titleField.setEditable(false);
        mainPanel.add(titleField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        copyTitleButton = new JButton("复制");
        copyTitleButton.addActionListener(e -> copyToClipboard(titleField.getText(), "标题"));
        mainPanel.add(copyTitleButton, gbc);

        // 用户名
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("用户名:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        usernameField = new JTextField(25);
        usernameField.setEditable(false);
        mainPanel.add(usernameField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        copyUsernameButton = new JButton("复制");
        copyUsernameButton.addActionListener(e -> copyToClipboard(usernameField.getText(), "用户名"));
        mainPanel.add(copyUsernameButton, gbc);

        // 密码
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("密码:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        passwordField = new JPasswordField(25);
        passwordField.setEditable(false);
        mainPanel.add(passwordField, gbc);

        // 密码操作按钮面板
        gbc.gridx = 2;
        gbc.weightx = 0;
        JPanel passwordButtonPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        togglePasswordButton = new JButton("显示");
        togglePasswordButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 10));
        togglePasswordButton.addActionListener(e -> togglePasswordVisibility());
        passwordButtonPanel.add(togglePasswordButton);

        copyPasswordButton = new JButton("复制");
        copyPasswordButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 10));
        copyPasswordButton.addActionListener(e -> copyToClipboard(decryptedPassword, "密码"));
        passwordButtonPanel.add(copyPasswordButton);
        mainPanel.add(passwordButtonPanel, gbc);

        // URL
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("URL:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        urlField = new JTextField(25);
        urlField.setEditable(false);
        mainPanel.add(urlField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        copyUrlButton = new JButton("复制");
        copyUrlButton.addActionListener(e -> copyToClipboard(urlField.getText(), "URL"));
        mainPanel.add(copyUrlButton, gbc);

        // 分类
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("分类:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        categoryField = new JTextField(25);
        categoryField.setEditable(false);
        mainPanel.add(categoryField, gbc);
        gbc.gridwidth = 1;

        // 备注
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        mainPanel.add(new JLabel("备注:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        notesArea = new JTextArea(5, 25);
        notesArea.setEditable(false);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScrollPane = new JScrollPane(notesArea);
        mainPanel.add(notesScrollPane, gbc);
        gbc.gridwidth = 1;

        // 创建时间
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("创建时间:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        createdAtField = new JTextField(25);
        createdAtField.setEditable(false);
        mainPanel.add(createdAtField, gbc);
        gbc.gridwidth = 1;

        // 更新时间
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("更新时间:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        updatedAtField = new JTextField(25);
        updatedAtField.setEditable(false);
        mainPanel.add(updatedAtField, gbc);
        gbc.gridwidth = 1;

        add(mainPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        editButton = new JButton("编辑");
        editButton.addActionListener(e -> openEditMode());

        closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(editButton);
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadPasswordDetails() {
        try {
            VaultService.DecryptedPasswordEntry decryptedEntry = vaultService.getPassword(entryId);

            // 填充字段
            titleField.setText(decryptedEntry.getTitle());
            usernameField.setText(decryptedEntry.getUsername());
            decryptedPassword = decryptedEntry.getDecryptedPassword();
            passwordField.setText("********");
            urlField.setText(decryptedEntry.getUrl());
            notesArea.setText(decryptedEntry.getNotes());

            // 分类名称
            Integer categoryId = decryptedEntry.getCategoryId();
            if (categoryId != null) {
                try {
                    Category category = categoryService.getCategory(categoryId);
                    categoryField.setText(category != null ? category.getName() : "未分类");
                } catch (Exception e) {
                    categoryField.setText("未分类");
                }
            } else {
                categoryField.setText("未分类");
            }

            // 时间
            if (decryptedEntry.getCreatedAt() != null) {
                createdAtField.setText(decryptedEntry.getCreatedAt().format(formatter));
            }
            if (decryptedEntry.getUpdatedAt() != null) {
                updatedAtField.setText(decryptedEntry.getUpdatedAt().format(formatter));
            }

        } catch (VaultService.VaultException e) {
            JOptionPane.showMessageDialog(this,
                    "加载密码详情失败: " + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            passwordField.setText(decryptedPassword);
            togglePasswordButton.setText("隐藏");
        } else {
            passwordField.setText("********");
            togglePasswordButton.setText("显示");
        }
    }

    private void copyToClipboard(String content, String contentType) {
        if (content == null || content.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    contentType + "为空，无法复制",
                    "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean confirmed = CopyConfirmDialog.showConfirmDialog(this, contentType);
        if (confirmed) {
            ClipboardUtil.ContentType type = "密码".equals(contentType)
                    ? ClipboardUtil.ContentType.PASSWORD
                    : ClipboardUtil.ContentType.OTHER;
            ClipboardUtil.getInstance().copyToClipboard(content, type);
            JOptionPane.showMessageDialog(this,
                    contentType + "已复制到剪贴板",
                    "成功", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void openEditMode() {
        dispose();
        // 获取选中的密码条目并打开编辑对话框
        PasswordTable passwordTable = mainFrame.getVaultPanel().getPasswordTable();
        // 通过 entryId 创建 PasswordEntry 对象用于编辑
        try {
            VaultService.DecryptedPasswordEntry decryptedEntry = vaultService.getPassword(entryId);
            PasswordDialog dialog = new PasswordDialog(mainFrame, PasswordDialog.DialogMode.EDIT, decryptedEntry.getEntry());
            dialog.setVisible(true);
            // 编辑后刷新列表
            mainFrame.refreshData();
        } catch (VaultService.VaultException e) {
            JOptionPane.showMessageDialog(this,
                    "打开编辑对话框失败: " + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupKeyBindings() {
        // Escape 键关闭对话框
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
        getRootPane().getActionMap().put("close", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }
}
