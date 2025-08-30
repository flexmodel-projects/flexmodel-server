# Flexmodel Server 项目代码风格规则

## 项目概述
这是一个基于 Quarkus 3 的 Java 21 微服务项目，采用 DDD（领域驱动设计）分层架构，提供统一数据访问层和 API 设计平台。

## 技术栈
- **语言**: Java 21
- **框架**: Quarkus 3
- **构建工具**: Maven
- **数据库**: SQLite（默认）/ 可扩展
- **依赖注入**: CDI (Contexts and Dependency Injection)
- **API文档**: OpenAPI/Swagger
- **测试**: JUnit 5 + REST Assured
- **工具**: Lombok, GraphQL Java

## 代码风格规范

### 1. 包结构规范
```
tech.wetech.flexmodel/
├── interfaces/          # 接口层 - REST API、协议适配
├── application/         # 应用层 - 用例编排、DTO、事务脚本
├── domain/             # 领域层 - 核心业务模型、领域服务
├── infrastructure/     # 基础设施层 - 外部资源适配
└── shared/             # 公共工具和通用能力
```

### 2. 命名规范
- **类名**: PascalCase，如 `ChatResource`、`BusinessException`
- **方法名**: camelCase，如 `sendMessage`、`getSettings`
- **常量**: UPPER_SNAKE_CASE，如 `ROOT_PATH`
- **包名**: 全小写，如 `tech.wetech.flexmodel.interfaces.rest`

### 3. 注解使用规范
- 使用 `@Slf4j` 进行日志记录
- 使用 `@ApplicationScoped` 进行依赖注入
- 使用 `@Path` 定义 REST 路径
- 使用 `@Inject` 进行依赖注入
- 使用 `@Test` 标记测试方法

### 4. 代码格式规范
- 缩进使用 2 个空格
- 类和方法之间空一行
- 导入语句按字母顺序排列
- 使用 Lombok 简化代码（如 `@Slf4j`）

### 5. 异常处理规范
- 继承 `BusinessException` 创建业务异常
- 异常类使用抽象类设计
- 提供带消息和原因的构造函数

### 6. 配置管理规范
- 使用 `@ConfigMapping` 进行配置映射
- 配置接口使用 `@WithName` 和 `@WithDefault` 注解
- 支持可选配置项使用 `Optional<T>`

### 7. 测试规范
- 使用 `@QuarkusTest` 进行集成测试
- 使用 `@QuarkusTestResource` 配置测试资源
- 测试方法使用描述性命名
- 使用 REST Assured 进行 API 测试
- 测试数据使用中文注释说明

### 8. API 设计规范
- REST 基础路径为 `/api`
- 使用 HTTP 标准方法（GET、POST、PUT、PATCH、DELETE）
- 返回 JSON 格式数据
- 支持 SSE（Server-Sent Events）流式输出

### 9. 文档规范
- 类和方法添加 JavaDoc 注释
- 使用 `@author` 标记作者
- README 使用中文编写
- 配置说明详细完整

### 10. 依赖管理规范
- 使用 Maven 进行依赖管理
- 版本号统一在 `pom.xml` 中管理
- 使用 Quarkus BOM 管理依赖版本
- 第三方依赖明确指定版本

## 代码生成规则

### 1. 新类创建
- 遵循 DDD 分层架构
- 添加适当的注解
- 包含必要的导入语句
- 添加作者注释

### 2. 测试类创建
- 继承现有测试模式
- 使用 `@QuarkusTest` 注解
- 包含完整的测试用例
- 使用中文注释说明测试场景

### 3. 配置类创建
- 使用 `@ConfigMapping` 接口
- 提供默认值
- 支持可选配置
- 添加配置说明

### 4. 异常类创建
- 继承 `BusinessException`
- 提供多个构造函数
- 添加业务含义说明

## 最佳实践

### 1. 性能优化
- 使用连接池（HikariCP）
- 启用缓存机制
- 支持异步处理
- 流式输出大数据

### 2. 安全性
- 支持 JWT 认证
- 实现速率限制
- 配置安全策略
- 环境变量管理敏感信息

### 3. 可维护性
- 清晰的包结构
- 统一的命名规范
- 完整的测试覆盖
- 详细的文档说明

### 4. 可扩展性
- 插件化架构
- 支持多种数据源
- 模块化设计
- 配置驱动开发

## 注意事项
- 所有代码注释使用中文
- 保持代码风格一致性
- 遵循 DDD 设计原则
- 重视测试覆盖率
- 关注性能和安全性

## AI 助手指令

当AI助手帮助您开发时，请遵循以下指令：

1. **始终使用中文回复**
2. **请不要添加用不上的方法**
3. **尽量只修改单个文件**