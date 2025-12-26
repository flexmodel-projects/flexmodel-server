# Flexmodel Overview

Flexmodel是面向下一代应用程序的统一数据访问层，支持生成GraphQLAPI、RestfulAPI访问数据，支持服务编排，Flexmodel支持一种专用的IDL（接口定义语言），这个IDL有如下规则
IDL使用类似TypeScript的语法，支持以下基本元素：

- **模型定义**: `model ModelName { ... }`
- **枚举定义**: `enum EnumName { ... }`
- **字段定义**: `fieldName: Type @annotation(...)`
- **注解**: `@annotationName(parameter: value)`
- **注释**: `// 单行注释` 或 `/* 多行注释 */`

## 模型定义

### 基本语法

```idl
model ModelName {
  fieldName: Type @annotation(...),
  optionalField?: Type @annotation(...),
}
```

### 字段类型

支持以下字段类型：

| 类型            | 描述     | 示例                          |
|---------------|--------|-----------------------------|
| `String`      | 字符串类型  | `name: String @length(255)` |
| `Int`         | 整数类型   | `age: Int`                  |
| `Long`        | 长整数类型  | `id: Long`                  |
| `Float`       | 浮点数类型  | `price: Float`              |
| `Boolean`     | 布尔类型   | `active: Boolean`           |
| `DateTime`    | 日期时间类型 | `createdAt: DateTime`       |
| `Date`        | 日期类型   | `birthday: Date`            |
| `Time`        | 时间类型   | `startTime: Time`           |
| `JSON`        | JSON类型 | `config: JSON`              |
| `EnumType`    | 枚举类型   | `gender: UserGender`        |
| `ModelType`   | 关联类型   | `student: Student`          |
| `ModelType[]` | 关联数组类型 | `students: Student[]`       |

### 字段修饰符

- **可选字段**: 在字段名后添加 `?` 表示该字段可为空
- **数组字段**: 在类型后添加 `[]` 表示该字段为数组类型

### 常用注解

| 注解                | 描述    | 示例                                  |
|-------------------|-------|-------------------------------------|
| `@id`             | 主键标识  | `id: String @id`                    |
| `@unique`         | 唯一约束  | `email: String @unique`             |
| `@default(value)` | 默认值   | `status: String @default("active")` |
| `@length(n)`      | 字符串长度 | `name: String @length(255)`         |
| `@comment(text)`  | 字段注释  | `name: String @comment("用户姓名")`     |
| `@relation(...)`  | 关联关系  | `student: Student @relation(...)`   |

### 主键注解

```idl
// UUID主键
id: String @id @default(uuid())

// 自增主键
id: Long @id @default(autoIncrement())

// UUID主键
id: String @id @default(uuid())
```

### 关联关系注解

```idl
// 一对一关系
studentDetail: StudentDetail @relation(localField: "id", foreignField: "studentId", cascadeDelete: true)

// 一对多关系
students: Student[] @relation(localField: "id", foreignField: "classId", cascadeDelete: true)

// 多对一关系
studentClass: Classes @relation(localField: "classId", foreignField: "id")
```

### 索引注解

```idl
// 单字段索引
@index(name: "IDX_studentName", unique: false, fields: [studentName])

// 复合索引
@index(name: "IDX_student_class", unique: false, fields: [classId, studentName: (sort: "desc")])

// 唯一索引
@index(name: "IDX_email", unique: true, fields: [email])
```

## 枚举定义

### 基本语法

```idl
enum EnumName {
  VALUE1,
  VALUE2,
  VALUE3
}
```

#### 示例

```idl
enum UserGender {
  UNKNOWN,
  MALE,
  FEMALE
}

enum ApiType {
  FOLDER,
  API
}
```

## 完整示例

### 学生管理系统

```idl
// 班级模型
model Classes {
  id: String @id @default(uuid()),
  classCode: String @unique @length(255),
  className?: String @default("A班级"),
  students: Student[] @relation(localField: "id", foreignField: "classId", cascadeDelete: true),
}

// 学生模型
model Student {
  id: String @id @default(uuid()),
  studentName?: String @length(255),
  gender?: UserGender,
  interest?: User_interest[],
  age?: Int,
  classId?: Long,
  studentClass: Classes @relation(localField: "classId", foreignField: "id"),
  studentDetail: StudentDetail @relation(localField: "id", foreignField: "studentId", cascadeDelete: true),
  createdAt?: DateTime @default(now()),
  updatedAt?: DateTime @default(now()),
  @index(name: "IDX_studentName", unique: false, fields: [classId, studentName: (sort: "desc")]),
  @index(unique: false, fields: [studentName]),
  @index(unique: false, fields: [classId]),
}

// 学生详情模型
model StudentDetail {
  id: String @id @default(autoIncrement()),
  studentId?: Long,
  description?: String @length(255),
}

// 用户性别枚举
enum UserGender {
  UNKNOWN,
  MALE,
  FEMALE
}

// 用户爱好枚举
enum user_interest {
  chang,
  tiao,
  rap,
  daLanQiu
}
```

#### 系统配置模型

```idl
// 数据源配置
model fs_datasource {
  name: String @id,
  type?: DatasourceType,
  config?: JSON,
  createdAt?: DateTime @default(now()),
  updatedAt?: DateTime @default(now()),
  enabled: Boolean @comment("数据源") @default(true),
}

// API定义
model fs_api_definition {
  id: String @id @unique @default(uuid()),
  name: String @length(255),
  parentId?: String @length(255),
  type: ApiType,
  method?: String @length(255),
  path?: String @length(255),
  createdAt: DateTime @default(now()),
  updatedAt: DateTime @default(now()),
  meta?: JSON,
  enabled: Boolean @comment("是否开启") @default(true),
}

// 用户模型
model fs_user {
  id: String @id @default(uuid()),
  username: String @length(255) @comment("用户名"),
  avatar?: String @length(255) @comment("头像"),
  password_hash: String @length(255) @comment("密码HASH"),
  created_at: DateTime @default(now()) @comment("创建时间"),
  updated_at: DateTime @default(now()) @comment("更新时间"),
  @index(name: "IDX_USERNAME", unique: true, fields: [username]),
  @comment("用户")
}

// 枚举定义
enum ApiType {
  FOLDER,
  API
}

enum DatasourceType {
  SYSTEM,
  USER
}
```

### 注解详解

#### @id 注解

标识主键字段，支持以下生成策略：

```idl
// UUID生成
id: String @id @default(uuid())

// 自增ID
id: Long @id @default(autoIncrement())

// UUID生成
id: String @id @default(uuid())

// 不自动生成
id: String @id
```

#### @default 注解

设置字段默认值：

```idl
// 字符串默认值
status: String @default("active")

// 数字默认值
count: Int @default(0)

// 布尔默认值
enabled: Boolean @default(true)

// 函数默认值
createdAt: DateTime @default(now())
```

#### @relation 注解

定义关联关系，支持以下参数：

| 参数              | 类型      | 描述     | 示例                          |
|-----------------|---------|--------|-----------------------------|
| `localField`    | String  | 本地字段名  | `localField: "id"`          |
| `foreignField`  | String  | 外键字段名  | `foreignField: "studentId"` |
| `cascadeDelete` | Boolean | 是否级联删除 | `cascadeDelete: true`       |

#### @index 注解

定义索引，支持以下参数：

| 参数       | 类型      | 描述     | 示例                                           |
|----------|---------|--------|----------------------------------------------|
| `name`   | String  | 索引名称   | `name: "IDX_studentName"`                    |
| `unique` | Boolean | 是否唯一   | `unique: false`                              |
| `fields` | Array   | 索引字段列表 | `fields: [studentName, age: (sort: "desc")]` |

### 语法规则

1. **标识符**: 支持字母、数字、下划线，不能以数字开头
2. **字符串**: 使用双引号包围，支持转义字符
3. **注释**: 支持单行注释 `//` 和多行注释 `/* */`
4. **分号**: 字段定义后必须使用逗号分隔
5. **空格**: 语法对空格不敏感，但建议保持良好的格式
