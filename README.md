# 笔记分享系统

一个功能完善的笔记分享与知识管理平台，支持笔记管理、问答社区、用户互动、内容推荐等功能。

## 📋 项目简介

笔记分享系统是一个基于 Spring Boot 和 Vue.js 构建的全栈应用，旨在为用户提供便捷的笔记管理、知识分享和社区交流平台。系统支持富文本编辑、文件上传、实时聊天、内容审核等核心功能。

## ✨ 主要功能

### 用户功能
- **笔记管理**
  - 创建、编辑、删除笔记
  - 支持 Markdown、PDF、EPUB 等多种格式
  - 笔记空间和笔记本的层级管理
  - 笔记标签分类
  - 笔记发布与分享

- **问答社区**
  - 提问与回答
  - 评论与回复
  - 点赞与收藏
  - 问题搜索

- **社交互动**
  - 用户关注/粉丝系统
  - 私信聊天（WebSocket 实时通信）
  - 评论与互动
  - 通知系统

- **内容发现**
  - 热门笔记推荐
  - 个性化推荐系统
  - 全文搜索（Elasticsearch）
  - 每日推荐

### 管理功能
- **用户管理**
  - 用户信息查看与管理
  - 在线用户监控
  - 用户角色管理

- **内容管理**
  - 笔记审核与管理
  - 评论管理
  - 问答内容管理
  - 敏感词检测与过滤

- **系统监控**
  - 数据统计（笔记数、评论数等）
  - 内容审核记录
  - 系统健康监控

## 🛠️ 技术栈

### 后端 (Login_api)
- **框架**: Spring Boot 3.5.1
- **语言**: Java 25
- **数据库**: 
  - MySQL 8.0+ (主数据库)
  - MongoDB (会话存储)
  - Redis (缓存)
- **ORM**: JPA/Hibernate, MyBatis
- **安全**: Spring Security, JWT
- **消息队列**: RabbitMQ
- **搜索引擎**: Elasticsearch 8.18.1
- **文件存储**: MinIO
- **其他**: 
  - Flyway (数据库迁移)
  - WebSocket (实时通信)
  - MapStruct (DTO 转换)
  - Apache Flink (流处理)

### 前端 (Login_ui)
- **框架**: Vue.js 3.3.4
- **路由**: Vue Router 4.2.4
- **状态管理**: Pinia 3.0.4
- **HTTP 客户端**: Axios 1.13.2
- **富文本编辑器**: Tiptap 3.11.0
- **实时通信**: SockJS, STOMP.js
- **构建工具**: Vue CLI 5.0

## 📁 项目结构

```
note-sharing-main/
├── Login_api/                    # 后端 API 服务
│   ├── src/main/java/           # Java 源代码
│   │   └── com/project/login/
│   │       ├── controller/      # 控制器层
│   │       ├── service/         # 业务逻辑层
│   │       ├── repository/     # 数据访问层
│   │       ├── model/           # 数据模型
│   │       ├── config/          # 配置类
│   │       ├── exception/       # 异常处理
│   │       └── websocket/       # WebSocket 处理
│   ├── src/main/resources/
│   │   ├── application.yml      # 应用配置
│   │   └── db/migration/       # 数据库迁移脚本
│   └── build.gradle            # Gradle 构建配置
│
├── Login_ui/                     # 前端应用
│   ├── src/
│   │   ├── api/                 # API 接口封装
│   │   ├── components/          # Vue 组件
│   │   │   ├── admin/           # 管理端组件
│   │   │   └── user/            # 用户端组件
│   │   ├── views/               # 页面视图
│   │   ├── router/              # 路由配置
│   │   ├── stores/              # Pinia 状态管理
│   │   └── utils/               # 工具函数
│   ├── public/                  # 静态资源
│   └── package.json             # 依赖配置
│
├── API_DOCUMENTATION.md         # API 接口文档
├── database_schema.md          # 数据库结构文档
└── README.md                   # 项目说明文档
```

## 🔧 环境要求

### 必需环境
- **JDK**: 25+
- **Node.js**: 14.0+
- **MySQL**: 8.0+
- **MongoDB**: 4.0+
- **Redis**: 6.0+
- **RabbitMQ**: 3.8+
- **Elasticsearch**: 8.18.1
- **MinIO**: 最新版本

### 可选环境
- **Gradle**: 7.0+ (或使用项目自带的 Gradle Wrapper)

## 🚀 快速开始

### 1. 克隆项目

```bash
git clone <repository-url>
cd note-sharing-main
```

### 2. 数据库配置

#### MySQL 数据库
```sql
CREATE DATABASE ebook_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### MongoDB 数据库
确保 MongoDB 服务运行在 `localhost:27017`，数据库名称为 `note_db`

### 3. 后端配置

#### 修改配置文件
编辑 `Login_api/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ebook_platform?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC
    username: your_username
    password: your_password
  
  redis:
    host: localhost
    port: 6379
  
  rabbitmq:
    host: localhost
    port: 5672
    username: admin
    password: admin
  
  data:
    mongodb:
      uri: mongodb://localhost:27017/note_db
    elasticsearch:
      repositories:
        enabled: true

minio:
  endpoint: http://localhost:9000
  access-key: your_access_key
  secret-key: your_secret_key
  bucket: notesharing
```

#### 启动后端服务

```bash
cd Login_api
./gradlew bootRun
# Windows 使用: gradlew.bat bootRun
```

后端服务默认运行在 `http://localhost:8080`

### 4. 前端配置

#### 安装依赖

```bash
cd Login_ui
npm install
```

#### 启动开发服务器

```bash
npm run serve
```

前端应用默认运行在 `http://localhost:8080` (或 Vue CLI 默认端口)

#### 构建生产版本

```bash
npm run build
```

## 📚 配置说明

### 后端配置项

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `spring.datasource.url` | MySQL 数据库连接地址 | - |
| `spring.datasource.username` | 数据库用户名 | - |
| `spring.datasource.password` | 数据库密码 | - |
| `jwt.secret` | JWT 密钥 | - |
| `jwt.expiration` | Token 过期时间（秒） | 3600 |
| `minio.endpoint` | MinIO 服务地址 | http://localhost:9000 |
| `minio.bucket` | MinIO 存储桶名称 | notesharing |
| `modelscope.api.key` | ModelScope API 密钥 | - |

### 敏感词配置

系统支持敏感词检测，配置文件位于：
- `Login_api/src/main/resources/keywords.txt`
- `Login_api/src/main/resources/politics.txt`
- `Login_api/src/main/resources/rudewords.txt`
- `Login_api/src/main/resources/sex.txt`

## 📖 API 文档

详细的 API 接口文档请参考 [API_DOCUMENTATION.md](./API_DOCUMENTATION.md)

主要 API 模块：
- 认证与用户管理 (11个接口)
- 管理端 (18个接口)
- 笔记管理 (10个接口)
- 评论管理 (6个接口)
- 关注关系 (6个接口)
- 问答系统 (13个接口)
- 搜索 (2个接口)
- 推荐系统 (2个接口)
- 通知管理 (4个接口)
- 私信会话 (6个接口)
- WebSocket 聊天 (1个接口)

**总计**: 约 99个 API 接口

## 🗄️ 数据库结构

详细的数据库结构文档请参考 [database_schema.md](./database_schema.md)

### 主要数据表

- **users**: 用户表
- **notes**: 笔记表
- **notebooks**: 笔记本表
- **note_spaces**: 笔记空间表
- **note_stats**: 笔记统计表
- **user_follow**: 用户关注关系表
- **user_favorite_note**: 用户收藏表
- **note_moderation**: 笔记审核表
- **conversation_relation**: 会话关系表

## 🔐 安全特性

- JWT Token 认证
- Spring Security 权限控制
- 敏感词自动检测与过滤
- 内容审核机制
- 邮箱验证码验证
- 密码加密存储
- RBAC 角色权限管理

## 🎯 核心特性

### 1. 富文本编辑
- 基于 Tiptap 的强大编辑器
- 支持 Markdown 语法
- 图片上传与处理
- 代码高亮
- 表格编辑

### 2. 实时通信
- WebSocket 双向通信
- 实时私信聊天
- 在线用户状态
- 消息推送

### 3. 智能推荐
- 基于用户行为的个性化推荐
- 热门内容推荐
- 协同过滤算法

### 4. 全文搜索
- Elasticsearch 全文检索
- 支持聚合查询
- 相关性排序

### 5. 内容审核
- 敏感词自动检测
- LLM 内容审核
- 管理员审核流程
- 审核记录追踪

## 🧪 开发指南

### 后端开发

1. **添加新的 API 接口**
   - 在 `controller` 包下创建控制器
   - 在 `service` 包下实现业务逻辑
   - 在 `repository` 包下定义数据访问

2. **数据库迁移**
   - 在 `src/main/resources/db/migration/` 下创建 SQL 脚本
   - 命名格式: `V{版本号}__{描述}.sql`
   - Flyway 会自动执行迁移

3. **DTO 转换**
   - 使用 MapStruct 进行对象转换
   - 在 `convert` 包下定义转换器

### 前端开发

1. **添加新页面**
   - 在 `src/views/` 下创建视图组件
   - 在 `src/router/index.js` 中注册路由

2. **API 调用**
   - 在 `src/api/` 下封装 API 接口
   - 使用 `src/api/request.js` 进行统一请求处理

3. **状态管理**
   - 使用 Pinia Store 管理全局状态
   - Store 文件位于 `src/stores/`

## 📝 许可证

本项目采用 MIT 许可证。

## 👥 贡献

欢迎提交 Issue 和 Pull Request！

## 📞 联系方式

如有问题或建议，请通过 Issue 反馈。

---

**项目版本**: 1.0.0  
**最后更新**: 2024年
