package org.florious.passwordmanager.service;

import org.florious.passwordmanager.model.User;
import org.florious.passwordmanager.repository.DatabaseManager;
import org.florious.passwordmanager.repository.UserRepository;
import org.junit.jupiter.api.*;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AuthService单元测试
 */
@DisplayName("AuthService 测试")
class AuthServiceTest {

    private AuthService authService;
    private UserRepository userRepository;

    @BeforeAll
    static void setUpClass() {
        // 初始化数据库（通过获取实例触发初始化）
        DatabaseManager.getInstance();
    }

    @BeforeEach
    void setUp() {
        authService = new AuthService();
        userRepository = new UserRepository();
        
        // 清理测试数据
        try {
            User testUser = userRepository.findByUsername("testuser");
            if (testUser != null) {
                userRepository.delete(testUser.getId());
            }
        } catch (SQLException e) {
            // 忽略
        }
    }

    @AfterEach
    void tearDown() {
        // 登出
        authService.logout();
    }

    @Nested
    @DisplayName("用户注册测试")
    class RegistrationTests {

        @Test
        @DisplayName("应该成功注册新用户")
        void shouldRegisterNewUser() throws AuthService.AuthException {
            // Given
            String username = "testuser";
            String password = "TestPassword123!";

            // When
            User user = authService.register(username, password);

            // Then
            assertNotNull(user);
            assertEquals(username, user.getUsername());
            assertNotNull(user.getPasswordHash());
            assertNotNull(user.getSalt());
            assertTrue(user.getId() > 0);
        }

        @Test
        @DisplayName("应该拒绝已存在的用户名")
        void shouldRejectExistingUsername() {
            // Given
            String username = "testuser";
            String password = "TestPassword123!";

            // When & Then
            assertThrows(AuthService.AuthException.class, () -> {
                authService.register(username, password);
                authService.register(username, "AnotherPassword456!");
            });
        }

        @Test
        @DisplayName("应该拒绝短用户名")
        void shouldRejectShortUsername() {
            // Given
            String username = "ab"; // 少于3个字符
            String password = "TestPassword123!";

            // When & Then
            AuthService.AuthException exception = assertThrows(AuthService.AuthException.class, () -> {
                authService.register(username, password);
            });
            assertTrue(exception.getMessage().contains("用户名长度必须至少3个字符"));
        }

        @Test
        @DisplayName("应该拒绝包含特殊字符的用户名")
        void shouldRejectUsernameWithSpecialChars() {
            // Given
            String username = "test@user";
            String password = "TestPassword123!";

            // When & Then
            AuthService.AuthException exception = assertThrows(AuthService.AuthException.class, () -> {
                authService.register(username, password);
            });
            assertTrue(exception.getMessage().contains("用户名只能包含字母、数字和下划线"));
        }

        @Test
        @DisplayName("应该拒绝短密码")
        void shouldRejectShortPassword() {
            // Given
            String username = "testuser";
            String password = "Short1!"; // 少于8个字符

            // When & Then
            AuthService.AuthException exception = assertThrows(AuthService.AuthException.class, () -> {
                authService.register(username, password);
            });
            assertTrue(exception.getMessage().contains("密码长度必须至少8个字符"));
        }

        @Test
        @DisplayName("应该拒绝空用户名")
        void shouldRejectEmptyUsername() {
            // Given
            String username = "";
            String password = "TestPassword123!";

            // When & Then
            assertThrows(AuthService.AuthException.class, () -> {
                authService.register(username, password);
            });
        }

        @Test
        @DisplayName("应该拒绝空密码")
        void shouldRejectEmptyPassword() {
            // Given
            String username = "testuser";
            String password = "";

            // When & Then
            assertThrows(AuthService.AuthException.class, () -> {
                authService.register(username, password);
            });
        }
    }

    @Nested
    @DisplayName("用户登录测试")
    class LoginTests {

        @Test
        @DisplayName("应该成功登录")
        void shouldLoginSuccessfully() throws AuthService.AuthException {
            // Given
            String username = "testuser";
            String password = "TestPassword123!";
            authService.register(username, password);

            // When
            User user = authService.login(username, password);

            // Then
            assertNotNull(user);
            assertEquals(username, user.getUsername());
            assertTrue(authService.isLoggedIn());
        }

        @Test
        @DisplayName("应该拒绝错误密码")
        void shouldRejectWrongPassword() {
            // Given
            String username = "testuser";
            String password = "TestPassword123!";
            String wrongPassword = "WrongPassword456!";

            // When & Then
            assertThrows(AuthService.AuthException.class, () -> {
                authService.register(username, password);
                authService.login(username, wrongPassword);
            });
        }

        @Test
        @DisplayName("应该拒绝不存在的用户")
        void shouldRejectNonExistentUser() {
            // Given
            String username = "nonexistent";
            String password = "TestPassword123!";

            // When & Then
            AuthService.AuthException exception = assertThrows(AuthService.AuthException.class, () -> {
                authService.login(username, password);
            });
            assertTrue(exception.getMessage().contains("用户不存在"));
        }

        @Test
        @DisplayName("应该拒绝空用户名登录")
        void shouldRejectEmptyUsernameForLogin() {
            // Given
            String username = "";
            String password = "TestPassword123!";

            // When & Then
            assertThrows(AuthService.AuthException.class, () -> {
                authService.login(username, password);
            });
        }

        @Test
        @DisplayName("应该拒绝空密码登录")
        void shouldRejectEmptyPasswordForLogin() {
            // Given
            String username = "testuser";
            String password = "";

            // When & Then
            assertThrows(AuthService.AuthException.class, () -> {
                authService.login(username, password);
            });
        }
    }

    @Nested
    @DisplayName("用户登出测试")
    class LogoutTests {

        @Test
        @DisplayName("应该成功登出")
        void shouldLogoutSuccessfully() throws AuthService.AuthException {
            // Given
            String username = "testuser";
            String password = "TestPassword123!";
            authService.register(username, password);
            authService.login(username, password);
            assertTrue(authService.isLoggedIn());

            // When
            authService.logout();

            // Then
            assertFalse(authService.isLoggedIn());
            assertNull(authService.getCurrentUser());
        }

        @Test
        @DisplayName("登出后应该能够重新登录")
        void shouldReLoginAfterLogout() throws AuthService.AuthException {
            // Given
            String username = "testuser";
            String password = "TestPassword123!";
            authService.register(username, password);
            authService.login(username, password);
            authService.logout();

            // When
            User user = authService.login(username, password);

            // Then
            assertNotNull(user);
            assertTrue(authService.isLoggedIn());
        }
    }

    @Nested
    @DisplayName("获取当前用户测试")
    class GetCurrentUserTests {

        @Test
        @DisplayName("登录后应该返回当前用户")
        void shouldReturnCurrentUserAfterLogin() throws AuthService.AuthException {
            // Given
            String username = "testuser";
            String password = "TestPassword123!";
            authService.register(username, password);
            authService.login(username, password);

            // When
            User currentUser = authService.getCurrentUser();

            // Then
            assertNotNull(currentUser);
            assertEquals(username, currentUser.getUsername());
        }

        @Test
        @DisplayName("未登录时应该返回null")
        void shouldReturnNullWhenNotLoggedIn() {
            // When
            User currentUser = authService.getCurrentUser();

            // Then
            assertNull(currentUser);
        }
    }

    @Nested
    @DisplayName("会话管理测试")
    class SessionManagementTests {

        @Test
        @DisplayName("登录后应该创建会话")
        void shouldCreateSessionAfterLogin() throws AuthService.AuthException {
            // Given
            String username = "testuser";
            String password = "TestPassword123!";
            authService.register(username, password);

            // When
            authService.login(username, password);

            // Then
            SessionManager sessionManager = SessionManager.getInstance();
            assertTrue(sessionManager.hasActiveSession());
        }

        @Test
        @DisplayName("登出后应该销毁会话")
        void shouldDestroySessionAfterLogout() throws AuthService.AuthException {
            // Given
            String username = "testuser";
            String password = "TestPassword123!";
            authService.register(username, password);
            authService.login(username, password);

            // When
            authService.logout();

            // Then
            SessionManager sessionManager = SessionManager.getInstance();
            assertFalse(sessionManager.hasActiveSession());
        }
    }
}
