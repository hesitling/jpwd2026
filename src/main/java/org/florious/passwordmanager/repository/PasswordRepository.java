package org.florious.passwordmanager.repository;

import org.florious.passwordmanager.model.PasswordEntry;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 密码条目仓库类
 * 负责密码条目数据的CRUD操作和查询
 */
public class PasswordRepository {
    private final DatabaseManager dbManager;

    public PasswordRepository() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /**
     * 创建新密码条目
     * @param entry 密码条目对象
     * @return 创建的密码条目（包含生成的ID）
     * @throws SQLException 如果创建失败
     */
    public PasswordEntry create(PasswordEntry entry) throws SQLException {
        String sql = """
            INSERT INTO password_entries 
            (user_id, title, username, encrypted_password, iv, url, notes, category_id, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql, 
                Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, entry.getUserId());
            pstmt.setString(2, entry.getTitle());
            pstmt.setString(3, entry.getUsername());
            pstmt.setString(4, entry.getEncryptedPassword());
            pstmt.setString(5, entry.getIv());
            pstmt.setString(6, entry.getUrl());
            pstmt.setString(7, entry.getNotes());
            
            if (entry.getCategoryId() != null) {
                pstmt.setInt(8, entry.getCategoryId());
            } else {
                pstmt.setNull(8, Types.INTEGER);
            }
            
            pstmt.setString(9, entry.getCreatedAt().toString());
            pstmt.setString(10, entry.getUpdatedAt().toString());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("创建密码条目失败，没有行被影响");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entry.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("创建密码条目失败，未获取到ID");
                }
            }
        }
        
        return entry;
    }

    /**
     * 根据ID查找密码条目
     * @param id 密码条目ID
     * @return 密码条目对象，如果不存在返回null
     * @throws SQLException 如果查询失败
     */
    public PasswordEntry findById(int id) throws SQLException {
        String sql = "SELECT * FROM password_entries WHERE id = ?";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPasswordEntry(rs);
                }
            }
        }
        
        return null;
    }

    /**
     * 获取用户的所有密码条目
     * @param userId 用户ID
     * @return 密码条目列表
     * @throws SQLException 如果查询失败
     */
    public List<PasswordEntry> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM password_entries WHERE user_id = ? ORDER BY title";
        List<PasswordEntry> entries = new ArrayList<>();
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    entries.add(mapResultSetToPasswordEntry(rs));
                }
            }
        }
        
        return entries;
    }

    /**
     * 根据分类ID和用户ID获取密码条目
     * @param categoryId 分类ID
     * @param userId 用户ID
     * @return 密码条目列表
     * @throws SQLException 如果查询失败
     */
    public List<PasswordEntry> findByCategoryId(int categoryId, int userId) throws SQLException {
        String sql = "SELECT * FROM password_entries WHERE category_id = ? AND user_id = ? ORDER BY title";
        List<PasswordEntry> entries = new ArrayList<>();
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, categoryId);
            pstmt.setInt(2, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    entries.add(mapResultSetToPasswordEntry(rs));
                }
            }
        }
        
        return entries;
    }

    /**
     * 搜索密码条目
     * @param userId 用户ID
     * @param query 搜索关键词
     * @return 匹配的密码条目列表
     * @throws SQLException 如果查询失败
     */
    public List<PasswordEntry> search(int userId, String query) throws SQLException {
        String sql = """
            SELECT * FROM password_entries 
            WHERE user_id = ? AND (
                title LIKE ? ESCAPE '\\' OR 
                username LIKE ? ESCAPE '\\' OR 
                url LIKE ? ESCAPE '\\' OR
                notes LIKE ? ESCAPE '\\'
            )
            ORDER BY title
        """;
        
        List<PasswordEntry> entries = new ArrayList<>();
        // 转义 SQL LIKE 通配符
        String escapedQuery = query.replace("\\", "\\\\")
                                   .replace("%", "\\%")
                                   .replace("_", "\\_");
        String searchPattern = "%" + escapedQuery + "%";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            pstmt.setString(4, searchPattern);
            pstmt.setString(5, searchPattern);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    entries.add(mapResultSetToPasswordEntry(rs));
                }
            }
        }
        
        return entries;
    }

    /**
     * 更新密码条目
     * @param entry 密码条目对象
     * @return 更新是否成功
     * @throws SQLException 如果更新失败
     */
    public boolean update(PasswordEntry entry) throws SQLException {
        String sql = """
            UPDATE password_entries 
            SET title = ?, username = ?, encrypted_password = ?, iv = ?, 
                url = ?, notes = ?, category_id = ?, updated_at = ?
            WHERE id = ?
        """;
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, entry.getTitle());
            pstmt.setString(2, entry.getUsername());
            pstmt.setString(3, entry.getEncryptedPassword());
            pstmt.setString(4, entry.getIv());
            pstmt.setString(5, entry.getUrl());
            pstmt.setString(6, entry.getNotes());
            
            if (entry.getCategoryId() != null) {
                pstmt.setInt(7, entry.getCategoryId());
            } else {
                pstmt.setNull(7, Types.INTEGER);
            }
            
            pstmt.setString(8, LocalDateTime.now().toString());
            pstmt.setInt(9, entry.getId());
            
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * 删除密码条目
     * @param id 密码条目ID
     * @return 删除是否成功
     * @throws SQLException 如果删除失败
     */
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM password_entries WHERE id = ?";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * 获取用户的密码条目总数
     * @param userId 用户ID
     * @return 密码条目总数
     * @throws SQLException 如果查询失败
     */
    public int countByUserId(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM password_entries WHERE user_id = ?";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        
        return 0;
    }

    /**
     * 检查密码条目是否存在
     * @param id 密码条目ID
     * @return 如果存在返回true
     * @throws SQLException 如果查询失败
     */
    public boolean existsById(int id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM password_entries WHERE id = ?";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }

    /**
     * 检查标题是否已存在（同一用户下）
     * @param userId 用户ID
     * @param title 标题
     * @return 如果存在返回true
     * @throws SQLException 如果查询失败
     */
    public boolean existsByUserIdAndTitle(int userId, String title) throws SQLException {
        String sql = "SELECT COUNT(*) FROM password_entries WHERE user_id = ? AND title = ?";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, title);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }

    /**
     * 将ResultSet映射为PasswordEntry对象
     * @param rs ResultSet
     * @return PasswordEntry对象
     * @throws SQLException 如果映射失败
     */
    private PasswordEntry mapResultSetToPasswordEntry(ResultSet rs) throws SQLException {
        PasswordEntry entry = new PasswordEntry();
        entry.setId(rs.getInt("id"));
        entry.setUserId(rs.getInt("user_id"));
        entry.setTitle(rs.getString("title"));
        entry.setUsername(rs.getString("username"));
        entry.setEncryptedPassword(rs.getString("encrypted_password"));
        entry.setIv(rs.getString("iv"));
        entry.setUrl(rs.getString("url"));
        entry.setNotes(rs.getString("notes"));
        
        int categoryId = rs.getInt("category_id");
        if (!rs.wasNull()) {
            entry.setCategoryId(categoryId);
        }
        
        String createdAt = rs.getString("created_at");
        if (createdAt != null) {
            entry.setCreatedAt(LocalDateTime.parse(createdAt));
        }
        
        String updatedAt = rs.getString("updated_at");
        if (updatedAt != null) {
            entry.setUpdatedAt(LocalDateTime.parse(updatedAt));
        }
        
        return entry;
    }
}