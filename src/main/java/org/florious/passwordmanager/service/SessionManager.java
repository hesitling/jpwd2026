package org.florious.passwordmanager.service;

import org.florious.passwordmanager.crypto.CryptoService;
import org.florious.passwordmanager.model.User;
import org.florious.passwordmanager.util.ClipboardUtil;
import org.florious.passwordmanager.util.Config;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 会话管理器
 * 管理用户会话的创建、销毁和超时检查
 */
public class SessionManager {
    private static SessionManager instance;
    private Session currentSession;
    private final CryptoService cryptoService;
    private final List<SessionListener> listeners;
    private final ActivityMonitor activityMonitor;
    private Timer timeoutTimer;
    private static final int TIMEOUT_CHECK_INTERVAL = 30000; // 30秒

    private SessionManager() {
        this(false);
    }

    private SessionManager(boolean testMode) {
        this.cryptoService = new CryptoService(testMode);
        this.listeners = new CopyOnWriteArrayList<>();
        this.activityMonitor = ActivityMonitor.getInstance();
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
     * 获取单例实例（测试模式）
     * @param testMode 是否使用测试模式
     * @return SessionManager实例
     */
    public static synchronized SessionManager getInstance(boolean testMode) {
        if (instance == null) {
            instance = new SessionManager(testMode);
        }
        return instance;
    }

    /**
     * 重置单例实例（用于测试清理）
     */
    public static synchronized void resetInstance() {
        if (instance != null) {
            instance.destroySession();
            instance = null;
        }
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

        // 使用独立的 vaultSalt 派生加密密钥（域分离，与认证哈希独立）
        String vaultSalt = user.getVaultSalt();
        if (vaultSalt == null || vaultSalt.isEmpty()) {
            // 向后兼容：旧用户没有 vaultSalt，使用 salt
            vaultSalt = user.getSalt();
        }
        byte[] derivedKey = cryptoService.deriveKey(masterPassword, vaultSalt);

        // 创建新会话
        currentSession = new Session(user, derivedKey);

        // 启动活动监控
        activityMonitor.start();
        activityMonitor.resetActivity();

        // 启动超时检查定时器
        startTimeoutTimer();

        // 通知监听器
        notifySessionCreated();
    }

    /**
     * 手动锁定会话
     * 立即销毁会话并通知监听器
     */
    public void lock() {
        if (currentSession != null) {
            // 通知锁定
            notifySessionLocked();

            // 销毁会话
            destroySession();
        }
    }

    /**
     * 销毁当前会话
     */
    public void destroySession() {
        if (currentSession != null) {
            // 清除敏感数据
            currentSession.clearSensitiveData();

            // 清除剪贴板中的敏感内容
            ClipboardUtil.getInstance().clearClipboard();

            // 停止活动监控
            activityMonitor.stop();

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
        Session session = currentSession;
        if (session != null) {
            session.updateActivity();
        }
        return session;
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
        return activityMonitor.getIdleTimeMillis() >= timeoutMillis;
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
        long idleMillis = activityMonitor.getIdleTimeMillis();

        return Math.max(0, (timeoutMillis - idleMillis) / 1000);
    }

    /**
     * 重置会话超时
     * 在用户活动时调用
     */
    public void resetTimeout() {
        if (currentSession != null) {
            currentSession.updateActivity();
            activityMonitor.resetActivity();
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
     * 通知会话锁定
     */
    private void notifySessionLocked() {
        for (SessionListener listener : listeners) {
            listener.onSessionLocked();
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

        /**
         * 手动锁定时调用
         */
        default void onSessionLocked() {}
    }
}
