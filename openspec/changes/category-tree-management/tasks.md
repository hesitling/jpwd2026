## 1. 数据模型与树节点重构

- [x] 1.1 修改 `loadCategoryTree()`：将节点 userObject 从 `String`（category.getName()）改为 `Category` 对象本身
- [x] 1.2 修改 `filterByCategory()`：参数从 `String categoryName` 改为 `Integer categoryId`，移除按名称反查 ID 的逻辑
- [x] 1.3 修改选择监听器：从节点取出 `Category` 对象，调用 `filterByCategory(category.getId())`；根节点传 `null`

## 2. 自定义渲染器

- [x] 2.1 创建 `CategoryTreeCellRenderer` 内部类，继承 `DefaultTreeCellRenderer`
- [x] 2.2 实现 `getTreeCellRendererComponent()`：从 Category 对象取名称显示；根节点显示"所有分类"
- [x] 2.3 重写 `paintComponent()`：在分类名称左侧绘制颜色圆点（直径 10px），颜色取自 Category.color；无颜色时不绘制

## 3. 内联编辑器

- [x] 3.1 创建 `CategoryTreeCellEditor` 内部类，继承 `DefaultTreeCellEditor`
- [x] 3.2 重写 `getTreeCellEditorComponent()`：取出 Category 对象，保存 originalName，返回预填名称的 JTextField
- [x] 3.3 重写 `stopCellEditing()`：验证新名称（空值检查、重名检查、未变更检查），调用 `CategoryService.updateCategory()`，成功后局部更新节点 userObject 并通知模型
- [x] 3.4 验证失败时显示错误提示并返回 `false` 保持编辑状态；成功时返回 `true` 退出编辑
- [x] 3.5 在 `createCategoryPanel()` 中启用 `categoryTree.setEditable(true)` 并设置自定义编辑器和渲染器

## 4. 底部按钮面板

- [x] 4.1 在 `createCategoryPanel()` 中创建底部按钮面板（FlowLayout），包含「添加」和「删除」按钮
- [x] 4.2 删除按钮初始禁用；添加选择监听器：选中分类节点时启用，选中根节点或取消选中时禁用
- [x] 4.3 将按钮面板添加到分类面板的 `BorderLayout.SOUTH`

## 5. 添加按钮逻辑

- [x] 5.1 实现添加按钮点击事件：弹出 `JOptionPane.showInputDialog()` 输入分类名称
- [x] 5.2 调用 `CategoryService.createCategory(name, null)`，处理成功/失败（空名称、重名等错误提示）
- [x] 5.3 成功后调用 `loadCategoryTree()` 全量重建树

## 6. 删除按钮逻辑

- [x] 6.1 实现删除按钮点击事件：取出选中节点的 Category 对象
- [x] 6.2 调用 `CategoryService.getPasswordEntryCount()` 检查关联条目数量
- [x] 6.3 根据条目数量显示不同的确认对话框（有条目时提示将变为未分类）
- [x] 6.4 确认后调用 `CategoryService.deleteCategory()`，成功后调用 `loadCategoryTree()` 全量重建树

## 7. 集成验证

- [x] 7.1 验证添加分类后树正确刷新、新节点可见
- [x] 7.2 验证双击编辑重命名成功、重名/空名被拒绝
- [x] 7.3 验证删除分类（有/无关联条目）流程正确
- [x] 7.4 验证删除按钮在不同选中状态下的禁用/启用行为
- [x] 7.5 验证颜色圆点正确显示
