package org.florious.passwordmanager.ui;

import org.florious.passwordmanager.model.PasswordEntry;
import org.florious.passwordmanager.service.AuthService;
import org.florious.passwordmanager.service.Session;
import org.florious.passwordmanager.service.SessionManager;
import org.florious.passwordmanager.service.VaultService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 主窗口框架
 * 包含菜单栏、工具栏、状态栏和主内容区域
 */
public class MainFrame extends JFrame implements SessionManager.SessionListener {
    private final AuthService authService;
    private final SessionManager sessionManager;
    private final VaultService vaultService;
    
    private JMenuBar menuBar;
    private JToolBar toolBar;
    private JPanel statusPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private VaultPanel vaultPanel;
    private LockPanel lockPanel;
    private String lockedUsername;
    
    // 状态栏组件
    private JLabel selectionLabel;
    private JLabel totalLabel;
    
    // 面板名称常量
    private static final String LOGIN_PANEL = "login";
    private static final String REGISTER_PANEL = "register";
    private static final String VAULT_PANEL = "vault";
    private static final String LOCK_PANEL = "lock";
    
    public MainFrame() {
        this.authService = new AuthService();
        this.sessionManager = SessionManager.getInstance();
        this.vaultService = new VaultService();
        
        initComponents();
        setupMenuBar();
        setupToolBar();
        setupStatusBar();
        setupContentPanel();
        setupWindowListener();
        
        // 注册会话监听器
        sessionManager.addSessionListener(this);
        
        // 初始显示登录面板
        showLoginPanel();
        
        // 设置窗口属性
        setTitle("密码管理器");
        setSize(1000, 700);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
    }
    
    private void setupMenuBar() {
        menuBar = new JMenuBar();
        
        // 文件菜单
        JMenu fileMenu = new JMenu("文件");
        fileMenu.setMnemonic('F');
        
        JMenuItem importItem = new JMenuItem("导入...");
        importItem.setMnemonic('I');
        importItem.addActionListener(e -> showImportExportDialog());
        fileMenu.add(importItem);
        
        JMenuItem exportItem = new JMenuItem("导出...");
        exportItem.setMnemonic('E');
        exportItem.addActionListener(e -> showImportExportDialog());
        fileMenu.add(exportItem);
        
        fileMenu.addSeparator();
        
        JMenuItem exitItem = new JMenuItem("退出");
        exitItem.setMnemonic('X');
        exitItem.addActionListener(e -> exitApplication());
        fileMenu.add(exitItem);
        
        menuBar.add(fileMenu);
        
        // 编辑菜单
        JMenu editMenu = new JMenu("编辑");
        editMenu.setMnemonic('E');
        
        JMenuItem addPasswordItem = new JMenuItem("添加密码");
        addPasswordItem.setMnemonic('A');
        addPasswordItem.addActionListener(e -> showAddPasswordDialog());
        editMenu.add(addPasswordItem);
        
        JMenuItem editPasswordItem = new JMenuItem("编辑密码");
        editPasswordItem.setMnemonic('E');
        editPasswordItem.addActionListener(e -> showEditPasswordDialog());
        editMenu.add(editPasswordItem);
        
        JMenuItem deletePasswordItem = new JMenuItem("删除密码");
        deletePasswordItem.setMnemonic('D');
        deletePasswordItem.addActionListener(e -> deleteSelectedPassword());
        editMenu.add(deletePasswordItem);
        
        editMenu.addSeparator();
        
        JMenuItem settingsItem = new JMenuItem("设置...");
        settingsItem.setMnemonic('S');
        settingsItem.addActionListener(e -> showSettingsDialog());
        editMenu.add(settingsItem);
        
        menuBar.add(editMenu);
        
        // 视图菜单
        JMenu viewMenu = new JMenu("视图");
        viewMenu.setMnemonic('V');
        
        JMenuItem refreshItem = new JMenuItem("刷新");
        refreshItem.setMnemonic('R');
        refreshItem.addActionListener(e -> refreshData());
        viewMenu.add(refreshItem);
        
        JMenuItem lockItem = new JMenuItem("锁定");
        lockItem.setMnemonic('L');
        lockItem.addActionListener(e -> lockApplication());
        viewMenu.add(lockItem);
        
        menuBar.add(viewMenu);
        
        // 帮助菜单
        JMenu helpMenu = new JMenu("帮助");
        helpMenu.setMnemonic('H');
        
        JMenuItem aboutItem = new JMenuItem("关于");
        aboutItem.setMnemonic('A');
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);
        
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void setupToolBar() {
        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        
        // 添加按钮
        JButton addButton = createToolBarButton("添加", "添加新密码");
        addButton.addActionListener(e -> showAddPasswordDialog());
        toolBar.add(addButton);
        
        // 编辑按钮
        JButton editButton = createToolBarButton("编辑", "编辑选中的密码");
        editButton.addActionListener(e -> showEditPasswordDialog());
        toolBar.add(editButton);
        
        // 删除按钮
        JButton deleteButton = createToolBarButton("删除", "删除选中的密码");
        deleteButton.addActionListener(e -> deleteSelectedPassword());
        toolBar.add(deleteButton);
        
        toolBar.addSeparator();
        
        // 搜索按钮
        JButton searchButton = createToolBarButton("搜索", "搜索密码");
        searchButton.addActionListener(e -> showSearchDialog());
        toolBar.add(searchButton);
        
        toolBar.addSeparator();
        
        // 导入按钮
        JButton importButton = createToolBarButton("导入", "导入CSV文件");
        importButton.addActionListener(e -> showImportExportDialog());
        toolBar.add(importButton);
        
        // 导出按钮
        JButton exportButton = createToolBarButton("导出", "导出CSV文件");
        exportButton.addActionListener(e -> showImportExportDialog());
        toolBar.add(exportButton);
        
        toolBar.addSeparator();
        
        // 生成密码按钮
        JButton generateButton = createToolBarButton("生成密码", "打开密码生成器");
        generateButton.addActionListener(e -> showPasswordGenerator());
        toolBar.add(generateButton);
        
        add(toolBar, BorderLayout.NORTH);
    }
    
    private JButton createToolBarButton(String text, String toolTip) {
        JButton button = new JButton(text);
        button.setToolTipText(toolTip);
        button.setFocusPainted(false);
        return button;
    }
    
    private SessionStatusBar sessionStatusBar;
    
    private void setupStatusBar() {
        statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        // 左侧：选择信息和总计信息
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        selectionLabel = new JLabel("选中: 0");
        totalLabel = new JLabel("总计: 0");
        leftPanel.add(selectionLabel);
        leftPanel.add(totalLabel);
        
        // 右侧：会话状态栏
        sessionStatusBar = new SessionStatusBar(sessionManager);
        
        statusPanel.add(leftPanel, BorderLayout.WEST);
        statusPanel.add(sessionStatusBar, BorderLayout.EAST);
        
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private void setupContentPanel() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        
        // 创建各个面板
        LoginPanel loginPanel = new LoginPanel(this);
        RegisterPanel registerPanel = new RegisterPanel(this);
        vaultPanel = new VaultPanel(this);
        lockPanel = new LockPanel();
        
        // 设置锁定面板的解锁回调
        lockPanel.setUnlockCallback(new LockPanel.UnlockCallback() {
            @Override
            public void onUnlockAttempt(String password) {
                handleUnlock(password);
            }
            
            @Override
            public void onUnlockFailure(String message) {
                lockPanel.showError(message);
            }
        });
        
        contentPanel.add(loginPanel, LOGIN_PANEL);
        contentPanel.add(registerPanel, REGISTER_PANEL);
        contentPanel.add(vaultPanel, VAULT_PANEL);
        contentPanel.add(lockPanel, LOCK_PANEL);
        
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private void setupWindowListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });
    }
    
    // 面板切换方法
    public void showLoginPanel() {
        cardLayout.show(contentPanel, LOGIN_PANEL);
        updateSessionStatus();
    }
    
    public void showLockPanel() {
        cardLayout.show(contentPanel, LOCK_PANEL);
        lockPanel.setWelcomeMessage(lockedUsername);
        lockPanel.clearPassword();
        lockPanel.requestPasswordFocus();
    }
    
    public void showRegisterPanel() {
        cardLayout.show(contentPanel, REGISTER_PANEL);
    }
    
    public void showVaultPanel() {
        cardLayout.show(contentPanel, VAULT_PANEL);
        refreshData();
    }
    
    // 对话框显示方法
    private void showImportExportDialog() {
        ImportExportDialog dialog = new ImportExportDialog(this);
        dialog.setVisible(true);
    }
    
    private void showAddPasswordDialog() {
        PasswordDialog dialog = new PasswordDialog(this, PasswordDialog.DialogMode.ADD);
        dialog.setVisible(true);
        refreshData();
    }
    
    private void showEditPasswordDialog() {
        PasswordEntry selectedEntry = vaultPanel.getPasswordTable().getSelectedPasswordEntry();
        if (selectedEntry == null) {
            JOptionPane.showMessageDialog(this, "请先选择一个密码条目", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        PasswordDialog dialog = new PasswordDialog(this, PasswordDialog.DialogMode.EDIT, selectedEntry);
        dialog.setVisible(true);
        refreshData();
    }
    
    private void deleteSelectedPassword() {
        PasswordEntry selectedEntry = vaultPanel.getPasswordTable().getSelectedPasswordEntry();
        if (selectedEntry == null) {
            JOptionPane.showMessageDialog(this, "请先选择一个密码条目", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int choice = JOptionPane.showConfirmDialog(this,
                "确定要删除密码条目 \"" + selectedEntry.getTitle() + "\" 吗？",
                "确认删除",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        
        if (choice == JOptionPane.YES_OPTION) {
            try {
                vaultService.deletePassword(selectedEntry.getId());
                refreshData();
                JOptionPane.showMessageDialog(this, "密码条目已删除", "成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (VaultService.VaultException e) {
                JOptionPane.showMessageDialog(this, "删除失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void showSearchDialog() {
        vaultPanel.focusSearchField();
    }
    
    private void showSettingsDialog() {
        SettingsDialog dialog = new SettingsDialog(this);
        dialog.setVisible(true);
    }
    
    private void showPasswordGenerator() {
        PasswordGeneratorDialog dialog = new PasswordGeneratorDialog(this);
        dialog.setVisible(true);
    }
    
    private void showAboutDialog() {
        String message = "密码管理器 v1.0\n\n" +
                "一个安全的密码管理应用程序\n" +
                "使用Java Swing开发";
        JOptionPane.showMessageDialog(this, message, "关于", JOptionPane.INFORMATION_MESSAGE);
    }
    
    // 业务方法
    public AuthService getAuthService() {
        return authService;
    }
    
    public SessionManager getSessionManager() {
        return sessionManager;
    }
    
    public VaultService getVaultService() {
        return vaultService;
    }
    
    public VaultPanel getVaultPanel() {
        return vaultPanel;
    }
    
    public void refreshData() {
        if (vaultPanel != null) {
            vaultPanel.refresh();
        }
    }
    
    public void lockApplication() {
        // 保存当前用户名
        Session session = sessionManager.getCurrentSession();
        if (session != null) {
            lockedUsername = session.getUser().getUsername();
        }
        
        // 销毁会话
        sessionManager.destroySession();
        
        // 显示锁定面板
        showLockPanel();
    }
    
    private void handleUnlock(String password) {
        if (lockedUsername == null || lockedUsername.isEmpty()) {
            lockPanel.showError("无法获取用户名，请重新登录");
            showLoginPanel();
            return;
        }
        
        try {
            authService.login(lockedUsername, password);
            // 解锁成功
            showVaultPanel();
        } catch (AuthService.AuthException e) {
            lockPanel.showError("密码错误");
        }
    }
    
    public void exitApplication() {
        int choice = JOptionPane.showConfirmDialog(this, 
                "确定要退出应用程序吗？", 
                "确认退出", 
                JOptionPane.YES_NO_OPTION);
        
        if (choice == JOptionPane.YES_OPTION) {
            sessionManager.destroySession();
            System.exit(0);
        }
    }
    
    public void updateSelectionStatus(int selectedCount) {
        selectionLabel.setText("选中: " + selectedCount);
    }
    
    public void updateTotalStatus(int totalCount) {
        totalLabel.setText("总计: " + totalCount);
    }
    
    public void updateSessionStatus() {
        // SessionStatusBar 会自动更新，无需手动更新
    }
    
    // SessionListener 接口实现
    @Override
    public void onSessionCreated(Session session) {
        // 会话创建时的处理
    }
    
    @Override
    public void onSessionDestroyed() {
        // 会话销毁时的处理
    }
    
    @Override
    public void onSessionTimeout() {
        SwingUtilities.invokeLater(() -> {
            showLockPanel();
        });
    }
    
    @Override
    public void onSessionLocked() {
        SwingUtilities.invokeLater(() -> {
            showLockPanel();
        });
    }
    
    /**
     * 启动应用程序
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // 设置系统外观
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // 使用默认外观
            }
            
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}