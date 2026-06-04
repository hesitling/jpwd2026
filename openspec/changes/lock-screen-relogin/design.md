## 上下文

MainFrame 使用 CardLayout 管理多个面板（LoginPanel、RegisterPanel、VaultPanel）。当前锁定流程直接跳转到 LoginPanel，要求用户输入用户名和密码。已存在 LockPanel 组件，只需输入密码即可解锁，但未被集成。

SessionManager 提供 SessionListener 接口，支持 onSessionTimeout() 和 onSessionLocked() 回调。

## 目标 / 非目标

**目标：**
- 锁定后显示 LockPanel（只需输入密码）
- 保存锁定前的用户名，用于解锁验证
- 会话超时后自动显示 LockPanel
- 解锁成功后恢复到 VaultPanel

**非目标：**
- 修改 LockPanel 的 UI（已实现）
- 添加解锁失败次数限制
- 添加"返回登录"按钮
- 显示锁定原因

## 决策

### 1. 在 MainFrame 中保存锁定前的用户名
**决策**: 添加 `lockedUsername` 字段，在锁定时保存当前用户名。

**理由**: 锁定后会话被销毁，用户名信息丢失。需要在锁定前保存，以便解锁时调用 `authService.login(username, password)`。

**替代方案**: 
- 从数据库查询最近登录的用户 - 增加复杂性，且可能有多个用户
- 在 SessionManager 中保存用户名 - 增加耦合

### 2. MainFrame 实现 SessionListener 接口
**决策**: 让 MainFrame 实现 SessionListener，在构造函数中注册监听器。

**理由**: 这是处理会话事件的标准方式，已有现成接口。

**替代方案**: 使用匿名内部类 - 代码分散，不利于维护。

### 3. 解锁失败时保持在 LockPanel
**决策**: 解锁失败时显示错误信息，保持在 LockPanel，不清空密码输入。

**理由**: 用户可能只是输错了密码，保持在当前界面方便重试。

**替代方案**: 失败后跳转到 LoginPanel - 用户体验差。

## 风险 / 权衡

### 风险 1: lockedUsername 可能与实际用户不一致
**风险**: 如果数据库被外部修改，lockedUsername 可能无效。

**缓解措施**: 解锁失败时提示"密码错误"，不泄露用户名是否存在。极端情况下用户可重启应用。

### 风险 2: 会话超时与手动锁定的处理时机
**风险**: 会话超时时 SessionManager 已销毁会话，需要确保 MainFrame 正确响应。

**缓解措施**: SessionListener 的回调在 SessionManager 内部调用，时序已保证。
