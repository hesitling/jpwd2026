package org.florious.passwordmanager.ui;

import org.florious.passwordmanager.util.Config;

import javax.swing.*;
import java.awt.*;

/**
 * 复制确认对话框
 * 在复制敏感信息前显示确认提示
 */
public class CopyConfirmDialog extends JDialog {
    private boolean confirmed = false;
    private JCheckBox skipConfirmCheckBox;
    private static final String CONFIG_KEY_SKIP_CONFIRM = "clipboard.copy.skip_confirm";

    /**
     * 显示复制确认对话框
     * @param parent 父组件
     * @param contentType 内容类型描述（如"密码"、"用户名"）
     * @return 如果用户确认返回true
     */
    public static boolean showConfirmDialog(Component parent, String contentType) {
        // 检查是否跳过确认
        if (Config.getBoolean(CONFIG_KEY_SKIP_CONFIRM, false)) {
            return true;
        }

        CopyConfirmDialog dialog = new CopyConfirmDialog(
                parent instanceof Frame ? (Frame) parent : null,
                contentType
        );
        dialog.setVisible(true);
        return dialog.isConfirmed();
    }

    /**
     * 构造函数
     * @param parent 父窗口
     * @param contentType 内容类型
     */
    private CopyConfirmDialog(Frame parent, String contentType) {
        super(parent, "复制确认", true);
        initComponents(contentType);
        pack();
        setLocationRelativeTo(parent);
    }

    /**
     * 初始化界面组件
     * @param contentType 内容类型
     */
    private void initComponents(String contentType) {
        setLayout(new BorderLayout(10, 10));
        setResizable(false);

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        // 图标和消息
        JPanel messagePanel = new JPanel(new BorderLayout(15, 0));

        // 警告图标
        JLabel iconLabel = new JLabel("⚠️");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        messagePanel.add(iconLabel, BorderLayout.WEST);

        // 消息文本
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 5));

        JLabel titleLabel = new JLabel("确认复制" + contentType);
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        textPanel.add(titleLabel);

        JLabel messageLabel = new JLabel(contentType + "将被复制到系统剪贴板");
        messageLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        messageLabel.setForeground(Color.GRAY);
        textPanel.add(messageLabel);

        messagePanel.add(textPanel, BorderLayout.CENTER);
        mainPanel.add(messagePanel, BorderLayout.NORTH);

        // 安全提示
        JPanel tipPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tipPanel.setBackground(new Color(255, 255, 230));
        tipPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 230, 150)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        JLabel tipLabel = new JLabel("💡 提示：剪贴板内容将在30秒后自动清除");
        tipLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
        tipPanel.add(tipLabel);

        mainPanel.add(tipPanel, BorderLayout.CENTER);

        // 不再提示选项
        JPanel skipPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        skipPanel.setOpaque(false);

        skipConfirmCheckBox = new JCheckBox("不再提示此确认");
        skipConfirmCheckBox.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
        skipConfirmCheckBox.addActionListener(e -> {
            Config.setBoolean(CONFIG_KEY_SKIP_CONFIRM, skipConfirmCheckBox.isSelected());
            Config.save();
        });
        skipPanel.add(skipConfirmCheckBox);

        mainPanel.add(skipPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        JButton cancelButton = new JButton("取消");
        cancelButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        cancelButton.setPreferredSize(new Dimension(80, 30));
        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        JButton confirmButton = new JButton("确认复制");
        confirmButton.setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
        confirmButton.setPreferredSize(new Dimension(100, 30));
        confirmButton.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        // 设置默认按钮
        getRootPane().setDefaultButton(confirmButton);

        buttonPanel.add(cancelButton);
        buttonPanel.add(confirmButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * 获取确认结果
     * @return 如果用户确认返回true
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * 检查是否应该跳过确认
     * @return 如果应该跳过返回true
     */
    public static boolean shouldSkipConfirm() {
        return Config.getBoolean(CONFIG_KEY_SKIP_CONFIRM, false);
    }

    /**
     * 设置是否跳过确认
     * @param skip 是否跳过
     */
    public static void setSkipConfirm(boolean skip) {
        Config.setBoolean(CONFIG_KEY_SKIP_CONFIRM, skip);
        Config.save();
    }
}
