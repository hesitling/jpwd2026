package org.florious.passwordmanager.ui;

import org.florious.passwordmanager.model.Category;
import org.florious.passwordmanager.service.CategoryService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

/**
 * 分类管理对话框
 * 提供分类的增删改查功能
 */
public class CategoryManagementDialog extends JDialog {
    private final CategoryService categoryService;
    private JTable categoryTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton closeButton;

    public CategoryManagementDialog(JFrame parent) {
        super(parent, "分类管理", true);
        this.categoryService = new CategoryService();
        initComponents();
        loadCategories();
        pack();
        setLocationRelativeTo(parent);
    }

    /**
     * 初始化界面组件
     */
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setMinimumSize(new Dimension(500, 400));

        // 创建表格模型
        String[] columns = {"ID", "分类名称", "颜色", "密码条目数量"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 表格不可编辑
            }
        };

        // 创建表格
        categoryTable = new JTable(tableModel);
        categoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryTable.getTableHeader().setReorderingAllowed(false);

        // 隐藏ID列
        categoryTable.getColumnModel().getColumn(0).setMinWidth(0);
        categoryTable.getColumnModel().getColumn(0).setMaxWidth(0);
        categoryTable.getColumnModel().getColumn(0).setWidth(0);

        // 设置列宽
        categoryTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        categoryTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        categoryTable.getColumnModel().getColumn(3).setPreferredWidth(120);

        // 设置颜色列的渲染器
        categoryTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof String color && color != null && !color.isEmpty()) {
                    try {
                        Color bgColor = Color.decode(color);
                        c.setBackground(bgColor);
                        // 根据背景色设置前景色
                        int brightness = (bgColor.getRed() * 299 + bgColor.getGreen() * 587 + bgColor.getBlue() * 114) / 1000;
                        c.setForeground(brightness > 128 ? Color.BLACK : Color.WHITE);
                    } catch (NumberFormatException e) {
                        c.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                        c.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
                    }
                } else {
                    c.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                    c.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
                }
                return c;
            }
        });

        // 添加表格到滚动面板
        JScrollPane scrollPane = new JScrollPane(categoryTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        addButton = new JButton("添加");
        editButton = new JButton("编辑");
        deleteButton = new JButton("删除");
        closeButton = new JButton("关闭");

        // 添加按钮事件
        addButton.addActionListener(e -> addCategory());
        editButton.addActionListener(e -> editCategory());
        deleteButton.addActionListener(e -> deleteCategory());
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // 添加表格选择监听器
        categoryTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonState();
            }
        });

        // 初始按钮状态
        updateButtonState();
    }

    /**
     * 更新按钮状态
     */
    private void updateButtonState() {
        boolean selected = categoryTable.getSelectedRow() >= 0;
        editButton.setEnabled(selected);
        deleteButton.setEnabled(selected);
    }

    /**
     * 加载分类数据
     */
    private void loadCategories() {
        try {
            List<Category> categories = categoryService.getAllCategories();
            tableModel.setRowCount(0); // 清空表格

            for (Category category : categories) {
                int entryCount = categoryService.getPasswordEntryCount(category.getId());
                tableModel.addRow(new Object[]{
                        category.getId(),
                        category.getName(),
                        category.getColor() != null ? category.getColor() : "",
                        entryCount
                });
            }
        } catch (CategoryService.CategoryException e) {
            JOptionPane.showMessageDialog(this,
                    "加载分类失败: " + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 添加分类
     */
    private void addCategory() {
        CategoryEditDialog dialog = new CategoryEditDialog(this, null);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            try {
                categoryService.createCategory(dialog.getCategoryName(), dialog.getCategoryColor());
                loadCategories();
            } catch (CategoryService.CategoryException e) {
                JOptionPane.showMessageDialog(this,
                        "创建分类失败: " + e.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 编辑分类
     */
    private void editCategory() {
        int selectedRow = categoryTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }

        int categoryId = (int) tableModel.getValueAt(selectedRow, 0);
        String currentName = (String) tableModel.getValueAt(selectedRow, 1);
        String currentColor = (String) tableModel.getValueAt(selectedRow, 2);

        CategoryEditDialog dialog = new CategoryEditDialog(this, currentName, currentColor);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            try {
                categoryService.updateCategory(categoryId, dialog.getCategoryName(), dialog.getCategoryColor());
                loadCategories();
            } catch (CategoryService.CategoryException e) {
                JOptionPane.showMessageDialog(this,
                        "更新分类失败: " + e.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 删除分类
     */
    private void deleteCategory() {
        int selectedRow = categoryTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }

        int categoryId = (int) tableModel.getValueAt(selectedRow, 0);
        String categoryName = (String) tableModel.getValueAt(selectedRow, 1);
        int entryCount = (int) tableModel.getValueAt(selectedRow, 3);

        String message;
        if (entryCount > 0) {
            message = String.format("分类 \"%s\" 有 %d 个密码条目。\n删除分类后，这些条目将变为未分类。\n\n确定要删除吗？",
                    categoryName, entryCount);
        } else {
            message = String.format("确定要删除分类 \"%s\" 吗？", categoryName);
        }

        int result = JOptionPane.showConfirmDialog(this,
                message, "确认删除", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            try {
                CategoryService.DeleteResult deleteResult = categoryService.deleteCategory(categoryId);
                if (deleteResult.isSuccess()) {
                    loadCategories();
                    if (deleteResult.getAffectedEntries() > 0) {
                        JOptionPane.showMessageDialog(this,
                                String.format("已删除分类，并将 %d 个密码条目设为未分类。",
                                        deleteResult.getAffectedEntries()),
                                "删除成功", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (CategoryService.CategoryException e) {
                JOptionPane.showMessageDialog(this,
                        "删除分类失败: " + e.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 分类编辑对话框（内部类）
     */
    private static class CategoryEditDialog extends JDialog {
        private JTextField nameField;
        private JButton colorButton;
        private JButton saveButton;
        private JButton cancelButton;
        private boolean saved = false;
        private String categoryColor;

        public CategoryEditDialog(JDialog parent, String currentName) {
            this(parent, currentName, null);
        }

        public CategoryEditDialog(JDialog parent, String currentName, String currentColor) {
            super(parent, currentName == null ? "添加分类" : "编辑分类", true);
            this.categoryColor = currentColor;
            initComponents(currentName, currentColor);
            pack();
            setLocationRelativeTo(parent);
        }

        private void initComponents(String currentName, String currentColor) {
            setLayout(new BorderLayout(10, 10));

            // 创建表单面板
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;

            // 分类名称
            gbc.gridx = 0;
            gbc.gridy = 0;
            formPanel.add(new JLabel("分类名称:"), gbc);

            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            nameField = new JTextField(currentName != null ? currentName : "", 20);
            formPanel.add(nameField, gbc);

            // 颜色选择
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            formPanel.add(new JLabel("颜色:"), gbc);

            gbc.gridx = 1;
            colorButton = new JButton("选择颜色");
            colorButton.setBackground(currentColor != null && !currentColor.isEmpty() ?
                    Color.decode(currentColor) : Color.WHITE);
            colorButton.addActionListener(e -> {
                Color chosen = JColorChooser.showDialog(this, "选择分类颜色", colorButton.getBackground());
                if (chosen != null) {
                    colorButton.setBackground(chosen);
                    categoryColor = String.format("#%02x%02x%02x", chosen.getRed(), chosen.getGreen(), chosen.getBlue());
                }
            });
            formPanel.add(colorButton, gbc);

            add(formPanel, BorderLayout.CENTER);

            // 创建按钮面板
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

            saveButton = new JButton("保存");
            cancelButton = new JButton("取消");

            saveButton.addActionListener(e -> {
                if (validateInput()) {
                    saved = true;
                    dispose();
                }
            });

            cancelButton.addActionListener(e -> dispose());

            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);

            add(buttonPanel, BorderLayout.SOUTH);
        }

        private boolean validateInput() {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "分类名称不能为空", "验证错误", JOptionPane.WARNING_MESSAGE);
                nameField.requestFocus();
                return false;
            }
            if (name.length() > 50) {
                JOptionPane.showMessageDialog(this,
                        "分类名称不能超过50个字符", "验证错误", JOptionPane.WARNING_MESSAGE);
                nameField.requestFocus();
                return false;
            }
            return true;
        }

        public boolean isSaved() {
            return saved;
        }

        public String getCategoryName() {
            return nameField.getText().trim();
        }

        public String getCategoryColor() {
            return categoryColor;
        }
    }
}
