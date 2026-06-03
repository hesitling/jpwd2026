package org.florious.passwordmanager.model;

import java.time.LocalDateTime;

/**
 * 密码条目模型类
 * 存储加密的密码条目信息
 */
public class PasswordEntry {
    private int id;
    private int userId;
    private String title;
    private String username;
    private String encryptedPassword;
    private String iv; // 初始化向量
    private String url;
    private String notes;
    private Integer categoryId; // 可以为null
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 默认构造函数
     */
    public PasswordEntry() {
    }

    /**
     * 创建新密码条目的构造函数
     * @param userId 用户ID
     * @param title 标题
     * @param username 用户名
     * @param encryptedPassword 加密后的密码
     * @param iv 初始化向量
     */
    public PasswordEntry(int userId, String title, String username, 
                        String encryptedPassword, String iv) {
        this.userId = userId;
        this.title = title;
        this.username = username;
        this.encryptedPassword = encryptedPassword;
        this.iv = iv;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * 更新修改时间
     */
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "PasswordEntry{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", username='" + username + '\'' +
                ", url='" + url + '\'' +
                ", categoryId=" + categoryId +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PasswordEntry that = (PasswordEntry) o;
        return id == that.id && userId == that.userId && 
               title.equals(that.title);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, userId, title);
    }
}