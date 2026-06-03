package org.florious.passwordmanager.service;

import org.florious.passwordmanager.model.PasswordEntry;
import org.florious.passwordmanager.model.User;
import org.florious.passwordmanager.repository.DatabaseManager;
import org.florious.passwordmanager.repository.PasswordRepository;
import org.florious.passwordmanager.repository.UserRepository;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * VaultService单元测试
 */
@DisplayName("VaultService 测试")
class VaultServiceTest {

    private AuthService authService;
    private VaultService vaultService;
    private UserRepository userRepository;
    private PasswordRepository passwordRepository;
    private User testUser;

    @BeforeAll
    static void setUpClass() {
        // 使用内存数据库进行测试
        DatabaseManager.getInstance(":memory:");
    }

    @AfterAll
    static void tearDownClass() {
        // 重置数据库实例
        DatabaseManager.resetInstance();
    }

    @BeforeEach
    void setUp() throws AuthService.AuthException {
        authService = new AuthService();
        vaultService = new VaultService();
        userRepository = new UserRepository();
        passwordRepository = new PasswordRepository();

        // 清理测试数据
        cleanupTestData();

        // 创建测试用户并登录
        testUser = authService.register("testuser", "TestPassword123!");
        authService.login("testuser", "TestPassword123!");
    }

    @AfterEach
    void tearDown() {
        // 登出
        authService.logout();
        cleanupTestData();
    }

    private void cleanupTestData() {
        try {
            // 删除测试密码条目
            User user = userRepository.findByUsername("testuser");
            if (user != null) {
                List<PasswordEntry> entries = passwordRepository.findByUserId(user.getId());
                for (PasswordEntry entry : entries) {
                    passwordRepository.delete(entry.getId());
                }
                // 删除测试用户
                userRepository.delete(user.getId());
            }
        } catch (SQLException e) {
            // 忽略
        }
    }

    @Nested
    @DisplayName("添加密码测试")
    class AddPasswordTests {

        @Test
        @DisplayName("应该成功添加密码条目")
        void shouldAddPasswordEntry() throws VaultService.VaultException {
            // Given
            String title = "GitHub";
            String username = "test@example.com";
            String password = "MyGitHubPassword123!";
            String url = "https://github.com";
            String notes = "开发账号";
            Integer categoryId = null;

            // When
            PasswordEntry entry = vaultService.addPassword(title, username, password, url, notes, categoryId);

            // Then
            assertNotNull(entry);
            assertEquals(title, entry.getTitle());
            assertEquals(username, entry.getUsername());
            assertNotNull(entry.getEncryptedPassword());
            assertTrue(entry.getId() > 0);
        }

        @Test
        @DisplayName("应该拒绝空标题")
        void shouldRejectEmptyTitle() {
            // Given
            String title = "";
            String username = "test@example.com";
            String password = "MyGitHubPassword123!";

            // When & Then
            VaultService.VaultException exception = assertThrows(VaultService.VaultException.class, () -> {
                vaultService.addPassword(title, username, password, null, null, null);
            });
            assertTrue(exception.getMessage().contains("标题不能为空"));
        }

        @Test
        @DisplayName("应该拒绝空密码")
        void shouldRejectEmptyPassword() {
            // Given
            String title = "GitHub";
            String username = "test@example.com";
            String password = "";

            // When & Then
            VaultService.VaultException exception = assertThrows(VaultService.VaultException.class, () -> {
                vaultService.addPassword(title, username, password, null, null, null);
            });
            assertTrue(exception.getMessage().contains("密码不能为空"));
        }
    }

    @Nested
    @DisplayName("获取密码测试")
    class GetPasswordTests {

        @Test
        @DisplayName("应该成功获取并解密密码")
        void shouldGetAndDecryptPassword() throws VaultService.VaultException {
            // Given
            String title = "GitHub";
            String username = "test@example.com";
            String password = "MyGitHubPassword123!";
            PasswordEntry entry = vaultService.addPassword(title, username, password, null, null, null);

            // When
            VaultService.DecryptedPasswordEntry decrypted = vaultService.getPassword(entry.getId());

            // Then
            assertNotNull(decrypted);
            assertEquals(title, decrypted.getTitle());
            assertEquals(username, decrypted.getUsername());
            assertEquals(password, decrypted.getDecryptedPassword());
        }

        @Test
        @DisplayName("应该拒绝访问不存在的条目")
        void shouldRejectAccessToNonExistentEntry() {
            // Given
            int nonExistentId = 99999;

            // When & Then
            VaultService.VaultException exception = assertThrows(VaultService.VaultException.class, () -> {
                vaultService.getPassword(nonExistentId);
            });
            assertTrue(exception.getMessage().contains("密码条目不存在"));
        }
    }

    @Nested
    @DisplayName("更新密码测试")
    class UpdatePasswordTests {

        @Test
        @DisplayName("应该成功更新密码条目")
        void shouldUpdatePasswordEntry() throws VaultService.VaultException {
            // Given
            String title = "GitHub";
            String username = "test@example.com";
            String password = "MyGitHubPassword123!";
            PasswordEntry entry = vaultService.addPassword(title, username, password, null, null, null);

            // When
            String newTitle = "GitHub Updated";
            String newUsername = "new@example.com";
            String newPassword = "NewPassword456!";
            PasswordEntry updated = vaultService.updatePassword(
                    entry.getId(), newTitle, newUsername, newPassword, "https://github.com", "更新的备注", null
            );

            // Then
            assertNotNull(updated);
            assertEquals(newTitle, updated.getTitle());
            assertEquals(newUsername, updated.getUsername());

            // 验证密码已更新
            VaultService.DecryptedPasswordEntry decrypted = vaultService.getPassword(entry.getId());
            assertEquals(newPassword, decrypted.getDecryptedPassword());
        }

        @Test
        @DisplayName("应该拒绝空标题更新")
        void shouldRejectEmptyTitleUpdate() throws VaultService.VaultException {
            // Given
            String title = "GitHub";
            String username = "test@example.com";
            String password = "MyGitHubPassword123!";
            PasswordEntry entry = vaultService.addPassword(title, username, password, null, null, null);

            // When & Then
            VaultService.VaultException exception = assertThrows(VaultService.VaultException.class, () -> {
                vaultService.updatePassword(entry.getId(), "", username, password, null, null, null);
            });
            assertTrue(exception.getMessage().contains("标题不能为空"));
        }

        @Test
        @DisplayName("应该拒绝更新不存在的条目")
        void shouldRejectUpdateNonExistentEntry() {
            // Given
            int nonExistentId = 99999;

            // When & Then
            VaultService.VaultException exception = assertThrows(VaultService.VaultException.class, () -> {
                vaultService.updatePassword(nonExistentId, "Title", "user", "pass", null, null, null);
            });
            assertTrue(exception.getMessage().contains("密码条目不存在"));
        }
    }

    @Nested
    @DisplayName("删除密码测试")
    class DeletePasswordTests {

        @Test
        @DisplayName("应该成功删除密码条目")
        void shouldDeletePasswordEntry() throws VaultService.VaultException {
            // Given
            String title = "GitHub";
            String username = "test@example.com";
            String password = "MyGitHubPassword123!";
            PasswordEntry entry = vaultService.addPassword(title, username, password, null, null, null);

            // When
            vaultService.deletePassword(entry.getId());

            // Then
            VaultService.VaultException exception = assertThrows(VaultService.VaultException.class, () -> {
                vaultService.getPassword(entry.getId());
            });
            assertTrue(exception.getMessage().contains("密码条目不存在"));
        }

        @Test
        @DisplayName("应该拒绝删除不存在的条目")
        void shouldRejectDeleteNonExistentEntry() {
            // Given
            int nonExistentId = 99999;

            // When & Then
            VaultService.VaultException exception = assertThrows(VaultService.VaultException.class, () -> {
                vaultService.deletePassword(nonExistentId);
            });
            assertTrue(exception.getMessage().contains("密码条目不存在"));
        }
    }

    @Nested
    @DisplayName("搜索密码测试")
    class SearchPasswordTests {

        @Test
        @DisplayName("应该按标题搜索密码")
        void shouldSearchByTitle() throws VaultService.VaultException {
            // Given
            vaultService.addPassword("GitHub", "user1", "pass1", null, null, null);
            vaultService.addPassword("GitLab", "user2", "pass2", null, null, null);
            vaultService.addPassword("Google", "user3", "pass3", null, null, null);

            // When
            List<PasswordEntry> results = vaultService.searchPasswords("Git");

            // Then
            assertEquals(2, results.size());
        }

        @Test
        @DisplayName("应该按用户名搜索密码")
        void shouldSearchByUsername() throws VaultService.VaultException {
            // Given
            vaultService.addPassword("GitHub", "test@example.com", "pass1", null, null, null);
            vaultService.addPassword("GitLab", "admin@example.com", "pass2", null, null, null);

            // When
            List<PasswordEntry> results = vaultService.searchPasswords("test@");

            // Then
            assertEquals(1, results.size());
            assertEquals("GitHub", results.get(0).getTitle());
        }

        @Test
        @DisplayName("应该返回所有密码当搜索词为空")
        void shouldReturnAllPasswordsWhenQueryIsEmpty() throws VaultService.VaultException {
            // Given
            vaultService.addPassword("GitHub", "user1", "pass1", null, null, null);
            vaultService.addPassword("GitLab", "user2", "pass2", null, null, null);

            // When
            List<PasswordEntry> results = vaultService.searchPasswords("");

            // Then
            assertEquals(2, results.size());
        }

        @Test
        @DisplayName("应该返回空列表当没有匹配")
        void shouldReturnEmptyListWhenNoMatch() throws VaultService.VaultException {
            // Given
            vaultService.addPassword("GitHub", "user1", "pass1", null, null, null);

            // When
            List<PasswordEntry> results = vaultService.searchPasswords("NonExistent");

            // Then
            assertTrue(results.isEmpty());
        }
    }

    @Nested
    @DisplayName("获取所有密码测试")
    class GetAllPasswordsTests {

        @Test
        @DisplayName("应该获取当前用户的所有密码")
        void shouldGetAllPasswordsForCurrentUser() throws VaultService.VaultException {
            // Given
            vaultService.addPassword("GitHub", "user1", "pass1", null, null, null);
            vaultService.addPassword("GitLab", "user2", "pass2", null, null, null);
            vaultService.addPassword("Google", "user3", "pass3", null, null, null);

            // When
            List<PasswordEntry> results = vaultService.getAllPasswords();

            // Then
            assertEquals(3, results.size());
        }

        @Test
        @DisplayName("应该返回空列表当没有密码")
        void shouldReturnEmptyListWhenNoPasswords() throws VaultService.VaultException {
            // When
            List<PasswordEntry> results = vaultService.getAllPasswords();

            // Then
            assertTrue(results.isEmpty());
        }
    }

    @Nested
    @DisplayName("获取密码数量测试")
    class GetPasswordCountTests {

        @Test
        @DisplayName("应该正确统计密码数量")
        void shouldCountPasswordsCorrectly() throws VaultService.VaultException {
            // Given
            vaultService.addPassword("GitHub", "user1", "pass1", null, null, null);
            vaultService.addPassword("GitLab", "user2", "pass2", null, null, null);

            // When
            int count = vaultService.getPasswordCount();

            // Then
            assertEquals(2, count);
        }

        @Test
        @DisplayName("应该返回0当没有密码")
        void shouldReturnZeroWhenNoPasswords() throws VaultService.VaultException {
            // When
            int count = vaultService.getPasswordCount();

            // Then
            assertEquals(0, count);
        }
    }

    @Nested
    @DisplayName("集成测试")
    class IntegrationTests {

        @Test
        @DisplayName("完整的密码管理流程")
        void shouldPerformCompletePasswordManagementFlow() throws VaultService.VaultException {
            // 1. 添加密码
            PasswordEntry entry = vaultService.addPassword(
                    "GitHub", "test@example.com", "MyPassword123!",
                    "https://github.com", "开发账号", null
            );
            assertNotNull(entry);

            // 2. 获取密码
            VaultService.DecryptedPasswordEntry decrypted = vaultService.getPassword(entry.getId());
            assertEquals("MyPassword123!", decrypted.getDecryptedPassword());

            // 3. 更新密码
            PasswordEntry updated = vaultService.updatePassword(
                    entry.getId(), "GitHub Updated", "new@example.com", "NewPassword456!",
                    "https://github.com", "更新的备注", null
            );
            assertEquals("GitHub Updated", updated.getTitle());

            // 4. 验证更新后的密码
            VaultService.DecryptedPasswordEntry updatedDecrypted = vaultService.getPassword(entry.getId());
            assertEquals("NewPassword456!", updatedDecrypted.getDecryptedPassword());

            // 5. 搜索密码
            List<PasswordEntry> searchResults = vaultService.searchPasswords("GitHub");
            assertFalse(searchResults.isEmpty());

            // 6. 删除密码
            vaultService.deletePassword(entry.getId());
            assertThrows(VaultService.VaultException.class, () -> {
                vaultService.getPassword(entry.getId());
            });
        }
    }
}
