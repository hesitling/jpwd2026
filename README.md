# 密码管理器

一个安全的桌面密码管理应用程序，使用 Java Swing 开发，支持 AES-256 加密和 Argon2id 密码哈希。

## 功能特性

- **安全存储**：AES-256-GCM 加密所有密码数据
- **强密码哈希**：使用 Argon2id 算法保护主密码
- **密码生成器**：可自定义长度和字符类型的强密码生成
- **分类管理**：创建分类组织密码条目
- **快速搜索**：按标题、用户名、URL 搜索密码
- **导入导出**：支持 CSV 格式导入导出
- **会话管理**：自动超时锁定保护隐私
- **剪贴板安全**：自动清除剪贴板中的密码

## 系统要求

- Java 21 或更高版本
- Windows、macOS 或 Linux

## 快速开始

### 安装

1. 确保已安装 [Java 21](https://adoptium.net/) 或更高版本
2. 克隆或下载项目
3. 运行启动脚本：
   ```bash
   # Linux/macOS
   ./run.sh
   
   # Windows
   run.bat
   ```

### 首次使用

1. 启动应用程序
2. 点击"注册"创建账户
3. 设置强主密码（请牢记，忘记无法恢复）
4. 使用主密码登录

### 基本操作

**添加密码**：
1. 点击工具栏"添加"按钮
2. 填写标题、用户名、密码等信息
3. 点击"保存"

**搜索密码**：
1. 在搜索框输入关键词
2. 点击"搜索"或按回车

**分类管理**：
1. 在左侧分类树查看分类
2. 点击分类筛选密码
3. 右键编辑或删除分类

## 安全特性

### 加密机制
- 主密码：Argon2id 哈希 + 随机盐值
- 数据加密：AES-256-GCM + 随机 IV
- 密钥派生：PBKDF2 从主密码派生加密密钥

### 会话安全
- 自动超时锁定（默认 5 分钟）
- 手动锁定：`Ctrl+L`
- 退出时清除内存敏感数据

### 剪贴板保护
- 复制密码后自动清除（默认 30 秒）
- 可配置清除时间

## 配置

配置文件位置：`~/.config/jpwd/config.properties`

```properties
# 会话超时时间（分钟）
session.timeout=5

# 剪贴板清除时间（秒）
clipboard.clear.timeout=30

# 是否显示复制确认对话框
clipboard.show.copy.confirm=true
```

## 快捷键

| 快捷键 | 功能 |
|--------|------|
| `Ctrl+N` | 添加新密码 |
| `Ctrl+E` | 编辑选中的密码 |
| `Delete` | 删除选中的密码 |
| `Ctrl+F` | 搜索 |
| `Ctrl+L` | 锁定应用程序 |
| `Ctrl+G` | 打开密码生成器 |

## 导入导出

### 导出
1. 菜单：文件 → 导出...
2. 选择保存位置
3. 密码将解密后导出为 CSV

### 导入
1. 菜单：文件 → 导入...
2. 选择 CSV 文件
3. 选择重复处理方式（跳过/覆盖/重命名）

CSV 格式：`title,username,password,url,notes,category`

## 备份

数据库文件位置：
- Windows：`%USERPROFILE%\.config\jpwd\passwords.db`
- macOS/Linux：`~/.config/jpwd/passwords.db`

定期备份此文件以防止数据丢失。

## 故障排除

**忘记主密码**：无法恢复，需重新创建账户

**无法启动**：检查 Java 版本（`java -version`）

**导入失败**：检查 CSV 格式和编码（UTF-8）

## 开发

详见 [CONTRIBUTING.md](CONTRIBUTING.md)

## 许可证

[MIT License](LICENSE)

## 贡献

欢迎提交 Issue 和 Pull Request！