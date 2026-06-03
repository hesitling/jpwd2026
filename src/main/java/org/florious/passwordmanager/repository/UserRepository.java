package org.florious.passwordmanager.repository;

import org.florious.passwordmanager.model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户仓库类
 * 负责用户数据的CRUD操作
 */
public class UserRepository {
    private final DatabaseManager dbManager;

    public UserRepository() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /**
     * 创建新用户
     * @param user 用户对象
     * @return 创建的用户（包含生成的ID）
     * @throws SQLException 如果创建失败
     */
    public User create(User user) throws SQLException {
        String sql = """
            INSERT INTO users (username, password_hash, salt, created_at)
            VALUES (?, ?, ?, ?)
        """;

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql, 
                Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getSalt());
            pstmt.setString(4, user.getCreatedAt().toString());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("创建用户失败，没有行被影响");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("创建用户失败，未获取到ID");
                }
            }
        }
        
        return user;
    }

    /**
     * 根据ID查找用户
     * @param id 用户ID
     * @return 用户对象，如果不存在返回null
     * @throws SQLException 如果查询失败
     */
    public User findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        
        return null;
    }

    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户对象，如果不存在返回null
     * @throws SQLException 如果查询失败
     */
    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        
        return null;
    }

    /**
     * 获取所有用户
     * @return 用户列表
     * @throws SQLException 如果查询失败
     */
    public List<User> findAll() throws SQLException {
        String sql = "SELECT * FROM users ORDER BY username";
        List<User> users = new ArrayList<>();
        
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        
        return users;
    }

    /**
     * 更新用户信息
     * @param user 用户对象
     * @return 更新是否成功
     * @throws SQLException 如果更新失败
     */
    public boolean update(User user) throws SQLException {
        String sql = """
            UPDATE users 
            SET username = ?, password_hash = ?, salt = ?, last_login = ?
            WHERE id = ?
        """;
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getSalt());
            pstmt.setString(4, user.getLastLogin() != null ? user.getLastLogin().toString() : null);
            pstmt.setInt(5, user.getId());
            
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * 更新用户最后登录时间
     * @param userId 用户ID
     * @return 更新是否成功
     * @throws SQLException 如果更新失败
     */
    public boolean updateLastLogin(int userId) throws SQLException {
        String sql = "UPDATE users SET last_login = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, LocalDateTime.now().toString());
            pstmt.setInt(2, userId);
            
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * 删除用户
     * @param id 用户ID
     * @return 删除是否成功
     * @throws SQLException 如果删除失败
     */
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return 如果存在返回true
     * @throws SQLException 如果查询失败
     */
    public boolean existsByUsername(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }

    /**
     * 获取用户总数
     * @return 用户总数
     * @throws SQLException 如果查询失败
     */
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users";
        
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        
        return 0;
    }

    /**
     * 将ResultSet映射为User对象
     * @param rs ResultSet
     * @return User对象
     * @throws SQLException 如果映射失败
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setSalt(rs.getString("salt"));
        
        String createdAt = rs.getString("created_at");
        if (createdAt != null) {
            user.setCreatedAt(LocalDateTime.parse(createdAt));
        }
        
        String lastLogin = rs.getString("last_login");
        if (lastLogin != null) {
            user.setLastLogin(LocalDateTime.parse(lastLogin));
        }
        
        return user;
    }
}