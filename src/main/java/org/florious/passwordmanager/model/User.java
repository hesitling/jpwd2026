package org.florious.passwordmanager.model;

import java.time.LocalDateTime;

/**
 * 用户模型类
 * 存储用户信息和认证数据
 */
public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String salt;
    private String vaultSalt;  // 用于派生加密密钥的独立盐值
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    /**
     * 默认构造函数
     */
    public User() {
    }

    /**
     * 创建新用户的构造函数
     * @param username 用户名
     * @param passwordHash 密码哈希
     * @param salt 认证盐值
     * @param vaultSalt 加密密钥派生盐值
     */
    public User(String username, String passwordHash, String salt, String vaultSalt) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.vaultSalt = vaultSalt;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getVaultSalt() {
        return vaultSalt;
    }

    public void setVaultSalt(String vaultSalt) {
        this.vaultSalt = vaultSalt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", createdAt=" + createdAt +
                ", lastLogin=" + lastLogin +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id && java.util.Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, username);
    }
}