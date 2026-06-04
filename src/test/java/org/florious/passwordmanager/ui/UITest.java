package org.florious.passwordmanager.ui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UI组件测试
 * 测试UI组件的创建和基本属性
 */
@DisplayName("UI组件测试")
class UITest {

    @Nested
    @DisplayName("MainFrame测试")
    class MainFrameTests {

        @Test
        @DisplayName("应该能够创建MainFrame实例")
        void shouldCreateMainFrameInstance() {
            // Given & When
            MainFrame frame = new MainFrame();

            // Then
            assertNotNull(frame);
            assertEquals("密码管理器", frame.getTitle());
            assertEquals(JFrame.DO_NOTHING_ON_CLOSE, frame.getDefaultCloseOperation());
            
            // 清理
            frame.dispose();
        }

        @Test
        @DisplayName("MainFrame应该包含菜单栏")
        void shouldHaveMenuBar() {
            // Given
            MainFrame frame = new MainFrame();

            // When
            JMenuBar menuBar = frame.getJMenuBar();

            // Then
            assertNotNull(menuBar);
            assertTrue(menuBar.getMenuCount() > 0);
            
            // 清理
            frame.dispose();
        }

        @Test
        @DisplayName("MainFrame应该包含工具栏")
        void shouldHaveToolBar() {
            // Given
            MainFrame frame = new MainFrame();

            // When & Then
            // 检查是否包含工具栏组件
            boolean hasToolBar = false;
            for (java.awt.Component component : frame.getContentPane().getComponents()) {
                if (component instanceof JToolBar) {
                    hasToolBar = true;
                    break;
                }
            }
            assertTrue(hasToolBar);
            
            // 清理
            frame.dispose();
        }
    }

    @Nested
    @DisplayName("LoginPanel测试")
    class LoginPanelTests {

        @Test
        @DisplayName("应该能够创建LoginPanel实例")
        void shouldCreateLoginPanelInstance() {
            // Given
            MainFrame frame = new MainFrame();

            // When
            LoginPanel panel = new LoginPanel(frame);

            // Then
            assertNotNull(panel);
            
            // 清理
            frame.dispose();
        }

        @Test
        @DisplayName("LoginPanel应该包含用户名和密码字段")
        void shouldHaveUsernameAndPasswordFields() {
            // Given
            MainFrame frame = new MainFrame();
            LoginPanel panel = new LoginPanel(frame);

            // When & Then
            boolean hasUsernameField = false;
            boolean hasPasswordField = false;
            
            for (java.awt.Component component : panel.getComponents()) {
                if (component instanceof JTextField) {
                    hasUsernameField = true;
                }
                if (component instanceof JPasswordField) {
                    hasPasswordField = true;
                }
            }
            
            assertTrue(hasUsernameField);
            assertTrue(hasPasswordField);
            
            // 清理
            frame.dispose();
        }
    }

    @Nested
    @DisplayName("RegisterPanel测试")
    class RegisterPanelTests {

        @Test
        @DisplayName("应该能够创建RegisterPanel实例")
        void shouldCreateRegisterPanelInstance() {
            // Given
            MainFrame frame = new MainFrame();

            // When
            RegisterPanel panel = new RegisterPanel(frame);

            // Then
            assertNotNull(panel);
            
            // 清理
            frame.dispose();
        }
    }

    @Nested
    @DisplayName("VaultPanel测试")
    class VaultPanelTests {

        @Test
        @DisplayName("应该能够创建VaultPanel实例")
        void shouldCreateVaultPanelInstance() {
            // Given
            MainFrame frame = new MainFrame();

            // When
            VaultPanel panel = new VaultPanel(frame);

            // Then
            assertNotNull(panel);
            
            // 清理
            frame.dispose();
        }
    }

    @Nested
    @DisplayName("PasswordTable测试")
    class PasswordTableTests {

        @Test
        @DisplayName("应该能够创建PasswordTable实例")
        void shouldCreatePasswordTableInstance() {
            // Given
            MainFrame frame = new MainFrame();

            // When
            PasswordTable table = new PasswordTable(frame);

            // Then
            assertNotNull(table);
            assertEquals(5, table.getColumnCount()); // 5列：标题、用户名、URL、分类、更新时间
            
            // 清理
            frame.dispose();
        }
    }

    @Nested
    @DisplayName("PasswordDialog测试")
    class PasswordDialogTests {

        @Test
        @DisplayName("应该能够创建添加模式的PasswordDialog")
        void shouldCreateAddModePasswordDialog() {
            // Given
            MainFrame frame = new MainFrame();

            // When
            PasswordDialog dialog = new PasswordDialog(frame, PasswordDialog.DialogMode.ADD);

            // Then
            assertNotNull(dialog);
            assertTrue(dialog.getTitle().contains("添加"));
            
            // 清理
            dialog.dispose();
            frame.dispose();
        }

        @Test
        @DisplayName("应该能够创建编辑模式的PasswordDialog")
        void shouldCreateEditModePasswordDialog() {
            // Given
            MainFrame frame = new MainFrame();

            // When
            PasswordDialog dialog = new PasswordDialog(frame, PasswordDialog.DialogMode.EDIT);

            // Then
            assertNotNull(dialog);
            assertTrue(dialog.getTitle().contains("编辑"));
            
            // 清理
            dialog.dispose();
            frame.dispose();
        }
    }

    @Nested
    @DisplayName("SettingsDialog测试")
    class SettingsDialogTests {

        @Test
        @DisplayName("应该能够创建SettingsDialog实例")
        void shouldCreateSettingsDialogInstance() {
            // Given
            MainFrame frame = new MainFrame();

            // When
            SettingsDialog dialog = new SettingsDialog(frame);

            // Then
            assertNotNull(dialog);
            assertEquals("设置", dialog.getTitle());
            
            // 清理
            dialog.dispose();
            frame.dispose();
        }
    }

    @Nested
    @DisplayName("ImportExportDialog测试")
    class ImportExportDialogTests {

        @Test
        @DisplayName("应该能够创建ImportExportDialog实例")
        void shouldCreateImportExportDialogInstance() {
            // Given
            MainFrame frame = new MainFrame();

            // When
            ImportExportDialog dialog = new ImportExportDialog(frame);

            // Then
            assertNotNull(dialog);
            assertEquals("导入导出", dialog.getTitle());
            
            // 清理
            dialog.dispose();
            frame.dispose();
        }
    }

    @Nested
    @DisplayName("PasswordGeneratorDialog测试")
    class PasswordGeneratorDialogTests {

        @Test
        @DisplayName("应该能够创建PasswordGeneratorDialog实例")
        void shouldCreatePasswordGeneratorDialogInstance() {
            // Given
            JFrame frame = new JFrame();

            // When
            PasswordGeneratorDialog dialog = new PasswordGeneratorDialog(frame);

            // Then
            assertNotNull(dialog);
            assertEquals("密码生成器", dialog.getTitle());
            
            // 清理
            dialog.dispose();
            frame.dispose();
        }
    }
}