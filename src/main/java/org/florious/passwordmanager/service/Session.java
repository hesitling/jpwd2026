package org.florious.passwordmanager.service;

import org.florious.passwordmanager.model.User;

import java.time.LocalDateTime;

/**
 * 会话类
 * 存储用户会话信息和派生的加密密钥
 */
public class Session {
    private final User user;
    private final byte[] derivedKey;
    private final LocalDateTime loginTime;
    private LocalDateTime lastActivityTime;

    /**
     * 创建新会话
     * @param user 用户对象
     * @param derivedKey 从主密码派生的加密密钥
     */
    public Session(User user, byte[] derivedKey) {
        this.user = user;
        this.derivedKey = derivedKey;
        this.loginTime = LocalDateTime.now();
        this.lastActivityTime = LocalDateTime.now();
    }

    /**
     * 获取用户对象
     * @return 用户对象
     */
    public User getUser() {
        return user;
    }

    /**
     * 获取派生的加密密钥
     * @return 32字节的加密密钥
     */
    public byte[] getDerivedKey() {
        return derivedKey;
    }

    /**
     * 获取登录时间
     * @return 登录时间
     */
    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    /**
     * 获取最后活动时间
     * @return 最后活动时间
     */
    public LocalDateTime getLastActivityTime() {
        return lastActivityTime;
    }

    /**
     * 更新最后活动时间
     */
    public void updateActivity() {
        this.lastActivityTime = LocalDateTime.now();
    }

    /**
     * 清除敏感数据
     */
    public void clearSensitiveData() {
        if (derivedKey != null) {
            java.util.Arrays.fill(derivedKey, (byte) 0);
        }
    }

    @Override
    public String toString() {
        return "Session{" +
                "userId=" + user.getId() +
                ", username='" + user.getUsername() + '\'' +
                ", loginTime=" + loginTime +
                ", lastActivityTime=" + lastActivityTime +
                '}';
    }
}
