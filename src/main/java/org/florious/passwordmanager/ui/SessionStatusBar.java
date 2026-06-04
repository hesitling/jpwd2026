package org.florious.passwordmanager.ui;

import org.florious.passwordmanager.service.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 会话状态栏
 * 显示会话剩余时间和锁定状态
 */
public class SessionStatusBar extends JPanel {
    private final SessionManager sessionManager;
    private JLabel statusLabel;
    private JLabel timerLabel;
    private JButton lockButton;
    private Timer updateTimer;
    private boolean locked = false;

    public SessionStatusBar(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        initComponents();
        startUpdateTimer();
        updateStatus(); // 立即同步状态
    }

    /**
     * 初始化界面组件
     */
    private void initComponents() {
        setLayout(new BorderLayout(10, 0));
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        setBackground(new Color(240, 240, 240));

        // 左侧状态面板
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);

        // 会话状态图标
        statusLabel = new JLabel("🔓");
        statusLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        leftPanel.add(statusLabel);

        // 会话状态文本
        JLabel sessionLabel = new JLabel("会话状态:");
        sessionLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        leftPanel.add(sessionLabel);

        // 倒计时显示
        timerLabel = new JLabel("--:--");
        timerLabel.setFont(new Font("Monospaced", Font.BOLD, 12));
        timerLabel.setForeground(new Color(0, 120, 0));
        leftPanel.add(timerLabel);

        add(leftPanel, BorderLayout.WEST);

        // 右侧锁定按钮
        lockButton = new JButton("锁定");
        lockButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        lockButton.setPreferredSize(new Dimension(70, 25));
        lockButton.addActionListener(e -> lockApplication());
        add(lockButton, BorderLayout.EAST);
    }

    /**
     * 启动更新定时器
     */
    private void startUpdateTimer() {
        updateTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateStatus();
            }
        });
        updateTimer.start();
    }

    /**
     * 更新状态显示
     */
    private void updateStatus() {
        if (locked) {
            statusLabel.setText("🔒");
            timerLabel.setText("已锁定");
            timerLabel.setForeground(Color.RED);
            lockButton.setEnabled(false);
            return;
        }

        if (sessionManager.hasActiveSession()) {
            statusLabel.setText("🔓");
            lockButton.setEnabled(true);

            long remainingSeconds = sessionManager.getSessionRemainingSeconds();
            if (remainingSeconds >= 0) {
                long minutes = remainingSeconds / 60;
                long seconds = remainingSeconds % 60;
                timerLabel.setText(String.format("%02d:%02d", minutes, seconds));

                // 根据剩余时间改变颜色
                if (remainingSeconds < 60) {
                    timerLabel.setForeground(Color.RED);
                } else if (remainingSeconds < 120) {
                    timerLabel.setForeground(new Color(255, 165, 0)); // 橙色
                } else {
                    timerLabel.setForeground(new Color(0, 120, 0)); // 绿色
                }
            } else {
                timerLabel.setText("--:--");
                timerLabel.setForeground(Color.GRAY);
            }
        } else {
            statusLabel.setText("🔒");
            timerLabel.setText("未登录");
            timerLabel.setForeground(Color.GRAY);
            lockButton.setEnabled(false);
        }
    }

    /**
     * 锁定应用程序
     */
    private void lockApplication() {
        if (sessionManager.hasActiveSession()) {
            sessionManager.lock();
        }
    }

    /**
     * 设置锁定状态
     * @param locked 是否锁定
     */
    public void setLocked(boolean locked) {
        this.locked = locked;
        updateStatus();
    }

    /**
     * 停止更新定时器
     */
    public void stopTimer() {
        if (updateTimer != null) {
            updateTimer.stop();
        }
    }

    /**
     * 恢复更新定时器
     */
    public void resumeTimer() {
        if (updateTimer != null) {
            updateTimer.start();
        }
    }

    /**
     * 释放资源
     */
    public void dispose() {
        stopTimer();
    }
}
