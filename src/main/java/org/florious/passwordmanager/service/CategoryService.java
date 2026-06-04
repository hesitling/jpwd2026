package org.florious.passwordmanager.service;

import org.florious.passwordmanager.model.Category;
import org.florious.passwordmanager.model.PasswordEntry;
import org.florious.passwordmanager.repository.CategoryRepository;
import org.florious.passwordmanager.repository.PasswordRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * 分类服务类
 * 处理分类的业务逻辑，包括CRUD操作和验证
 */
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final PasswordRepository passwordRepository;
    private final SessionManager sessionManager;

    public CategoryService() {
        this(false);
    }

    /**
     * 创建 CategoryService
     * @param testMode 测试模式
     */
    public CategoryService(boolean testMode) {
        this.categoryRepository = new CategoryRepository();
        this.passwordRepository = new PasswordRepository();
        this.sessionManager = SessionManager.getInstance(testMode);
    }

    /**
     * 创建新分类
     * @param name 分类名称
     * @param color 分类颜色（可为null）
     * @return 创建的分类
     * @throws CategoryException 如果创建失败
     */
    public Category createCategory(String name, String color) throws CategoryException {
        // 规范化和验证输入
        name = normalizeCategoryName(name);
        validateCategoryName(name);

        // 获取当前会话
        Session session = getCurrentSession();

        try {
            // 检查名称是否已存在
            if (categoryRepository.existsByUserIdAndName(session.getUser().getId(), name)) {
                throw new CategoryException("分类名称已存在");
            }

            // 创建分类
            Category category = new Category(session.getUser().getId(), name, color);
            return categoryRepository.create(category);
        } catch (CategoryException e) {
            throw e;
        } catch (Exception e) {
            throw new CategoryException("创建分类失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取分类详情
     * @param categoryId 分类ID
     * @return 分类对象
     * @throws CategoryException 如果获取失败
     */
    public Category getCategory(int categoryId) throws CategoryException {
        Session session = getCurrentSession();

        try {
            Category category = categoryRepository.findById(categoryId);
            if (category == null) {
                throw new CategoryException("分类不存在");
            }

            // 验证分类属于当前用户
            if (category.getUserId() != session.getUser().getId()) {
                throw new CategoryException("无权访问此分类");
            }

            return category;
        } catch (CategoryException e) {
            throw e;
        } catch (Exception e) {
            throw new CategoryException("获取分类失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取当前用户的所有分类
     * @return 分类列表
     * @throws CategoryException 如果获取失败
     */
    public List<Category> getAllCategories() throws CategoryException {
        Session session = getCurrentSession();

        try {
            return categoryRepository.findByUserId(session.getUser().getId());
        } catch (Exception e) {
            throw new CategoryException("获取分类列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 更新分类
     * @param categoryId 分类ID
     * @param name 新名称
     * @param color 新颜色
     * @return 更新后的分类
     * @throws CategoryException 如果更新失败
     */
    public Category updateCategory(int categoryId, String name, String color) throws CategoryException {
        // 规范化和验证输入
        name = normalizeCategoryName(name);
        validateCategoryName(name);

        Session session = getCurrentSession();

        try {
            // 获取现有分类
            Category existingCategory = categoryRepository.findById(categoryId);
            if (existingCategory == null) {
                throw new CategoryException("分类不存在");
            }

            // 验证分类属于当前用户
            if (existingCategory.getUserId() != session.getUser().getId()) {
                throw new CategoryException("无权修改此分类");
            }

            // 检查名称是否与其他分类冲突
            if (!existingCategory.getName().equals(name)) {
                if (categoryRepository.existsByUserIdAndName(session.getUser().getId(), name)) {
                    throw new CategoryException("分类名称已存在");
                }
            }

            // 更新分类
            existingCategory.setName(name);
            existingCategory.setColor(color);
            categoryRepository.update(existingCategory);

            return existingCategory;
        } catch (CategoryException e) {
            throw e;
        } catch (Exception e) {
            throw new CategoryException("更新分类失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除分类
     * @param categoryId 分类ID
     * @return 删除结果信息
     * @throws CategoryException 如果删除失败
     */
    public DeleteResult deleteCategory(int categoryId) throws CategoryException {
        Session session = getCurrentSession();

        try {
            // 获取分类
            Category category = categoryRepository.findById(categoryId);
            if (category == null) {
                throw new CategoryException("分类不存在");
            }

            // 验证分类属于当前用户
            if (category.getUserId() != session.getUser().getId()) {
                throw new CategoryException("无权删除此分类");
            }

            // 检查是否有密码条目使用该分类
            int entryCount = categoryRepository.countPasswordEntries(categoryId);

            // 先解除关联的密码条目
            if (entryCount > 0) {
                dissociateAllEntriesFromCategory(categoryId);
            }

            // 删除分类
            boolean deleted = categoryRepository.delete(categoryId);
            if (!deleted) {
                throw new CategoryException("删除分类失败");
            }

            return new DeleteResult(true, entryCount);
        } catch (CategoryException e) {
            throw e;
        } catch (Exception e) {
            throw new CategoryException("删除分类失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取分类下的密码条目数量
     * @param categoryId 分类ID
     * @return 密码条目数量
     * @throws CategoryException 如果查询失败
     */
    public int getPasswordEntryCount(int categoryId) throws CategoryException {
        Session session = getCurrentSession();

        try {
            // 验证分类存在且属于当前用户
            Category category = categoryRepository.findById(categoryId);
            if (category == null || category.getUserId() != session.getUser().getId()) {
                throw new CategoryException("分类不存在");
            }

            return categoryRepository.countPasswordEntries(categoryId);
        } catch (CategoryException e) {
            throw e;
        } catch (Exception e) {
            throw new CategoryException("获取密码条目数量失败: " + e.getMessage(), e);
        }
    }

    /**
     * 关联密码条目到分类
     * @param entryId 密码条目ID
     * @param categoryId 分类ID
     * @throws CategoryException 如果关联失败
     */
    public void associateEntryToCategory(int entryId, int categoryId) throws CategoryException {
        Session session = getCurrentSession();

        try {
            // 验证分类存在且属于当前用户
            Category category = categoryRepository.findById(categoryId);
            if (category == null || category.getUserId() != session.getUser().getId()) {
                throw new CategoryException("分类不存在");
            }

            // 获取密码条目
            PasswordEntry entry = passwordRepository.findById(entryId);
            if (entry == null || entry.getUserId() != session.getUser().getId()) {
                throw new CategoryException("密码条目不存在");
            }

            // 更新分类关联
            entry.setCategoryId(categoryId);
            passwordRepository.update(entry);
        } catch (CategoryException e) {
            throw e;
        } catch (Exception e) {
            throw new CategoryException("关联密码条目到分类失败: " + e.getMessage(), e);
        }
    }

    /**
     * 取消密码条目的分类关联
     * @param entryId 密码条目ID
     * @throws CategoryException 如果取消关联失败
     */
    public void dissociateEntryFromCategory(int entryId) throws CategoryException {
        Session session = getCurrentSession();

        try {
            // 获取密码条目
            PasswordEntry entry = passwordRepository.findById(entryId);
            if (entry == null || entry.getUserId() != session.getUser().getId()) {
                throw new CategoryException("密码条目不存在");
            }

            // 取消分类关联
            entry.setCategoryId(null);
            passwordRepository.update(entry);
        } catch (CategoryException e) {
            throw e;
        } catch (Exception e) {
            throw new CategoryException("取消分类关联失败: " + e.getMessage(), e);
        }
    }

    /**
     * 取消分类下所有密码条目的关联
     * @param categoryId 分类ID
     * @return 取消关联的条目数量
     * @throws CategoryException 如果操作失败
     */
    public int dissociateAllEntriesFromCategory(int categoryId) throws CategoryException {
        Session session = getCurrentSession();

        try {
            // 验证分类存在且属于当前用户
            Category category = categoryRepository.findById(categoryId);
            if (category == null || category.getUserId() != session.getUser().getId()) {
                throw new CategoryException("分类不存在");
            }

            // 获取该分类下的所有密码条目
            List<PasswordEntry> entries = passwordRepository.findByCategoryId(
                    categoryId, session.getUser().getId());

            // 取消关联
            for (PasswordEntry entry : entries) {
                entry.setCategoryId(null);
                passwordRepository.update(entry);
            }

            return entries.size();
        } catch (CategoryException e) {
            throw e;
        } catch (Exception e) {
            throw new CategoryException("取消分类关联失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证分类名称
     * @param name 分类名称
     * @throws CategoryException 如果验证失败
     */
    private String normalizeCategoryName(String name) {
        if (name == null) {
            return null;
        }
        return name.trim();
    }

    private void validateCategoryName(String name) throws CategoryException {
        if (name == null || name.isEmpty()) {
            throw new CategoryException("分类名称不能为空");
        }
        if (name.length() > 50) {
            throw new CategoryException("分类名称不能超过50个字符");
        }
    }

    /**
     * 获取当前会话
     * @return 当前会话
     * @throws CategoryException 如果未登录
     */
    private Session getCurrentSession() throws CategoryException {
        Session session = sessionManager.getCurrentSession();
        if (session == null) {
            throw new CategoryException("用户未登录");
        }
        return session;
    }

    /**
     * 删除结果内部类
     */
    public static class DeleteResult {
        private final boolean success;
        private final int affectedEntries;

        public DeleteResult(boolean success, int affectedEntries) {
            this.success = success;
            this.affectedEntries = affectedEntries;
        }

        public boolean isSuccess() {
            return success;
        }

        public int getAffectedEntries() {
            return affectedEntries;
        }
    }

    /**
     * 分类异常类
     */
    public static class CategoryException extends Exception {
        public CategoryException(String message) {
            super(message);
        }

        public CategoryException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
