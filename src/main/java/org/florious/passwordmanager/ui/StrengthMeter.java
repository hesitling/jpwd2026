package org.florious.passwordmanager.ui;

import org.florious.passwordmanager.model.PasswordStrength;

import javax.swing.*;
import java.awt.*;

/**
 * 密码强度指示器组件
 */
public class StrengthMeter extends JPanel {
    private PasswordStrength strength = PasswordStrength.WEAK;
    private JLabel strengthLabel;
    private JPanel meterPanel;

    public StrengthMeter() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // 强度标签
        strengthLabel = new JLabel("强度: 弱");
        strengthLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        add(strengthLabel, BorderLayout.NORTH);

        // 强度指示器面板
        meterPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawMeter(g);
            }
        };
        meterPanel.setPreferredSize(new Dimension(200, 20));
        meterPanel.setBackground(Color.LIGHT_GRAY);
        add(meterPanel, BorderLayout.CENTER);
    }

    private void drawMeter(Graphics g) {
        int width = meterPanel.getWidth();
        int height = meterPanel.getHeight();
        
        // 绘制背景
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, width, height);
        
        // 绘制强度条
        int strengthWidth = (int) (width * (strength.getLevel() + 1) / 4.0);
        Color strengthColor = getStrengthColor();
        g.setColor(strengthColor);
        g.fillRect(0, 0, strengthWidth, height);
        
        // 绘制边框
        g.setColor(Color.DARK_GRAY);
        g.drawRect(0, 0, width - 1, height - 1);
    }

    private Color getStrengthColor() {
        try {
            return Color.decode(strength.getColorCode());
        } catch (NumberFormatException e) {
            return Color.RED;
        }
    }

    /**
     * 更新强度显示
     */
    public void updateStrength(PasswordStrength strength) {
        this.strength = strength;
        strengthLabel.setText("强度: " + strength.getDisplayName());
        meterPanel.repaint();
    }

    /**
     * 获取当前强度
     */
    public PasswordStrength getStrength() {
        return strength;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(200, 40);
    }
}