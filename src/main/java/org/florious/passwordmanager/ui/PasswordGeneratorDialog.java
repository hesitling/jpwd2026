package org.florious.passwordmanager.ui;

import org.florious.passwordmanager.crypto.PasswordGenerator;
import org.florious.passwordmanager.model.PasswordPolicy;
import org.florious.passwordmanager.model.PasswordStrength;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 密码生成器对话框
 */
public class PasswordGeneratorDialog extends JDialog {
    private PasswordGenerator passwordGenerator;
    private PasswordPolicy policy;
    private String generatedPassword;
    
    private JTextField passwordField;
    private JSlider lengthSlider;
    private JLabel lengthLabel;
    private JCheckBox uppercaseCheckBox;
    private JCheckBox lowercaseCheckBox;
    private JCheckBox digitsCheckBox;
    private JCheckBox symbolsCheckBox;
    private StrengthMeter strengthMeter;
    private JButton generateButton;
    private JButton copyButton;
    private JButton closeButton;

    public PasswordGeneratorDialog(Frame owner) {
        super(owner, "密码生成器", true);
        this.passwordGenerator = new PasswordGenerator();
        this.policy = new PasswordPolicy();
        initComponents();
        generatePassword();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setSize(400, 350);
        setLocationRelativeTo(getOwner());

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 密码显示面板
        JPanel passwordPanel = new JPanel(new BorderLayout(5, 5));
        passwordPanel.setBorder(BorderFactory.createTitledBorder("生成的密码"));
        
        passwordField = new JTextField();
        passwordField.setEditable(false);
        passwordField.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        passwordPanel.add(passwordField, BorderLayout.CENTER);
        
        mainPanel.add(passwordPanel, BorderLayout.NORTH);

        // 配置面板
        JPanel configPanel = new JPanel(new GridBagLayout());
        configPanel.setBorder(BorderFactory.createTitledBorder("密码配置"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 长度滑块
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        configPanel.add(new JLabel("密码长度:"), gbc);

        gbc.gridy = 1;
        lengthSlider = new JSlider(4, 64, policy.getLength());
        lengthSlider.setMajorTickSpacing(10);
        lengthSlider.setMinorTickSpacing(1);
        lengthSlider.setPaintTicks(true);
        lengthSlider.setPaintLabels(true);
        lengthSlider.addChangeListener(e -> {
            int length = lengthSlider.getValue();
            policy.setLength(length);
            lengthLabel.setText(length + " 位");
        });
        configPanel.add(lengthSlider, gbc);

        gbc.gridy = 2;
        lengthLabel = new JLabel(policy.getLength() + " 位");
        lengthLabel.setHorizontalAlignment(SwingConstants.CENTER);
        configPanel.add(lengthLabel, gbc);

        // 字符类型复选框
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        uppercaseCheckBox = new JCheckBox("大写字母 (A-Z)", policy.isIncludeUppercase());
        uppercaseCheckBox.addActionListener(e -> policy.setIncludeUppercase(uppercaseCheckBox.isSelected()));
        configPanel.add(uppercaseCheckBox, gbc);

        gbc.gridx = 1;
        lowercaseCheckBox = new JCheckBox("小写字母 (a-z)", policy.isIncludeLowercase());
        lowercaseCheckBox.addActionListener(e -> policy.setIncludeLowercase(lowercaseCheckBox.isSelected()));
        configPanel.add(lowercaseCheckBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        digitsCheckBox = new JCheckBox("数字 (0-9)", policy.isIncludeDigits());
        digitsCheckBox.addActionListener(e -> policy.setIncludeDigits(digitsCheckBox.isSelected()));
        configPanel.add(digitsCheckBox, gbc);

        gbc.gridx = 1;
        symbolsCheckBox = new JCheckBox("符号 (!@#$...)", policy.isIncludeSymbols());
        symbolsCheckBox.addActionListener(e -> policy.setIncludeSymbols(symbolsCheckBox.isSelected()));
        configPanel.add(symbolsCheckBox, gbc);

        mainPanel.add(configPanel, BorderLayout.CENTER);

        // 强度指示器
        strengthMeter = new StrengthMeter();
        mainPanel.add(strengthMeter, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        generateButton = new JButton("生成密码");
        generateButton.addActionListener(e -> generatePassword());
        buttonPanel.add(generateButton);

        copyButton = new JButton("复制密码");
        copyButton.addActionListener(e -> copyPassword());
        buttonPanel.add(copyButton);

        closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void generatePassword() {
        if (!policy.isValid()) {
            JOptionPane.showMessageDialog(this, "至少需要选择一种字符类型", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        generatedPassword = passwordGenerator.generate(policy);
        passwordField.setText(generatedPassword);
        
        PasswordStrength strength = passwordGenerator.checkStrength(generatedPassword);
        strengthMeter.updateStrength(strength);
    }

    private void copyPassword() {
        if (generatedPassword != null && !generatedPassword.isEmpty()) {
            StringSelection selection = new StringSelection(generatedPassword);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
            JOptionPane.showMessageDialog(this, "密码已复制到剪贴板", "成功", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * 获取生成的密码
     */
    public String getGeneratedPassword() {
        return generatedPassword;
    }

    /**
     * 显示对话框并返回生成的密码
     */
    public static String showDialog(Frame owner) {
        PasswordGeneratorDialog dialog = new PasswordGeneratorDialog(owner);
        dialog.setVisible(true);
        return dialog.getGeneratedPassword();
    }
}