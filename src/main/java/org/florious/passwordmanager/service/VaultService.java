package org.florious.passwordmanager.service;

import org.florious.passwordmanager.crypto.CryptoService;
import org.florious.passwordmanager.model.PasswordEntry;
import org.florious.passwordmanager.repository.PasswordRepository;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 密码库服务类
 * 处理密码条目的加密存储、解密读取和管理
 */
public class VaultService {
    private final PasswordRepository passwordRepository;
    private final CryptoService cryptoService;
    private final SessionManager sessionManager;

    public VaultService() {
        this(false);
    }

    /**
     * 创建 VaultService
     * @param testMode 测试模式下使用轻量级加密参数
     */
    public VaultService(boolean testMode) {
        this.passwordRepository = new PasswordRepository();
        this.cryptoService = new CryptoService(testMode);
        this.sessionManager = SessionManager.getInstance(testMode);
    }

    /**
     * 添加密码条目
     * @param title 标题
     * @param username 用户名
     * @param plainPassword 明文密码
     * @param url URL
     * @param notes 备注
     * @param categoryId 分类ID（可为null）
     * @return 创建的密码条目
     * @throws VaultException 如果添加失败
     */
    public PasswordEntry addPassword(String title, String username, String plainPassword,
                                     String url, String notes, Integer categoryId) throws VaultException {
        // 验证输入
        validatePasswordEntryInput(title, plainPassword);

        // 获取当前会话
        Session session = getCurrentSession();

        try {
            // 使用会话中的派生密钥加密密码
            byte[] key = session.getDerivedKey();
            String encryptedPassword = cryptoService.encryptPassword(plainPassword, key);

            // 创建密码条目对象
            PasswordEntry entry = new PasswordEntry(
                    session.getUser().getId(),
                    title,
                    username,
                    encryptedPassword,
                    extractIVFromEncrypted(encryptedPassword) // IV已包含在加密结果中
            );
            entry.setUrl(url);
            entry.setNotes(notes);
            entry.setCategoryId(categoryId);

            // 保存到数据库
            return passwordRepository.create(entry);
        } catch (Exception e) {
            throw new VaultException("添加密码条目失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取密码条目（解密密码）
     * @param entryId 条目ID
     * @return 解密后的密码条目信息
     * @throws VaultException 如果获取失败
     */
    public DecryptedPasswordEntry getPassword(int entryId) throws VaultException {
        // 获取当前会话
        Session session = getCurrentSession();

        try {
            // 从数据库获取条目
            PasswordEntry entry = passwordRepository.findById(entryId);
            if (entry == null) {
                throw new VaultException("密码条目不存在");
            }

            // 验证条目属于当前用户
            if (entry.getUserId() != session.getUser().getId()) {
                throw new VaultException("无权访问此密码条目");
            }

            // 解密密码
            byte[] key = session.getDerivedKey();
            String decryptedPassword = cryptoService.decryptPassword(entry.getEncryptedPassword(), key);

            // 返回解密后的条目信息
            return new DecryptedPasswordEntry(entry, decryptedPassword);
        } catch (VaultException e) {
            throw e;
        } catch (Exception e) {
            throw new VaultException("获取密码条目失败: " + e.getMessage(), e);
        }
    }

    /**
     * 更新密码条目
     * @param entryId 条目ID
     * @param title 标题
     * @param username 用户名
     * @param plainPassword 明文密码（如果为null则不更新密码）
     * @param url URL
     * @param notes 备注
     * @param categoryId 分类ID
     * @return 更新后的密码条目
     * @throws VaultException 如果更新失败
     */
    public PasswordEntry updatePassword(int entryId, String title, String username,
                                        String plainPassword, String url, String notes,
                                        Integer categoryId) throws VaultException {
        // 验证输入
        if (title == null || title.trim().isEmpty()) {
            throw new VaultException("标题不能为空");
        }

        // 获取当前会话
        Session session = getCurrentSession();

        try {
            // 从数据库获取现有条目
            PasswordEntry existingEntry = passwordRepository.findById(entryId);
            if (existingEntry == null) {
                throw new VaultException("密码条目不存在");
            }

            // 验证条目属于当前用户
            if (existingEntry.getUserId() != session.getUser().getId()) {
                throw new VaultException("无权修改此密码条目");
            }

            // 更新字段
            existingEntry.setTitle(title);
            existingEntry.setUsername(username);
            existingEntry.setUrl(url);
            existingEntry.setNotes(notes);
            existingEntry.setCategoryId(categoryId);
            existingEntry.setUpdatedAt(LocalDateTime.now());

            // 如果提供了新密码，重新加密
            if (plainPassword != null && !plainPassword.isEmpty()) {
                byte[] key = session.getDerivedKey();
                String encryptedPassword = cryptoService.encryptPassword(plainPassword, key);
                existingEntry.setEncryptedPassword(encryptedPassword);
                existingEntry.setIv(extractIVFromEncrypted(encryptedPassword));
            }

            // 保存到数据库
            passwordRepository.update(existingEntry);
            return existingEntry;
        } catch (VaultException e) {
            throw e;
        } catch (Exception e) {
            throw new VaultException("更新密码条目失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除密码条目
     * @param entryId 条目ID
     * @throws VaultException 如果删除失败
     */
    public void deletePassword(int entryId) throws VaultException {
        // 获取当前会话
        Session session = getCurrentSession();

        try {
            // 从数据库获取条目
            PasswordEntry entry = passwordRepository.findById(entryId);
            if (entry == null) {
                throw new VaultException("密码条目不存在");
            }

            // 验证条目属于当前用户
            if (entry.getUserId() != session.getUser().getId()) {
                throw new VaultException("无权删除此密码条目");
            }

            // 删除条目
            passwordRepository.delete(entryId);
        } catch (VaultException e) {
            throw e;
        } catch (Exception e) {
            throw new VaultException("删除密码条目失败: " + e.getMessage(), e);
        }
    }

    /**
     * 搜索密码条目
     * @param query 搜索关键词
     * @return 匹配的密码条目列表（密码字段为加密存储）
     * @throws VaultException 如果搜索失败
     */
    public List<PasswordEntry> searchPasswords(String query) throws VaultException {
        // 获取当前会话
        Session session = getCurrentSession();

        try {
            if (query == null || query.isEmpty()) {
                return passwordRepository.findByUserId(session.getUser().getId());
            }
            return passwordRepository.search(session.getUser().getId(), query);
        } catch (Exception e) {
            throw new VaultException("搜索密码条目失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取所有密码条目
     * @return 当前用户的所有密码条目（密码字段为加密存储）
     * @throws VaultException 如果获取失败
     */
    public List<PasswordEntry> getAllPasswords() throws VaultException {
        // 获取当前会话
        Session session = getCurrentSession();

        try {
            return passwordRepository.findByUserId(session.getUser().getId());
        } catch (Exception e) {
            throw new VaultException("获取密码条目失败: " + e.getMessage(), e);
        }
    }

    /**
     * 按分类获取密码条目
     * @param categoryId 分类ID
     * @return 该分类下的密码条目列表
     * @throws VaultException 如果获取失败
     */
    public List<PasswordEntry> getPasswordsByCategory(int categoryId) throws VaultException {
        // 获取当前会话
        Session session = getCurrentSession();

        try {
            // 直接通过用户ID和分类ID查询，无需内存过滤
            return passwordRepository.findByCategoryId(categoryId, session.getUser().getId());
        } catch (Exception e) {
            throw new VaultException("获取分类密码条目失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取密码条目总数
     * @return 当前用户的密码条目总数
     * @throws VaultException 如果获取失败
     */
    public int getPasswordCount() throws VaultException {
        Session session = getCurrentSession();

        try {
            return passwordRepository.countByUserId(session.getUser().getId());
        } catch (Exception e) {
            throw new VaultException("获取密码条目数量失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证密码条目输入
     * @param title 标题
     * @param plainPassword 密码
     * @throws VaultException 如果验证失败
     */
    private void validatePasswordEntryInput(String title, String plainPassword) throws VaultException {
        if (title == null || title.trim().isEmpty()) {
            throw new VaultException("标题不能为空");
        }
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new VaultException("密码不能为空");
        }
    }

    /**
     * 获取当前会话
     * @return 当前会话
     * @throws VaultException 如果未登录
     */
    private Session getCurrentSession() throws VaultException {
        Session session = sessionManager.getCurrentSession();
        if (session == null) {
            throw new VaultException("用户未登录");
        }
        return session;
    }

    /**
     * 从加密结果中提取IV
     * CryptoService.encryptPassword 输出格式：Base64(IV[12字节] + 密文)
     * @param encryptedPassword 加密后的密码（Base64编码）
     * @return IV的Base64编码
     */
    private String extractIVFromEncrypted(String encryptedPassword) {
        try {
            byte[] decoded = java.util.Base64.getDecoder().decode(encryptedPassword);
            // IV 是前 12 字节
            byte[] iv = new byte[12];
            System.arraycopy(decoded, 0, iv, 0, 12);
            return java.util.Base64.getEncoder().encodeToString(iv);
        } catch (Exception e) {
            // 如果解析失败，返回空字符串（兼容性处理）
            return "";
        }
    }

    /**
     * 解密后的密码条目内部类
     */
    public static class DecryptedPasswordEntry {
        private final PasswordEntry entry;
        private final String decryptedPassword;

        public DecryptedPasswordEntry(PasswordEntry entry, String decryptedPassword) {
            this.entry = entry;
            this.decryptedPassword = decryptedPassword;
        }

        public PasswordEntry getEntry() {
            return entry;
        }

        public String getDecryptedPassword() {
            return decryptedPassword;
        }

        public int getId() {
            return entry.getId();
        }

        public String getTitle() {
            return entry.getTitle();
        }

        public String getUsername() {
            return entry.getUsername();
        }

        public String getUrl() {
            return entry.getUrl();
        }

        public String getNotes() {
            return entry.getNotes();
        }

        public Integer getCategoryId() {
            return entry.getCategoryId();
        }

        public LocalDateTime getCreatedAt() {
            return entry.getCreatedAt();
        }

        public LocalDateTime getUpdatedAt() {
            return entry.getUpdatedAt();
        }
    }

    /**
     * 密码库异常类
     */
    public static class VaultException extends Exception {
        public VaultException(String message) {
            super(message);
        }

        public VaultException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
