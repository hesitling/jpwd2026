## 为什么

当前锁定流程存在体验问题：用户锁定应用后直接跳转到登录面板（LoginPanel），需要重新输入用户名和密码。已存在一个 LockPanel 组件（只需输入密码），但未被集成到 MainFrame 中。此外，会话超时后也应该显示锁定界面而非登录界面。

## 变更内容

1. **集成 LockPanel 到 MainFrame**：
   - 将 LockPanel 添加到 CardLayout 中
   - 添加 `showLockPanel()` 方法
   - 修改 `lockApplication()` 显示 LockPanel 而非 LoginPanel

2. **保存锁定前的用户信息**：
   - 锁定时保存当前用户名
   - LockPanel 显示欢迎信息（如"欢迎回来，admin"）

3. **实现解锁逻辑**：
   - 设置 LockPanel 的 UnlockCallback
   - 验证密码成功后重新创建会话并跳转到 VaultPanel
   - 验证失败显示错误信息

4. **处理会话超时**：
   - MainFrame 实现 SessionListener 接口
   - 会话超时时自动显示 LockPanel

## 功能 (Capabilities)

### 新增功能
- `lock-screen`: 锁定屏幕功能，会话锁定或超时后显示 LockPanel，支持输入密码解锁

### 修改功能
（无）

## 影响

- **代码**: MainFrame.java（主要修改）
- **UI 组件**: LockPanel（已有，无需修改）、SessionManager（已有 SessionListener 接口）
- **用户体验**: 锁定后只需输入密码即可解锁，无需重新输入用户名
