# 贡献指南

感谢您对密码管理器项目的关注！本文档将帮助您参与项目开发。

## 开发环境

### 环境要求

- Java 21 JDK
- Maven 3.6+
- Git

### 环境设置

1. **克隆项目**
   ```bash
   git clone https://github.com/yourusername/jpwd.git
   cd jpwd
   ```

2. **安装依赖**
   ```bash
   mvn clean install
   ```

3. **运行测试**
   ```bash
   mvn test
   ```

4. **启动应用**
   ```bash
   ./run.sh  # Linux/macOS
   run.bat   # Windows
   ```

### IDE 配置

#### IntelliJ IDEA
1. 导入 Maven 项目
2. 设置 Java 21 SDK
3. 安装 Lombok 插件（可选）

#### Eclipse
1. 导入为 Maven 项目
2. 设置 Java 21 编译器
3. 配置代码格式化

## 项目结构

```
src/
├── main/
│   ├── java/
│   │   └── org/florious/passwordmanager/
│   │       ├── crypto/      # 加密服务
│   │       ├── model/       # 数据模型
│   │       ├── repository/  # 数据访问
│   │       ├── service/     # 业务逻辑
│   │       ├── ui/          # 用户界面
│   │       └── util/        # 工具类
│   └── resources/
│       └── config.properties
└── test/
    └── java/                # 测试代码
```

## 开发流程

### 分支策略

- `main`：稳定版本
- `develop`：开发分支
- `feature/*`：功能分支
- `fix/*`：修复分支
- `docs/*`：文档分支

### 工作流程

1. **创建分支**
   ```bash
   git checkout -b feature/your-feature develop
   ```

2. **开发功能**
   - 编写代码
   - 添加测试
   - 更新文档

3. **提交代码**
   ```bash
   git add .
   git commit -m "feat(scope): 描述"
   ```

4. **推送分支**
   ```bash
   git push origin feature/your-feature
   ```

5. **创建 Pull Request**

## 代码规范

### 命名规范

- **类名**：PascalCase（如 `CryptoService`）
- **方法名**：camelCase（如 `encryptPassword`）
- **常量**：UPPER_SNAKE_CASE（如 `MAX_PASSWORD_LENGTH`）
- **包名**：小写（如 `org.florious.passwordmanager`）

### 代码风格

1. **缩进**：4 个空格
2. **行宽**：120 字符
3. **导入顺序**：java → javax → org → com
4. **大括号**：换行风格

### 注释规范

1. **类和方法**：必须有 JavaDoc
2. **复杂算法**：详细注释
3. **语言**：使用中文注释

### 异常处理

1. 使用自定义异常类
2. 提供用户友好的错误消息
3. 记录异常日志

## 测试指南

### 单元测试

1. 测试类放在 `src/test/java` 对应包下
2. 类名以 `Test` 结尾
3. 使用 `@DisplayName` 提供中文描述

```java
@Test
@DisplayName("应该成功加密密码")
void shouldEncryptPassword() {
    // Given
    String plainPassword = "test123";
    
    // When
    String encrypted = cryptoService.encryptPassword(plainPassword, key);
    
    // Then
    assertNotNull(encrypted);
    assertNotEquals(plainPassword, encrypted);
}
```

### 集成测试

1. 测试多个组件交互
2. 使用内存数据库
3. 测试业务流程

### UI 测试

1. 测试组件创建
2. 测试事件处理
3. 测试用户交互

### 运行测试

```bash
# 所有测试
mvn test

# 特定测试类
mvn test -Dtest=CryptoServiceTest

# 特定测试方法
mvn test -Dtest=CryptoServiceTest#shouldEncryptPassword
```

## 提交规范

使用 [约定式提交](https://www.conventionalcommits.org/zh-hans/) 格式：

```
<type>(scope): <description>

[optional body]

[optional footer]
```

### 提交类型

- `feat`：新功能
- `fix`：修复
- `docs`：文档
- `style`：格式
- `refactor`：重构
- `test`：测试
- `chore`：构建/工具
- `perf`：性能优化

### 示例

```
feat(ui): 添加密码生成功能

- 实现密码生成器对话框
- 支持自定义密码策略
- 添加密码强度指示器

Closes #123
```

## 文档要求

### 代码文档

1. 所有公共类和方法必须有 JavaDoc
2. 复杂算法必须有详细注释
3. 配置项必须有说明

### 用户文档

1. 更新 README.md
2. 添加使用示例
3. 说明配置选项

### API 文档

1. 生成 JavaDoc
2. 说明参数和返回值
3. 提供使用示例

## 问题报告

### Bug 报告

使用 Issue 模板，包含：
1. 问题描述
2. 复现步骤
3. 期望行为
4. 实际行为
5. 环境信息

### 功能请求

使用 Issue 模板，包含：
1. 功能描述
2. 使用场景
3. 实现建议

## 代码审查

### 审查要点

1. **功能正确性**：是否满足需求
2. **代码质量**：是否符合规范
3. **测试覆盖**：是否有足够测试
4. **安全性**：是否有安全漏洞
5. **性能**：是否有性能问题

### 审查流程

1. 创建 Pull Request
2. 自动运行测试
3. 人工代码审查
4. 修复审查意见
5. 合并代码

## 发布流程

### 版本号

使用 [语义化版本](https://semver.org/lang/zh-CN/)：
- 主版本号：不兼容的 API 修改
- 次版本号：向下兼容的功能性新增
- 修订号：向下兼容的问题修正

### 发布步骤

1. 更新版本号
2. 更新 CHANGELOG.md
3. 创建发布分支
4. 运行完整测试
5. 创建 Git 标签
6. 构建发布包
7. 发布到仓库

## 开发工具

### 推荐工具

- **IDE**：IntelliJ IDEA
- **构建**：Maven
- **版本控制**：Git
- **数据库**：DB Browser for SQLite

### 调试技巧

1. **日志调试**：修改日志级别
2. **断点调试**：使用 IDE 调试器
3. **数据库调试**：查看 SQLite 数据库

## 常见问题

### 编译错误

1. 检查 Java 版本
2. 清理 Maven 缓存：`mvn clean`
3. 更新依赖：`mvn dependency:resolve`

### 测试失败

1. 检查测试环境
2. 查看测试报告
3. 单独运行失败测试

### 运行错误

1. 检查日志文件
2. 检查配置文件
3. 检查数据库权限

## 联系方式

- **Issues**：GitHub Issues
- **讨论**：GitHub Discussions
- **邮箱**：your-email@example.com

## 行为准则

### 我们的承诺

- 营造开放、友好的环境
- 尊重不同观点和经验
- 接受建设性批评
- 关注对社区最有利的事情

### 我们的责任

- 使用友好和包容的语言
- 尊重不同的观点和经验
- 优雅地接受建设性批评
- 关注对社区最有利的事情

## 许可证

贡献代码将采用与项目相同的 [MIT 许可证](LICENSE)。