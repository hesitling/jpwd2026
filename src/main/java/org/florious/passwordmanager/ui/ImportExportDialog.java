package org.florious.passwordmanager.ui;

import org.florious.passwordmanager.model.Category;
import org.florious.passwordmanager.service.CategoryService;
import org.florious.passwordmanager.util.CsvHandler;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

/**
 * 导入导出对话框
 * 支持CSV格式的密码条目导入导出
 */
public class ImportExportDialog extends JDialog {
    private final CategoryService categoryService;
    private final CsvHandler csvHandler;
    
    private JTabbedPane tabbedPane;
    private JPanel importPanel;
    private JPanel exportPanel;
    
    // 导入组件
    private JTextField importFileField;
    private JButton importBrowseButton;
    private JComboBox<CsvHandler.DuplicateHandling> duplicateHandlingCombo;
    private JComboBox<CategoryItem> importCategoryCombo;
    private JCheckBox importAllColumnsCheckBox;
    
    // 导出组件
    private JTextField exportFileField;
    private JButton exportBrowseButton;
    private JRadioButton exportAllRadio;
    private JRadioButton exportSelectedRadio;
    private JComboBox<CategoryItem> exportCategoryCombo;
    
    // 按钮
    private JButton importButton;
    private JButton exportButton;
    private JButton closeButton;
    
    public ImportExportDialog(Frame owner) {
        super(owner, "导入导出", true);
        this.categoryService = new CategoryService();
        this.csvHandler = new CsvHandler();
        initComponents();
        loadCategories();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setSize(500, 400);
        setLocationRelativeTo(getOwner());
        
        // 创建选项卡面板
        tabbedPane = new JTabbedPane();
        importPanel = createImportPanel();
        exportPanel = createExportPanel();
        tabbedPane.addTab("导入", importPanel);
        tabbedPane.addTab("导出", exportPanel);
        
        // 创建按钮面板
        JPanel buttonPanel = createButtonPanel();
        
        // 添加到主面板
        add(tabbedPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createImportPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 文件选择
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("CSV文件:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        importFileField = new JTextField(20);
        panel.add(importFileField, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0;
        importBrowseButton = new JButton("浏览...");
        importBrowseButton.addActionListener(e -> browseImportFile());
        panel.add(importBrowseButton, gbc);
        
        // 重复处理选项
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("重复处理:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        duplicateHandlingCombo = new JComboBox<>(CsvHandler.DuplicateHandling.values());
        duplicateHandlingCombo.setSelectedItem(CsvHandler.DuplicateHandling.SKIP);
        panel.add(duplicateHandlingCombo, gbc);
        
        // 目标分类
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        panel.add(new JLabel("目标分类:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        importCategoryCombo = new JComboBox<>();
        panel.add(importCategoryCombo, gbc);
        
        // 导入选项
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        importAllColumnsCheckBox = new JCheckBox("导入所有列（包括空列）", true);
        panel.add(importAllColumnsCheckBox, gbc);
        
        // 导入按钮
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        importButton = new JButton("开始导入");
        importButton.addActionListener(e -> performImport());
        panel.add(importButton, gbc);
        
        return panel;
    }
    
    private JPanel createExportPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 文件选择
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("保存位置:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        exportFileField = new JTextField(20);
        panel.add(exportFileField, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0;
        exportBrowseButton = new JButton("浏览...");
        exportBrowseButton.addActionListener(e -> browseExportFile());
        panel.add(exportBrowseButton, gbc);
        
        // 导出范围
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        panel.add(new JLabel("导出范围:"), gbc);
        
        // 单选按钮面板
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        exportAllRadio = new JRadioButton("所有条目", true);
        exportSelectedRadio = new JRadioButton("选定条目", false);
        ButtonGroup group = new ButtonGroup();
        group.add(exportAllRadio);
        group.add(exportSelectedRadio);
        radioPanel.add(exportAllRadio);
        radioPanel.add(exportSelectedRadio);
        
        gbc.gridy = 2;
        panel.add(radioPanel, gbc);
        
        // 分类过滤
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        panel.add(new JLabel("分类过滤:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        exportCategoryCombo = new JComboBox<>();
        panel.add(exportCategoryCombo, gbc);
        
        // 导出按钮
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        exportButton = new JButton("开始导出");
        exportButton.addActionListener(e -> performExport());
        panel.add(exportButton, gbc);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> dispose());
        panel.add(closeButton);
        return panel;
    }
    
    private void loadCategories() {
        try {
            List<Category> categories = categoryService.getAllCategories();
            importCategoryCombo.removeAllItems();
            exportCategoryCombo.removeAllItems();
            
            // 添加"所有分类"选项
            importCategoryCombo.addItem(new CategoryItem(null, "所有分类"));
            exportCategoryCombo.addItem(new CategoryItem(null, "所有分类"));
            
            for (Category category : categories) {
                CategoryItem item = new CategoryItem(category.getId(), category.getName());
                importCategoryCombo.addItem(item);
                exportCategoryCombo.addItem(item);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载分类失败: " + e.getMessage(), 
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void browseImportFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择CSV文件");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV文件 (*.csv)", "csv"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            importFileField.setText(file.getAbsolutePath());
        }
    }
    
    private void browseExportFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("保存CSV文件");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV文件 (*.csv)", "csv"));
        fileChooser.setSelectedFile(new File("passwords.csv"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String path = file.getAbsolutePath();
            if (!path.toLowerCase().endsWith(".csv")) {
                path += ".csv";
            }
            exportFileField.setText(path);
        }
    }
    
    private void performImport() {
        String filePath = importFileField.getText().trim();
        if (filePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请选择CSV文件", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        CsvHandler.DuplicateHandling handling = (CsvHandler.DuplicateHandling) duplicateHandlingCombo.getSelectedItem();
        
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            importButton.setEnabled(false);
            
            CsvHandler.ImportResult result = csvHandler.importFromCsv(filePath, handling);
            
            String message = String.format("导入完成！\n新创建: %d\n覆盖更新: %d\n重命名: %d\n跳过: %d",
                    result.getCreatedCount(), result.getOverwrittenCount(), 
                    result.getRenamedCount(), result.getSkippedCount());
            
            JOptionPane.showMessageDialog(this, message, "导入成功", JOptionPane.INFORMATION_MESSAGE);
        } catch (CsvHandler.CsvException e) {
            JOptionPane.showMessageDialog(this, "导入失败: " + e.getMessage(), 
                    "错误", JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(Cursor.getDefaultCursor());
            importButton.setEnabled(true);
        }
    }
    
    private void performExport() {
        String filePath = exportFileField.getText().trim();
        if (filePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请选择保存位置", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            exportButton.setEnabled(false);
            
            int count = csvHandler.exportToCsv(filePath);
            
            String message = String.format("导出完成！\n共导出 %d 个密码条目", count);
            JOptionPane.showMessageDialog(this, message, "导出成功", JOptionPane.INFORMATION_MESSAGE);
        } catch (CsvHandler.CsvException e) {
            JOptionPane.showMessageDialog(this, "导出失败: " + e.getMessage(), 
                    "错误", JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(Cursor.getDefaultCursor());
            exportButton.setEnabled(true);
        }
    }
    
    /**
     * 分类选项内部类
     */
    private static class CategoryItem {
        private final Integer id;
        private final String name;
        
        public CategoryItem(Integer id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public Integer getId() {
            return id;
        }
        
        public String getName() {
            return name;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
}