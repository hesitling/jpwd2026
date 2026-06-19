package org.florious.passwordmanager.ui;

import org.florious.passwordmanager.model.Category;
import org.florious.passwordmanager.service.CategoryService;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.EventObject;

/**
 * 分类树内联编辑器
 * 支持双击分类节点进入编辑模式，验证后保存到数据库
 */
class CategoryTreeCellEditor extends DefaultTreeCellEditor {
    private final CategoryService categoryService;
    private final JTree tree;
    private final Runnable onEditSuccess;
    private Category editingCategory;
    private String originalName;

    /**
     * 创建分类树编辑器
     * @param tree 目标树
     * @param renderer 渲染器
     * @param categoryService 分类服务
     * @param onEditSuccess 编辑成功后的回调（用于刷新状态等）
     */
    public CategoryTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer,
            CategoryService categoryService, Runnable onEditSuccess) {
        super(tree, renderer);
        this.tree = tree;
        this.categoryService = categoryService;
        this.onEditSuccess = onEditSuccess;
    }

    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value,
            boolean isSelected, boolean expanded, boolean leaf, int row) {
        Component c = super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
        if (value instanceof DefaultMutableTreeNode node
                && node.getUserObject() instanceof Category category) {
            editingCategory = category;
            originalName = category.getName();
            if (c instanceof JTextField textField) {
                textField.setText(originalName);
                textField.selectAll();
            }
        } else {
            editingCategory = null;
            originalName = null;
        }
        return c;
    }

    @Override
    public boolean stopCellEditing() {
        if (editingCategory == null) {
            return super.stopCellEditing();
        }

        String newName = getCellEditorValue().toString().trim();

        // 名称未变更
        if (newName.equals(originalName)) {
            cancelCellEditing();
            return true;
        }

        // 空名称检查
        if (newName.isEmpty()) {
            JOptionPane.showMessageDialog(tree.getParent(), "分类名称不能为空", "错误",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }

        try {
            Category updated = categoryService.updateCategory(editingCategory.getId(),
                    newName, editingCategory.getColor());

            // 局部更新节点
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node != null) {
                node.setUserObject(updated);
                ((DefaultTreeModel) tree.getModel()).nodeChanged(node);
            }

            editingCategory = null;
            originalName = null;
            if (onEditSuccess != null) {
                onEditSuccess.run();
            }
            return super.stopCellEditing();
        } catch (CategoryService.CategoryException e) {
            JOptionPane.showMessageDialog(tree.getParent(), e.getMessage(), "更新失败",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(tree.getParent(), "更新分类失败: " + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    @Override
    public void cancelCellEditing() {
        editingCategory = null;
        originalName = null;
        super.cancelCellEditing();
    }

    @Override
    public boolean isCellEditable(EventObject e) {
        if (e instanceof MouseEvent mouseEvent) {
            // 只有双击才触发编辑
            if (mouseEvent.getClickCount() >= 2) {
                int row = tree.getRowForLocation(mouseEvent.getX(), mouseEvent.getY());
                if (row >= 0) {
                    TreePath path = tree.getPathForRow(row);
                    if (path != null) {
                        Object node = path.getLastPathComponent();
                        if (node instanceof DefaultMutableTreeNode treeNode
                                && treeNode.getUserObject() instanceof Category) {
                            return super.isCellEditable(e);
                        }
                    }
                }
            }
            return false;
        }
        return super.isCellEditable(e);
    }
}
