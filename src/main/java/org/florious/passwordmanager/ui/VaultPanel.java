package org.florious.passwordmanager.ui;

import org.florious.passwordmanager.model.Category;
import org.florious.passwordmanager.model.PasswordEntry;
import org.florious.passwordmanager.service.CategoryService;
import org.florious.passwordmanager.service.VaultService;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
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
        
        // 添加选择监听器
        categoryTree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) categoryTree.getLastSelectedPathComponent();
            if (node != null) {
                String categoryName = node.getUserObject().toString();
                filterByCategory(categoryName);
            }
        });
        
        JScrollPane treeScrollPane = new JScrollPane(categoryTree);
        panel.add(treeScrollPane, BorderLayout.CENTER);
        
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
            DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(category.getName());
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
    
    private void filterByCategory(String categoryName) {
        if (categoryName.equals("所有分类")) {
            loadPasswordList();
            return;
        }
        
        try {
            // 查找分类ID
            List<Category> categories = categoryService.getAllCategories();
            Integer categoryId = null;
            for (Category category : categories) {
                if (category.getName().equals(categoryName)) {
                    categoryId = category.getId();
                    break;
                }
            }
            
            if (categoryId != null) {
                List<PasswordEntry> entries = vaultService.getPasswordsByCategory(categoryId);
                passwordTable.setPasswordEntries(entries);
                updateStatus("显示分类 \"" + categoryName + "\" 的 " + entries.size() + " 个密码条目");
            } else {
                updateStatus("未找到分类: " + categoryName);
            }
        } catch (Exception e) {
            updateStatus("按分类筛选失败: " + e.getMessage());
        }
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
}