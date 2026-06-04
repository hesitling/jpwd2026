## 为什么

MainFrame 中有多个 TODO 方法未实现，导致菜单栏和工具栏的按钮点击后显示"待实现"的提示对话框，而不是实际的功能。这些方法应该连接到已实现的对话框组件（如 PasswordDialog、SettingsDialog 等），以提供完整的用户体验。

## 变更内容

1. **连接 MainFrame 的 TODO 方法**：
   - `showAddPasswordDialog()` → 连接到 PasswordDialog（添加模式）
   - `showEditPasswordDialog()` → 连接到 PasswordDialog（编辑模式）
   - `deleteSelectedPassword()` → 实现删除功能
   - `showSearchDialog()` → 连接到 VaultPanel 的搜索功能
   - `showSettingsDialog()` → 连接到 SettingsDialog
   - `refreshData()` → 实现数据刷新

2. **增强 PasswordTable**：
   - 双击显示密码详情时，显示完整的密码详情对话框（而不是简单的 JOptionPane）
   - 分类列显示实际的分类名称（而不是硬编码的"分类"）

3. **集成 SessionStatusBar**：
   - 将 SessionStatusBar 添加到 MainFrame 的状态栏中

## 功能 (Capabilities)

### 新增功能
- `ui-connection`: 连接 MainFrame 中的 TODO 方法到已实现的对话框组件
- `password-detail-dialog`: 创建密码详情对话框，用于显示完整的密码信息
- `category-display`: 在 PasswordTable 中显示实际的分类名称

### 修改功能
（无）

## 影响

- **代码**: MainFrame.java, PasswordTable.java, VaultPanel.java
- **UI 组件**: PasswordDialog, SettingsDialog, SessionStatusBar
- **用户体验**: 用户可以正常使用菜单栏和工具栏的所有功能
