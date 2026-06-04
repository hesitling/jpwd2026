package org.florious.passwordmanager.ui;

import org.florious.passwordmanager.model.Category;
import org.florious.passwordmanager.model.PasswordEntry;
import org.florious.passwordmanager.service.CategoryService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 密码表格组件
 * 显示密码条目列表
 */
public class PasswordTable extends JTable {
    private final MainFrame mainFrame;
    private final PasswordTableModel tableModel;
    private final CategoryService categoryService;
    private List<PasswordEntry> passwordEntries;
    private Map<Integer, String> categoryNameCache;
    
    public PasswordTable(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.passwordEntries = new ArrayList<>();
        this.tableModel = new PasswordTableModel();
        this.categoryService = new CategoryService();
        this.categoryNameCache = new HashMap<>();
        
        setModel(tableModel);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setRowHeight(25);
        setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        // 设置列宽
        setColumnWidths();
        
        // 添加鼠标监听器
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showPasswordDetail();
                }
            }
        });
        
        // 添加选择监听器
        getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateSelectionStatus();
            }
        });
    }
    
    private void setColumnWidths() {
        // 标题列
        getColumnModel().getColumn(0).setPreferredWidth(150);
        // 用户名列
        getColumnModel().getColumn(1).setPreferredWidth(120);
        // URL列
        getColumnModel().getColumn(2).setPreferredWidth(200);
        // 分类列
        getColumnModel().getColumn(3).setPreferredWidth(100);
        // 更新时间列
        getColumnModel().getColumn(4).setPreferredWidth(150);
    }
    
    public void setPasswordEntries(List<PasswordEntry> entries) {
        this.passwordEntries = entries;
        refreshCategoryNameCache();
        tableModel.fireTableDataChanged();
        updateSelectionStatus();
    }
    
    private void refreshCategoryNameCache() {
        categoryNameCache.clear();
        try {
            List<Category> categories = categoryService.getAllCategories();
            for (Category category : categories) {
                categoryNameCache.put(category.getId(), category.getName());
            }
        } catch (CategoryService.CategoryException e) {
            // 加载失败时缓存为空，显示"未分类"
        }
    }
    
    public PasswordEntry getSelectedPasswordEntry() {
        int selectedRow = getSelectedRow();
        if (selectedRow >= 0 && selectedRow < passwordEntries.size()) {
            return passwordEntries.get(selectedRow);
        }
        return null;
    }
    
    public List<PasswordEntry> getSelectedPasswordEntries() {
        List<PasswordEntry> selectedEntries = new ArrayList<>();
        int[] selectedRows = getSelectedRows();
        for (int row : selectedRows) {
            if (row >= 0 && row < passwordEntries.size()) {
                selectedEntries.add(passwordEntries.get(row));
            }
        }
        return selectedEntries;
    }
    
    private void showPasswordDetail() {
        PasswordEntry entry = getSelectedPasswordEntry();
        if (entry != null) {
            PasswordDetailDialog dialog = new PasswordDetailDialog(mainFrame, entry.getId());
            dialog.setVisible(true);
        }
    }
    
    private void updateSelectionStatus() {
        int selectedRow = getSelectedRow();
        mainFrame.updateSelectionStatus(selectedRow >= 0 ? 1 : 0);
    }
    
    /**
     * 表格模型内部类
     */
    private class PasswordTableModel extends AbstractTableModel {
        private final String[] columnNames = {"标题", "用户名", "URL", "分类", "更新时间"};
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        @Override
        public int getRowCount() {
            return passwordEntries.size();
        }
        
        @Override
        public int getColumnCount() {
            return columnNames.length;
        }
        
        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex < 0 || rowIndex >= passwordEntries.size()) {
                return null;
            }
            
            PasswordEntry entry = passwordEntries.get(rowIndex);
            switch (columnIndex) {
                case 0: // 标题
                    return entry.getTitle();
                case 1: // 用户名
                    return entry.getUsername();
                case 2: // URL
                    return entry.getUrl();
                case 3: // 分类
                    Integer categoryId = entry.getCategoryId();
                    if (categoryId == null) {
                        return "未分类";
                    }
                    return categoryNameCache.getOrDefault(categoryId, "未分类");
                case 4: // 更新时间
                    return entry.getUpdatedAt() != null ? entry.getUpdatedAt().format(formatter) : "";
                default:
                    return null;
            }
        }
        
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0: // 标题
                case 1: // 用户名
                case 2: // URL
                case 3: // 分类
                    return String.class;
                case 4: // 更新时间
                    return String.class;
                default:
                    return Object.class;
            }
        }
        
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false; // 所有单元格不可编辑
        }
    }
}