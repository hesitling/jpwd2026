package org.florious.passwordmanager.service;

import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;

/**
 * 活动监控器
 * 监控全局鼠标和键盘事件，用于会话超时管理
 */
public class ActivityMonitor {
    private static ActivityMonitor instance;
    private AWTEventListener awtEventListener;
    private volatile boolean monitoring = false;
    private volatile long lastActivityTime;

    private ActivityMonitor() {
        this.lastActivityTime = System.currentTimeMillis();
    }

    /**
     * 获取单例实例
     * @return ActivityMonitor实例
     */
    public static synchronized ActivityMonitor getInstance() {
        if (instance == null) {
            instance = new ActivityMonitor();
        }
        return instance;
    }

    /**
     * 重置单例实例（用于测试）
     */
    public static synchronized void resetInstance() {
        if (instance != null) {
            instance.stop();
            instance = null;
        }
    }

    /**
     * 开始监控活动
     */
    public synchronized void start() {
        if (monitoring) {
            return;
        }

        awtEventListener = event -> {
            if (event instanceof MouseEvent mouseEvent) {
                // 监控鼠标点击、移动、拖拽
                if (mouseEvent.getID() == MouseEvent.MOUSE_PRESSED ||
                    mouseEvent.getID() == MouseEvent.MOUSE_CLICKED ||
                    mouseEvent.getID() == MouseEvent.MOUSE_DRAGGED) {
                    recordActivity();
                }
            } else if (event instanceof KeyEvent keyEvent) {
                // 监控键盘按下
                if (keyEvent.getID() == KeyEvent.KEY_PRESSED) {
                    recordActivity();
                }
            }
        };

        Toolkit.getDefaultToolkit().addAWTEventListener(awtEventListener,
                AWTEvent.MOUSE_EVENT_MASK |
                AWTEvent.MOUSE_MOTION_EVENT_MASK |
                AWTEvent.KEY_EVENT_MASK);

        monitoring = true;
    }

    /**
     * 停止监控活动
     */
    public synchronized void stop() {
        if (!monitoring) {
            return;
        }

        if (awtEventListener != null) {
            Toolkit.getDefaultToolkit().removeAWTEventListener(awtEventListener);
            awtEventListener = null;
        }

        monitoring = false;
    }

    /**
     * 记录活动时间
     */
    private void recordActivity() {
        lastActivityTime = System.currentTimeMillis();
    }

    /**
     * 获取最后活动时间（毫秒）
     * @return 最后活动时间戳
     */
    public long getLastActivityTime() {
        return lastActivityTime;
    }

    /**
     * 获取自最后活动以来的空闲时间（毫秒）
     * @return 空闲时间
     */
    public long getIdleTimeMillis() {
        return System.currentTimeMillis() - lastActivityTime;
    }

    /**
     * 重置活动时间
     */
    public void resetActivity() {
        lastActivityTime = System.currentTimeMillis();
    }

    /**
     * 是否正在监控
     * @return 监控状态
     */
    public boolean isMonitoring() {
        return monitoring;
    }
}
