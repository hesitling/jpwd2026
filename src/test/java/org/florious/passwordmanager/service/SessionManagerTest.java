package org.florious.passwordmanager.service;

import org.florious.passwordmanager.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SessionManager单元测试
 */
@DisplayName("SessionManager 测试")
class SessionManagerTest {

    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        // 重置单例实例
        SessionManager.resetInstance();
        sessionManager = SessionManager.getInstance(true); // 使用测试模式
    }

    @Nested
    @DisplayName("会话创建测试")
    class SessionCreationTests {

        @Test
        @DisplayName("应该成功创建会话")
        void shouldCreateSession() {
            // Given
            User user = createTestUser();
            String masterPassword = "testPassword123";

            // When
            sessionManager.createSession(user, masterPassword);

            // Then
            assertTrue(sessionManager.hasActiveSession());
            assertNotNull(sessionManager.getCurrentSession());
        }

        @Test
        @DisplayName("创建会话时应该销毁现有会话")
        void shouldDestroyExistingSessionWhenCreatingNew() {
            // Given
            User user1 = createTestUser();
            User user2 = createTestUser();
            String masterPassword = "testPassword123";

            sessionManager.createSession(user1, masterPassword);
            assertTrue(sessionManager.hasActiveSession());

            // When
            sessionManager.createSession(user2, masterPassword);

            // Then
            assertTrue(sessionManager.hasActiveSession());
            Session session = sessionManager.getCurrentSession();
            assertNotNull(session);
            assertEquals(user2.getId(), session.getUser().getId());
        }
    }

    @Nested
    @DisplayName("会话销毁测试")
    class SessionDestructionTests {

        @Test
        @DisplayName("应该成功销毁会话")
        void shouldDestroySession() {
            // Given
            User user = createTestUser();
            String masterPassword = "testPassword123";
            sessionManager.createSession(user, masterPassword);
            assertTrue(sessionManager.hasActiveSession());

            // When
            sessionManager.destroySession();

            // Then
            assertFalse(sessionManager.hasActiveSession());
            assertNull(sessionManager.getCurrentSession());
        }

        @Test
        @DisplayName("销毁不存在的会话应该安全")
        void shouldSafelyDestroyNonExistentSession() {
            // Given
            assertFalse(sessionManager.hasActiveSession());

            // When & Then
            assertDoesNotThrow(() -> sessionManager.destroySession());
        }
    }

    @Nested
    @DisplayName("会话超时测试")
    class SessionTimeoutTests {

        @Test
        @DisplayName("新创建的会话不应该超时")
        void newlyCreatedSessionShouldNotBeTimedOut() {
            // Given
            User user = createTestUser();
            String masterPassword = "testPassword123";
            sessionManager.createSession(user, masterPassword);

            // When
            boolean isTimedOut = sessionManager.isSessionTimedOut();

            // Then
            assertFalse(isTimedOut);
        }

        @Test
        @DisplayName("应该正确计算会话剩余时间")
        void shouldCalculateSessionRemainingTime() {
            // Given
            User user = createTestUser();
            String masterPassword = "testPassword123";
            sessionManager.createSession(user, masterPassword);

            // When
            long remainingSeconds = sessionManager.getSessionRemainingSeconds();

            // Then
            assertTrue(remainingSeconds > 0);
            // 默认超时时间是5分钟（300秒）
            assertTrue(remainingSeconds <= 300);
        }

        @Test
        @DisplayName("应该正确重置会话超时")
        void shouldResetSessionTimeout() {
            // Given
            User user = createTestUser();
            String masterPassword = "testPassword123";
            sessionManager.createSession(user, masterPassword);

            // 获取初始剩余时间
            long initialRemaining = sessionManager.getSessionRemainingSeconds();

            // When
            sessionManager.resetTimeout();
            long newRemaining = sessionManager.getSessionRemainingSeconds();

            // Then
            // 重置后剩余时间应该大于或等于初始剩余时间（可能有微小时间差）
            assertTrue(newRemaining >= initialRemaining - 1); // 允许1秒误差
        }
    }

    @Nested
    @DisplayName("会话监听器测试")
    class SessionListenerTests {

        @Test
        @DisplayName("应该能够添加和移除会话监听器")
        void shouldAddAndRemoveSessionListener() {
            // Given
            TestSessionListener listener = new TestSessionListener();

            // When
            sessionManager.addSessionListener(listener);
            sessionManager.removeSessionListener(listener);

            // Then
            // 没有异常抛出
        }

        @Test
        @DisplayName("创建会话时应该通知监听器")
        void shouldNotifyListenerOnSessionCreation() {
            // Given
            TestSessionListener listener = new TestSessionListener();
            sessionManager.addSessionListener(listener);

            User user = createTestUser();
            String masterPassword = "testPassword123";

            // When
            sessionManager.createSession(user, masterPassword);

            // Then
            assertTrue(listener.sessionCreated);
            assertFalse(listener.sessionDestroyed);
        }

        @Test
        @DisplayName("销毁会话时应该通知监听器")
        void shouldNotifyListenerOnSessionDestruction() {
            // Given
            TestSessionListener listener = new TestSessionListener();
            sessionManager.addSessionListener(listener);

            User user = createTestUser();
            String masterPassword = "testPassword123";
            sessionManager.createSession(user, masterPassword);
            listener.sessionCreated = false; // 重置标志

            // When
            sessionManager.destroySession();

            // Then
            assertTrue(listener.sessionDestroyed);
            assertFalse(listener.sessionCreated);
        }
    }

    // 辅助方法
    private User createTestUser() {
        User user = new User();
        user.setId(1);
        user.setUsername("testuser");
        user.setPasswordHash("hash");
        user.setSalt("salt");
        user.setVaultSalt("vaultSalt");
        user.setCreatedAt(LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now());
        return user;
    }

    // 测试监听器实现
    private static class TestSessionListener implements SessionManager.SessionListener {
        boolean sessionCreated = false;
        boolean sessionDestroyed = false;
        Session session = null;

        @Override
        public void onSessionCreated(Session session) {
            this.sessionCreated = true;
            this.session = session;
        }

        @Override
        public void onSessionDestroyed() {
            this.sessionDestroyed = true;
        }

        @Override
        public void onSessionTimeout() {
            // 测试用
        }
    }
}