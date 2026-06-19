package org.florious.passwordmanager.ui;

import org.florious.passwordmanager.model.Category;
import org.florious.passwordmanager.model.PasswordEntry;
import org.florious.passwordmanager.service.CategoryService;
import org.florious.passwordmanager.service.VaultService;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.List;

/**
 * 密码库面板
 * 显示密码列表、搜索框和分类树
 */
public class VaultPanel extends JPanel {
    private final MainFrame mainFrame;
    private final VaultService vaultService;
    private final CategoryService categoryService;
    
    private JTextField searchField;
    private JButton searchButton;
    private JTree categoryTree;
    private PasswordTable passwordTable;
    private JLabel statusLabel;
    private JButton deleteButton;
    
    public VaultPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.vaultService = mainFrame.getVaultService();
        this.categoryService = new CategoryService();
        
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 顶部搜索面板
        JPanel searchPanel = createSearchPanel();
        add(searchPanel, BorderLayout.NORTH);
        
        // 左侧分类树
        JPanel categoryPanel = createCategoryPanel();
        add(categoryPanel, BorderLayout.WEST);
        
        // 中间密码表格
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);
        
        // 底部状态栏
        JPanel statusPanel = createStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        searchField = new JTextField();
        searchField.setToolTipText("输入关键词搜索密码");
        searchField.addActionListener(e -> performSearch());
        
        searchButton = new JButton("搜索");
        searchButton.addActionListener(e -> performSearch());
        
        panel.add(searchField, BorderLayout.CENTER);
        panel.add(searchButton, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createCategoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(200, 0));
        panel.setBorder(BorderFactory.createTitledBorder("分类"));
        
        // 创建分类树
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("所有分类");
        categoryTree = new JTree(root);
        categoryTree.setRootVisible(false);
        categoryTree.setShowsRootHandles(true);
        
        // 设置自定义渲染器和编辑器
        DefaultTreeCellRenderer renderer = new CategoryTreeCellRenderer();
        categoryTree.setCellRenderer(renderer);
        categoryTree.setCellEditor(new CategoryTreeCellEditor(categoryTree, renderer,
                categoryService, this::updateDeleteButtonState));
        categoryTree.setEditable(true);
        
        // 添加选择监听器
        categoryTree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) categoryTree.getLastSelectedPathComponent();
            if (node != null) {
                Object userObject = node.getUserObject();
                if (userObject instanceof Category cat) {
                    filterByCategory(cat.getId());
                } else {
                    filterByCategory(null);
                }
            }
            updateDeleteButtonState();
        });
        
        JScrollPane treeScrollPane = new JScrollPane(categoryTree);
        panel.add(treeScrollPane, BorderLayout.CENTER);
        
        // 底部按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 2));
        JButton addButton = new JButton("添加");
        deleteButton = new JButton("删除");
        deleteButton.setEnabled(false);
        
        addButton.addActionListener(e -> addCategory());
        deleteButton.addActionListener(e -> deleteCategory());
        
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        passwordTable = new PasswordTable(mainFrame);
        JScrollPane tableScrollPane = new JScrollPane(passwordTable);
        
        panel.add(tableScrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("就绪");
        panel.add(statusLabel);
        
        return panel;
    }
    
    public void loadData() {
        try {
            // 加载分类树
            loadCategoryTree();
            
            // 加载密码列表
            loadPasswordList();
            
            updateStatus("数据加载完成");
        } catch (Exception e) {
            updateStatus("加载数据失败: " + e.getMessage());
        }
    }
    
    private void loadCategoryTree() throws Exception {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("所有分类");
        DefaultTreeModel model = new DefaultTreeModel(root);
        
        List<Category> categories = categoryService.getAllCategories();
        for (Category category : categories) {
            DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(category);
            root.add(categoryNode);
        }
        
        categoryTree.setModel(model);
        
        // 展开所有节点
        for (int i = 0; i < categoryTree.getRowCount(); i++) {
            categoryTree.expandRow(i);
        }
    }
    
    private void loadPasswordList() {
        try {
            List<PasswordEntry> entries = vaultService.getAllPasswords();
            passwordTable.setPasswordEntries(entries);
            updateStatus("显示 " + entries.size() + " 个密码条目");
        } catch (Exception e) {
            updateStatus("加载密码列表失败: " + e.getMessage());
        }
    }
    
    private void performSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            loadPasswordList();
            return;
        }
        
        try {
            List<PasswordEntry> results = vaultService.searchPasswords(query);
            passwordTable.setPasswordEntries(results);
            updateStatus("找到 " + results.size() + " 个匹配的密码条目");
        } catch (Exception e) {
            updateStatus("搜索失败: " + e.getMessage());
        }
    }
    
    private void filterByCategory(Integer categoryId) {
        if (categoryId == null) {
            loadPasswordList();
            return;
        }
        
        try {
            List<PasswordEntry> entries = vaultService.getPasswordsByCategory(categoryId);
            passwordTable.setPasswordEntries(entries);
            updateStatus("显示 " + entries.size() + " 个密码条目");
        } catch (Exception e) {
            updateStatus("按分类筛选失败: " + e.getMessage());
        }
    }
    
    private void updateDeleteButtonState() {
        if (deleteButton == null) {
            return;
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) categoryTree.getLastSelectedPathComponent();
        boolean isCategoryNode = node != null && node.getUserObject() instanceof Category;
        deleteButton.setEnabled(isCategoryNode);
    }
    
    private void updateStatus(String message) {
        statusLabel.setText(message);
        mainFrame.updateTotalStatus(passwordTable.getRowCount());
    }
    
    public void refresh() {
        loadData();
    }
    
    public PasswordTable getPasswordTable() {
        return passwordTable;
    }
    
    public void focusSearchField() {
        searchField.requestFocusInWindow();
    }
    
    /**
     * 添加分类
     */
    private void addCategory() {
        String name = JOptionPane.showInputDialog(this, "请输入分类名称：", "添加分类",
                JOptionPane.PLAIN_MESSAGE);
        if (name == null) {
            return; // 用户取消
        }
        name = name.trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "分类名称不能为空", "错误", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            categoryService.createCategory(name, null);
            loadCategoryTree();
            updateStatus("分类 \"" + name + "\" 创建成功");
        } catch (CategoryService.CategoryException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "创建失败", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "创建分类失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 删除分类
     */
    private void deleteCategory() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) categoryTree.getLastSelectedPathComponent();
        if (node == null || !(node.getUserObject() instanceof Category category)) {
            return;
        }
        
        try {
            int entryCount = categoryService.getPasswordEntryCount(category.getId());
            String message;
            if (entryCount > 0) {
                message = String.format("分类 '%s' 下有 %d 个密码条目，删除后将变为未分类。确定删除？",
                        category.getName(), entryCount);
            } else {
                message = String.format("确定删除分类 '%s'？", category.getName());
            }
            
            int result = JOptionPane.showConfirmDialog(this, message, "确认删除",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (result != JOptionPane.YES_OPTION) {
                return;
            }
            
            categoryService.deleteCategory(category.getId());
            loadCategoryTree();
            updateStatus("分类 \"" + category.getName() + "\" 已删除");
        } catch (CategoryService.CategoryException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "删除失败", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "删除分类失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
}