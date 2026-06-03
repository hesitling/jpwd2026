package org.florious.passwordmanager.service;

import org.florious.passwordmanager.crypto.CryptoService;
import org.florious.passwordmanager.model.User;
import org.florious.passwordmanager.util.Config;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 会话管理器
 * 管理用户会话的创建、销毁和超时检查
 */
public class SessionManager {
    private static SessionManager instance;
    private Session currentSession;
    private final CryptoService cryptoService;
    private final List<SessionListener> listeners;
    private Timer timeoutTimer;
    private static final int TIMEOUT_CHECK_INTERVAL = 30000; // 30秒

    private SessionManager() {
        this.cryptoService = new CryptoService();
        this.listeners = new ArrayList<>();
    }

    /**
     * 获取单例实例
     * @return SessionManager实例
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * 创建新会话
     * @param user 用户对象
     * @param masterPassword 主密码（用于派生加密密钥）
     */
    public void createSession(User user, String masterPassword) {
        // 如果已有会话，先销毁
        if (currentSession != null) {
            destroySession();
        }

        // 从主密码派生加密密钥
        byte[] derivedKey = cryptoService.deriveKey(masterPassword, user.getSalt());

        // 创建新会话
        currentSession = new Session(user, derivedKey);

        // 启动超时检查定时器
        startTimeoutTimer();

        // 通知监听器
        notifySessionCreated();
    }

    /**
     * 销毁当前会话
     */
    public void destroySession() {
        if (currentSession != null) {
            // 清除敏感数据
            currentSession.clearSensitiveData();

            // 停止超时检查定时器
            stopTimeoutTimer();

            // 通知监听器
            notifySessionDestroyed();

            // 清空当前会话
            currentSession = null;
        }
    }

    /**
     * 获取当前会话
     * @return 当前会话，如果不存在返回null
     */
    public Session getCurrentSession() {
        if (currentSession != null) {
            currentSession.updateActivity();
        }
        return currentSession;
    }

    /**
     * 检查是否有活跃会话
     * @return 如果有活跃会话返回true
     */
    public boolean hasActiveSession() {
        return currentSession != null;
    }

    /**
     * 检查会话是否已超时
     * @return 如果会话已超时返回true
     */
    public boolean isSessionTimedOut() {
        if (currentSession == null) {
            return false;
        }

        long timeoutMillis = Config.getSessionTimeoutMillis();
        LocalDateTime lastActivity = currentSession.getLastActivityTime();
        LocalDateTime now = LocalDateTime.now();

        Duration duration = Duration.between(lastActivity, now);
        return duration.toMillis() >= timeoutMillis;
    }

    /**
     * 获取会话剩余时间（秒）
     * @return 剩余时间，如果会话不存在返回-1
     */
    public long getSessionRemainingSeconds() {
        if (currentSession == null) {
            return -1;
        }

        long timeoutMillis = Config.getSessionTimeoutMillis();
        LocalDateTime lastActivity = currentSession.getLastActivityTime();
        LocalDateTime now = LocalDateTime.now();

        Duration duration = Duration.between(lastActivity, now);
        long elapsedMillis = duration.toMillis();

        return Math.max(0, (timeoutMillis - elapsedMillis) / 1000);
    }

    /**
     * 重置会话超时
     * 在用户活动时调用
     */
    public void resetTimeout() {
        if (currentSession != null) {
            currentSession.updateActivity();
        }
    }

    /**
     * 添加会话监听器
     * @param listener 会话监听器
     */
    public void addSessionListener(SessionListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * 移除会话监听器
     * @param listener 会话监听器
     */
    public void removeSessionListener(SessionListener listener) {
        listeners.remove(listener);
    }

    /**
     * 启动超时检查定时器
     */
    private void startTimeoutTimer() {
        stopTimeoutTimer();

        timeoutTimer = new Timer("SessionTimeoutTimer", true);
        timeoutTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkTimeout();
            }
        }, TIMEOUT_CHECK_INTERVAL, TIMEOUT_CHECK_INTERVAL);
    }

    /**
     * 停止超时检查定时器
     */
    private void stopTimeoutTimer() {
        if (timeoutTimer != null) {
            timeoutTimer.cancel();
            timeoutTimer = null;
        }
    }

    /**
     * 检查会话超时
     */
    private void checkTimeout() {
        if (isSessionTimedOut()) {
            notifySessionTimeout();
            destroySession();
        }
    }

    /**
     * 通知会话创建
     */
    private void notifySessionCreated() {
        for (SessionListener listener : listeners) {
            listener.onSessionCreated(currentSession);
        }
    }

    /**
     * 通知会话销毁
     */
    private void notifySessionDestroyed() {
        for (SessionListener listener : listeners) {
            listener.onSessionDestroyed();
        }
    }

    /**
     * 通知会话超时
     */
    private void notifySessionTimeout() {
        for (SessionListener listener : listeners) {
            listener.onSessionTimeout();
        }
    }

    /**
     * 会话监听器接口
     */
    public interface SessionListener {
        /**
         * 会话创建时调用
         * @param session 新创建的会话
         */
        void onSessionCreated(Session session);

        /**
         * 会话销毁时调用
         */
        void onSessionDestroyed();

        /**
         * 会话超时时调用
         */
        void onSessionTimeout();
    }
}
