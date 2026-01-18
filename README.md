# flexmodel-server 

面向下一代应用程序的统一数据访问层，开源、自主可控的数据处理平台，让数据接口开发更简单、更高效。

### 特性
- 统一数据访问层：抽象底层数据源差异，屏蔽复杂性。
- API 设计与生成：基于模型定义快速生成标准化接口与代码模板。
- OpenAPI 支持：内置文档与调试入口，便于前后端协作。
- 可插拔模型与存储：支持多种数据库驱动与扩展组件。
- 流式对话能力：提供 Chat Completions SSE 流式输出接口，便于构建智能应用。

### 技术栈
- 语言与运行时：Java 21
- 主框架：Quarkus 3（REST、Jackson、Hibernate Validator、Scheduler、Cache）
- 文档与调试：OpenAPI/Swagger UI
- AI 能力：LangChain4j OpenAI 兼容接口（StreamingChatModel，SSE 流式返回）
- 数据库与连接池：SQLite（演示默认）/ 可扩展，HikariCP
- 依赖与构建：Maven，Quarkus Maven Plugin
- 辅助：Lombok、GraphQL Java、Docker（容器镜像构建）、Groovy（代码模板/生成）
- 相关组件：`flexmodel-core`、`flexmodel-graphql`、`flexmodel-ui`、`flexmodel-code-templates`

### 分层架构
项目采用DDD分层与按职责划分的包结构，便于演进与扩展：
- 接口层（Interfaces）：`dev.flexmodel.interfaces`
  - 提供 REST API（如 `rest.ChatResource`），对外暴露 HTTP 接口与协议适配。
- 应用层（Application）：`dev.flexmodel.application`
  - 编排领域用例与服务，处理事务脚本、DTO/Assembler、流程处理器等。
- 领域层（Domain）：`dev.flexmodel.domain`
  - 核心领域模型与领域服务，聚合根、实体、值对象、仓储接口等。
- 基础设施层（Infrastructure）：`dev.flexmodel.infrastructrue`
  - 外部资源适配与实现，如持久化、会话、消息消费、任务调度等。
- 公共与工具（shared）：`dev.flexmodel.shared`
  - 公共工具与通用能力（JSON、字符串、路径匹配等）。

说明：REST 基础路径为 `/api`；OpenAPI/Swagger UI 位于 `/q/swagger-ui`；SSE 流式聊天接口在 `chat` 资源下提供。

### 快速开始
1. 准备环境：
   - JDK 21+
   - Maven 3.9+

2. 克隆并启动（Windows）：
```
mvnw.cmd quarkus:dev
```
首次启动会生成/下载依赖，并在开发模式下热加载。

3. 访问入口：
- OpenAPI/Swagger UI: `http://localhost:8080/q/swagger-ui`
- REST 基础路径：`/api`
- 首页（如内置前端资源存在）：`http://localhost:8080/`


### 配置说明
项目默认使用 SQLite 作为演示数据源，核心配置位于 `src/main/resources/application.properties`。
- REST 前缀：`quarkus.rest.path=/api`
- OpenAPI UI：`quarkus.swagger-ui.always-include=true`

如需集成大模型服务，请按需配置相应供应商的 `base-url` 与 `api-key`（建议使用环境变量或本地不提交的配置文件覆盖，不要在公共仓库中明文提交密钥）。

### 构建与打包
- 运行单元测试并打包：
```
mvnw.cmd clean package
```
- 以可执行 JAR 运行（示例）：
```
java -jar target/flexmodel-server-dev.jar
```

### 目录结构（节选）
- `src/main/java/tech/wetech/flexmodel`：核心代码与 REST 接口
- `src/main/resources`：应用配置、OpenAPI、静态资源
- `src/test/java`：测试用例

### 许可证
本项目采用开源许可，详见仓库根目录中的许可证文件（如有）。

