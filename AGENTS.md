# AGENT.md

## 提交规范

- 提交信息必须遵循约定式提交格式：`<type>(scope): <中文说明>`。
- 说明部分必须使用中文，简洁描述变更结果。
- 常用类型：`feat`、`fix`、`refactor`、`test`、`docs`、`chore`。
- 示例：`feat(store): 接入 PostgreSQL 运行时`

## 文件大小约束

- 源文件上限 **400 行**，测试文件上限 **600 行**。
- 新增或修改文件后，必须确保该文件不超限。

## 日志规范

- 禁止使用 `System.out.println` 或 `System.err.println` 输出日志。
- 必须使用 SLF4J 日志门面：`org.slf4j.Logger` 和 `org.slf4j.LoggerFactory`。
- 日志声明：`private static final Logger logger = LoggerFactory.getLogger(ClassName.class);`
- 示例：`logger.info("用户登录成功: {}", username);`
