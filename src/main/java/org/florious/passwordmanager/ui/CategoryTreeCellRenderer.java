package org.florious.passwordmanager.ui;

import org.florious.passwordmanager.model.Category;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * 分类树单元格渲染器
 * 显示分类名称，并在名称左侧绘制颜色圆点
 */
class CategoryTreeCellRenderer extends DefaultTreeCellRenderer {
    private static final int DOT_SIZE = 10;
    private Category currentCategory;

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (value instanceof DefaultMutableTreeNode node
                && node.getUserObject() instanceof Category category) {
            currentCategory = category;
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            setText(category.getName());
        } else {
            currentCategory = null;
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        }
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (currentCategory != null && currentCategory.getColor() != null
                && !currentCategory.getColor().isEmpty()) {
            try {
                Color dotColor = Color.decode(currentCategory.getColor());

                // 先绘制背景和文本
                super.paintComponent(g);

                // 在文本左侧绘制颜色圆点
                int iconWidth = 0;
                if (getIcon() != null) {
                    iconWidth = getIcon().getIconWidth() + getIconTextGap();
                }
                int dotX = iconWidth;
                int dotY = (getHeight() - DOT_SIZE) / 2;

                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(dotColor);
                g2d.fillOval(dotX, dotY, DOT_SIZE, DOT_SIZE);
                g2d.dispose();
                return;
            } catch (NumberFormatException ignored) {
                // 颜色格式错误，不绘制
            }
        }
        super.paintComponent(g);
    }
}
