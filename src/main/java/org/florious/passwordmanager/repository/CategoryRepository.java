package org.florious.passwordmanager.repository;

import org.florious.passwordmanager.model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 分类仓库类
 * 负责分类数据的CRUD操作
 */
public class CategoryRepository {
    private final DatabaseManager dbManager;

    public CategoryRepository() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /**
     * 创建新分类
     * @param category 分类对象
     * @return 创建的分类（包含生成的ID）
     * @throws SQLException 如果创建失败
     */
    public Category create(Category category) throws SQLException {
        String sql = """
            INSERT INTO categories (user_id, name, color)
            VALUES (?, ?, ?)
        """;

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql, 
                Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, category.getUserId());
            pstmt.setString(2, category.getName());
            pstmt.setString(3, category.getColor());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("创建分类失败，没有行被影响");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    category.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("创建分类失败，未获取到ID");
                }
            }
        }
        
        return category;
    }

    /**
     * 根据ID查找分类
     * @param id 分类ID
     * @return 分类对象，如果不存在返回null
     * @throws SQLException 如果查询失败
     */
    public Category findById(int id) throws SQLException {
        String sql = "SELECT * FROM categories WHERE id = ?";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCategory(rs);
                }
            }
        }
        
        return null;
    }

    /**
     * 获取用户的所有分类
     * @param userId 用户ID
     * @return 分类列表
     * @throws SQLException 如果查询失败
     */
    public List<Category> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM categories WHERE user_id = ? ORDER BY name";
        List<Category> categories = new ArrayList<>();
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(mapResultSetToCategory(rs));
                }
            }
        }
        
        return categories;
    }

    /**
     * 根据用户ID和分类名查找分类
     * @param userId 用户ID
     * @param name 分类名
     * @return 分类对象，如果不存在返回null
     * @throws SQLException 如果查询失败
     */
    public Category findByUserIdAndName(int userId, String name) throws SQLException {
        String sql = "SELECT * FROM categories WHERE user_id = ? AND name = ?";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, name);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCategory(rs);
                }
            }
        }
        
        return null;
    }

    /**
     * 更新分类信息
     * @param category 分类对象
     * @return 更新是否成功
     * @throws SQLException 如果更新失败
     */
    public boolean update(Category category) throws SQLException {
        String sql = """
            UPDATE categories 
            SET name = ?, color = ?
            WHERE id = ?
        """;
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getColor());
            pstmt.setInt(3, category.getId());
            
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * 删除分类
     * @param id 分类ID
     * @return 删除是否成功
     * @throws SQLException 如果删除失败
     */
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM categories WHERE id = ?";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * 检查分类名是否存在（同一用户下）
     * @param userId 用户ID
     * @param name 分类名
     * @return 如果存在返回true
     * @throws SQLException 如果查询失败
     */
    public boolean existsByUserIdAndName(int userId, String name) throws SQLException {
        String sql = "SELECT COUNT(*) FROM categories WHERE user_id = ? AND name = ?";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, name);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }

    /**
     * 获取分类下的密码条目数量
     * @param categoryId 分类ID
     * @return 密码条目数量
     * @throws SQLException 如果查询失败
     */
    public int countPasswordEntries(int categoryId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM password_entries WHERE category_id = ?";
        
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, categoryId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        
        return 0;
    }

    /**
     * 获取用户的分类总数
     * @param userId 用户ID
     * @return 分类总数
     * @throws SQLException 如果查询失败
     */
    public int countByUserId(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM categories WHERE user_id = ?";
        
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
     * 将ResultSet映射为Category对象
     * @param rs ResultSet
     * @return Category对象
     * @throws SQLException 如果映射失败
     */
    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setId(rs.getInt("id"));
        category.setUserId(rs.getInt("user_id"));
        category.setName(rs.getString("name"));
        category.setColor(rs.getString("color"));
        
        return category;
    }
}