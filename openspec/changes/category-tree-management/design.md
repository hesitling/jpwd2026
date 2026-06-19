## 上下文

当前 `VaultPanel` 的分类树使用 `DefaultMutableTreeNode` 存储纯字符串作为 userObject。树仅用于展示和筛选，不具备编辑能力。`CategoryService` 已提供完整的 CRUD API（`createCategory`、`updateCategory`、`deleteCategory`、`getPasswordEntryCount`），无需后端改动。

分类树的构建逻辑在 `VaultPanel.createCategoryPanel()` 和 `loadCategoryTree()` 中。筛选逻辑在 `filterByCategory(String categoryName)` 中，通过分类名称反查 ID，存在一次冗余的数据库查询。

## 目标 / 非目标

**目标：**
- 在分类树底部提供「添加」和「删除」按钮，作为分类管理的快捷入口
- 支持双击分类节点进行内联重命名编辑
- 分类节点显示颜色圆点，增强视觉辨识
- 消除 `filterByCategory` 中按名称反查 ID 的冗余查询

**非目标：**
- 不修改 `CategoryService` 或 `CategoryRepository`（已有完整 API）
- 不删除或重构 `CategoryManagementDialog`（保留作为备选入口）
- 不在内联编辑中支持颜色修改（颜色修改仍通过其他途径）
- 不实现拖拽排序或拖拽移动分类

## 决策

### 决策 1：节点存储 Category 对象替代字符串

**选择**：将 `Category` 对象作为 `DefaultMutableTreeNode` 的 userObject。

**替代方案**：
- 保持字符串 + 额外维护一个 `Map<TreeNode, Category>` 映射 → 增加了同步负担
- 自定义 TreeNode 子类 → 过度设计

**理由**：`DefaultMutableTreeNode` 的 userObject 本身就是 Object 类型，直接存 Category 最简单。渲染器和编辑器都可以通过 `getUserObject()` 获取完整信息，无需额外查询。

### 决策 2：使用 DefaultTreeCellEditor 扩展内联编辑

**选择**：创建 `CategoryTreeCellEditor` 内部类，继承 `DefaultTreeCellEditor`，重写 `stopCellEditing()` 进行验证和持久化。

**替代方案**：
- 监听 `TreeModelListener` 的 `treeNodesChanged` 事件 → 事件触发时无法阻止，验证逻辑不好做
- 使用 `JTextField` + `FocusListener` 手动实现 → 需要处理太多边界情况（键盘事件、焦点管理）

**理由**：`DefaultTreeCellEditor` 已处理了编辑组件的显示/隐藏、键盘事件（Enter/Escape）、焦点管理。我们只需重写 `stopCellEditing()` 加入业务验证，是侵入性最小的方式。

### 决策 3：混合刷新策略

**选择**：
- 内联编辑成功后 → 局部更新节点（`node.setUserObject()` + `model.nodeChanged(node)`）
- 添加/删除操作后 → 全量重建树（`loadCategoryTree()`）

**替代方案**：
- 全部全量重建 → 简单但丢失展开/选中状态
- 全部局部更新 → 添加时需要维护排序顺序，删除时需要处理相邻节点选中

**理由**：内联编辑只改变名称，不影响树结构，局部更新可保持展开状态。添加/删除会改变树的节点集合和顺序，全量重建更可靠。

### 决策 4：按钮栏放在分类面板底部

**选择**：在分类树的 `JScrollPane` 下方放置按钮面板。

**替代方案**：
- 放在标题栏同行 → 空间拥挤
- 放在树上方 → 不符合操作区域的视觉分层
- 右键上下文菜单 → 可发现性低

**理由**：底部按钮符合「操作区在内容下方」的常见布局模式，且易于实现禁用/启用状态切换。

### 决策 5：添加操作使用简单输入框

**选择**：点击「添加」按钮后使用 `JOptionPane.showInputDialog()` 输入分类名称。

**替代方案**：
- 复用 `CategoryManagementDialog.CategoryEditDialog` → 可以选颜色，但增加了操作步骤

**理由**：快速添加场景下，名称是最核心的信息。颜色可后续通过其他方式补充。保持添加操作轻量。

## 风险 / 权衡

| 风险 | 缓解措施 |
|------|----------|
| 内联编辑时 `stopCellEditing()` 调用服务层可能抛异常，导致编辑状态卡住 | 在 catch 块中显示错误消息并返回 `false`（保持编辑状态），让用户修正后重试 |
| 全量重建树后展开/选中状态丢失 | 重建后自动展开所有节点（已有逻辑），暂不恢复选中状态 |
| `CategoryService` 在 EDT 线程中执行数据库操作，可能造成 UI 卡动 | 当前项目所有数据库操作都在 EDT 中执行，保持一致。后续可统一优化为后台线程 |
| 根节点"所有分类"不应被编辑或删除 | 编辑器中检查节点类型，根节点跳过编辑；删除按钮在选中根节点时禁用 |
