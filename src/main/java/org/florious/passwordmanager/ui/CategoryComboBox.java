package org.florious.passwordmanager.ui;

import org.florious.passwordmanager.model.Category;
import org.florious.passwordmanager.service.CategoryService;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 分类选择下拉框组件
 * 用于在密码条目编辑时选择分类
 */
public class CategoryComboBox extends JComboBox<CategoryComboBox.CategoryItem> {
    private final CategoryService categoryService;
    private List<CategoryItem> categoryItems;

    public CategoryComboBox() {
        super();
        this.categoryService = new CategoryService();
        this.categoryItems = new ArrayList<>();
        initComponents();
        loadCategories();
    }

    /**
     * 初始化组件
     */
    private void initComponents() {
        setRenderer(new CategoryListCellRenderer());
        setPreferredSize(new Dimension(200, 30));
    }

    /**
     * 加载分类数据
     */
    public void loadCategories() {
        try {
            List<Category> categories = categoryService.getAllCategories();
            categoryItems.clear();

            // 添加"未分类"选项
            categoryItems.add(new CategoryItem(null, "未分类", null));

            // 添加所有分类
            for (Category category : categories) {
                categoryItems.add(new CategoryItem(
                        category.getId(),
                        category.getName(),
                        category.getColor()
                ));
            }

            // 更新下拉框
            removeAllItems();
            for (CategoryItem item : categoryItems) {
                addItem(item);
            }
        } catch (CategoryService.CategoryException e) {
            // 加载失败时只显示未分类选项
            categoryItems.clear();
            categoryItems.add(new CategoryItem(null, "未分类", null));
            removeAllItems();
            addItem(categoryItems.get(0));
        }
    }

    /**
     * 获取选中的分类ID
     * @return 分类ID，如果选择"未分类"则返回null
     */
    public Integer getSelectedCategoryId() {
        CategoryItem selectedItem = (CategoryItem) getSelectedItem();
        return selectedItem != null ? selectedItem.id() : null;
    }

    /**
     * 设置选中的分类ID
     * @param categoryId 分类ID，null表示选择"未分类"
     */
    public void setSelectedCategoryId(Integer categoryId) {
        for (int i = 0; i < getCategoryCount(); i++) {
            CategoryItem item = getItemAt(i);
            if (item != null) {
                if (categoryId == null && item.id() == null) {
                    setSelectedIndex(i);
                    return;
                } else if (categoryId != null && categoryId.equals(item.id())) {
                    setSelectedIndex(i);
                    return;
                }
            }
        }
        // 如果没找到，选择"未分类"
        if (getCategoryCount() > 0) {
            setSelectedIndex(0);
        }
    }

    /**
     * 获取分类数量
     * @return 分类数量
     */
    public int getCategoryCount() {
        return getItemCount();
    }

    /**
     * 分类项记录
     */
    public record CategoryItem(Integer id, String name, String color) {
        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * 分类列表单元格渲染器
     */
    private static class CategoryListCellRenderer extends DefaultListCellRenderer {
        private static final int COLOR_BOX_SIZE = 14;
        private static final int COLOR_BOX_MARGIN = 5;

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof CategoryItem item) {
                setText(item.name());
                putClientProperty("CategoryItem", item);

                // 设置工具提示
                if (item.id() == null) {
                    list.setToolTipText("不选择分类");
                } else {
                    list.setToolTipText(item.name() + (item.color() != null ? " (" + item.color() + ")" : ""));
                }
            } else {
                putClientProperty("CategoryItem", null);
            }

            return c;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (getComponentCount() > 0) {
                return; // 不在下拉列表中绘制颜色框
            }

            // 获取当前项
            Object value = getClientProperty("CategoryItem");
            if (value instanceof CategoryItem item && item.color() != null && !item.color().isEmpty()) {
                try {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int x = getWidth() - COLOR_BOX_SIZE - COLOR_BOX_MARGIN;
                    int y = (getHeight() - COLOR_BOX_SIZE) / 2;

                    Color color = Color.decode(item.color());
                    g2d.setColor(color);
                    g2d.fillRoundRect(x, y, COLOR_BOX_SIZE, COLOR_BOX_SIZE, 3, 3);

                    g2d.setColor(Color.DARK_GRAY);
                    g2d.drawRoundRect(x, y, COLOR_BOX_SIZE, COLOR_BOX_SIZE, 3, 3);

                    g2d.dispose();
                } catch (NumberFormatException ignored) {
                    // 颜色格式错误，不绘制
                }
            }
        }
    }
}
