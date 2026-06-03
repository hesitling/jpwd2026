package org.florious.passwordmanager.service;

import org.florious.passwordmanager.crypto.CryptoService;
import org.florious.passwordmanager.model.User;
import org.florious.passwordmanager.repository.UserRepository;

import java.sql.SQLException;

/**
 * 认证服务类
 * 处理用户注册、登录、登出等认证相关操作
 */
public class AuthService {
    private final UserRepository userRepository;
    private final CryptoService cryptoService;
    private final SessionManager sessionManager;

    public AuthService() {
        this.userRepository = new UserRepository();
        this.cryptoService = new CryptoService();
        this.sessionManager = SessionManager.getInstance();
    }

    /**
     * 用户注册
     * @param username 用户名
     * @param masterPassword 主密码
     * @return 注册成功的用户对象
     * @throws AuthException 如果注册失败
     */
    public User register(String username, String masterPassword) throws AuthException {
        // 验证输入
        validateRegistrationInput(username, masterPassword);

        try {
            // 检查用户名是否已存在
            if (userRepository.existsByUsername(username)) {
                throw new AuthException("用户名已存在");
            }

            // 生成盐值
            String salt = cryptoService.generateSalt();

            // 使用Argon2id哈希密码
            String passwordHash = cryptoService.hashMasterPassword(masterPassword, salt);

            // 创建用户对象
            User user = new User(username, passwordHash, salt);

            // 保存到数据库
            return userRepository.create(user);
        } catch (SQLException e) {
            throw new AuthException("注册失败: " + e.getMessage(), e);
        }
    }

    /**
     * 用户登录
     * @param username 用户名
     * @param masterPassword 主密码
     * @return 登录成功的用户对象
     * @throws AuthException 如果登录失败
     */
    public User login(String username, String masterPassword) throws AuthException {
        // 验证输入
        if (username == null || username.isEmpty()) {
            throw new AuthException("用户名不能为空");
        }
        if (masterPassword == null || masterPassword.isEmpty()) {
            throw new AuthException("密码不能为空");
        }

        try {
            // 查找用户
            User user = userRepository.findByUsername(username);
            if (user == null) {
                // 统一错误消息防止用户名枚举
                throw new AuthException("用户名或密码错误");
            }

            // 验证密码
            boolean passwordValid = cryptoService.verifyMasterPassword(
                    masterPassword, 
                    user.getPasswordHash(), 
                    user.getSalt()
            );

            if (!passwordValid) {
                // 统一错误消息防止用户名枚举
                throw new AuthException("用户名或密码错误");
            }

            // 更新最后登录时间
            userRepository.updateLastLogin(user.getId());

            // 创建会话
            sessionManager.createSession(user, masterPassword);

            return user;
        } catch (SQLException e) {
            throw new AuthException("登录失败: " + e.getMessage(), e);
        }
    }

    /**
     * 用户登出
     */
    public void logout() {
        sessionManager.destroySession();
    }

    /**
     * 验证注册输入
     * @param username 用户名
     * @param masterPassword 主密码
     * @throws AuthException 如果验证失败
     */
    private void validateRegistrationInput(String username, String masterPassword) throws AuthException {
        if (username == null || username.trim().isEmpty()) {
            throw new AuthException("用户名不能为空");
        }
        if (username.length() < 3) {
            throw new AuthException("用户名长度必须至少3个字符");
        }
        if (username.length() > 50) {
            throw new AuthException("用户名长度不能超过50个字符");
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            throw new AuthException("用户名只能包含字母、数字和下划线");
        }
        if (masterPassword == null || masterPassword.isEmpty()) {
            throw new AuthException("密码不能为空");
        }
        if (masterPassword.length() < 8) {
            throw new AuthException("密码长度必须至少8个字符");
        }
        if (masterPassword.length() > 128) {
            throw new AuthException("密码长度不能超过128个字符");
        }
    }

    /**
     * 获取当前登录用户
     * @return 当前用户，如果未登录返回null
     */
    public User getCurrentUser() {
        Session session = sessionManager.getCurrentSession();
        return session != null ? session.getUser() : null;
    }

    /**
     * 检查用户是否已登录
     * @return 如果已登录返回true
     */
    public boolean isLoggedIn() {
        return sessionManager.hasActiveSession();
    }

    /**
     * 认证异常类
     */
    public static class AuthException extends Exception {
        public AuthException(String message) {
            super(message);
        }

        public AuthException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
