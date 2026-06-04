## 1. 创建 PasswordDetailDialog

- [x] 1.1 创建 PasswordDetailDialog 类，包含所有字段的显示
- [x] 1.2 实现密码显示/隐藏切换功能
- [x] 1.3 实现字段复制功能（标题、用户名、密码、URL）
- [x] 1.4 实现编辑按钮（关闭详情对话框并打开 PasswordDialog）
- [x] 1.5 实现关闭按钮和 Escape 键关闭功能

## 2. 修改 PasswordTable 显示分类名称

- [x] 2.1 修改 PasswordTableModel.getValueAt() 方法，查询 CategoryService 获取分类名称
- [x] 2.2 实现分类名称缓存，避免重复查询
- [x] 2.3 处理分类不存在的情况（显示"未分类"）

## 3. 连接 MainFrame 的菜单和工具栏功能

- [x] 3.1 修改 showAddPasswordDialog() 方法，创建并显示 PasswordDialog（添加模式）
- [x] 3.2 修改 showEditPasswordDialog() 方法，获取选中的密码条目并显示 PasswordDialog（编辑模式）
- [x] 3.3 修改 showSettingsDialog() 方法，创建并显示 SettingsDialog
- [x] 3.4 修改 showSearchDialog() 方法，将焦点设置到 VaultPanel 的搜索框
- [x] 3.5 修改 PasswordTable 的双击事件，显示 PasswordDetailDialog

## 4. 实现删除密码功能

- [x] 4.1 修改 deleteSelectedPassword() 方法，获取选中的密码条目
- [x] 4.2 实现确认对话框，显示将要删除的密码条目数量
- [x] 4.3 调用 VaultService 删除密码条目
- [x] 4.4 删除后刷新密码列表

## 5. 实现数据刷新功能

- [x] 5.1 修改 refreshData() 方法，调用 VaultPanel 的 refresh() 方法
- [x] 5.2 实现分类树的刷新
- [x] 5.3 在状态栏显示刷新结果

## 6. 集成 SessionStatusBar

- [x] 6.1 在 MainFrame 的状态栏中添加 SessionStatusBar 组件
- [x] 6.2 实现会话状态的实时更新
- [x] 6.3 实现锁定功能的连接

## 7. 测试和验证

- [ ] 7.1 测试所有菜单项和工具栏按钮的功能
- [ ] 7.2 测试删除功能的确认对话框
- [ ] 7.3 测试密码详情对话框的显示和操作
- [ ] 7.4 测试分类名称的显示
- [ ] 7.5 测试数据刷新功能
