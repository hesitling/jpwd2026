## 1. 集成 LockPanel 到 MainFrame

- [x] 1.1 添加 LOCK_PANEL 常量和 LockPanel 字段到 MainFrame
- [ ] 1.2 在 setupContentPanel() 中创建 LockPanel 实例并添加到 CardLayout
- [ ] 1.3 添加 showLockPanel() 方法

## 2. 保存锁定前的用户名

- [ ] 2.1 添加 lockedUsername 字段到 MainFrame
- [ ] 2.2 修改 lockApplication() 保存当前用户名

## 3. 实现解锁逻辑

- [ ] 3.1 在 LockPanel 中显示欢迎信息（欢迎回来，{username}）
- [ ] 3.2 设置 LockPanel 的 UnlockCallback 实现
- [ ] 3.3 解锁成功时调用 authService.login() 并显示 VaultPanel
- [ ] 3.4 解锁失败时显示错误信息

## 4. 处理会话事件

- [ ] 4.1 MainFrame 实现 SessionListener 接口
- [ ] 4.2 实现 onSessionTimeout() 显示 LockPanel
- [ ] 4.3 实现 onSessionLocked() 显示 LockPanel
- [ ] 4.4 在构造函数中注册 SessionListener

## 5. 测试和验证

- [ ] 5.1 测试手动锁定后显示 LockPanel
- [ ] 5.2 测试会话超时后显示 LockPanel
- [ ] 5.3 测试密码正确解锁成功
- [ ] 5.4 测试密码错误显示错误信息
