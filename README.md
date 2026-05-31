# 海伦云作业

面向百人规模教育机构的轻量级作业管理微信小程序，涵盖学生端、教师端和管理员（教务）端三个角色。

---

## 核心功能

| 角色 | 功能 |
|------|------|
| 学生 | 查看作业、提交作业、查看批改结果、浏览预习资料 |
| 教师 | 布置作业、批改作业、发布预习资料、查看班级作业情况 |
| 管理员 | 班级管理、人员管理、数据看板、作业统计 |

---

## 技术栈

**后端**
- Java 17 + Spring Boot 3.2.5
- MyBatis-Plus 3.5.5
- MySQL 8.0
- JWT 鉴权
- Knife4j API 文档

**前端**
- uni-app（Vue 3 + Composition API）
- Pinia 状态管理
- Vite 5 构建
- 微信小程序 / H5 双端支持

---

## 服务器配置

**阿里云 ECS 通用算力型 u1 实例**
- CPU：2 核
- 内存：4 GB
- 带宽：5 Mbps
- 系统盘：80GB ESSD Entry
- 操作系统：CentOS 7.9 或 Ubuntu 22.04（推荐）
- MySQL 版本：**8.0**（与 mysql-connector-j 8.3.0 兼容）

---

## 环境准备

| 工具 | 版本要求 |
|------|----------|
| JDK | 17+ |
| Maven | 3.6+ |
| Node.js | 18+（建议 LTS） |
| MySQL | 8.0 |

---

## 快速开始

### 1. 数据库初始化

登录 MySQL 并执行建库建表脚本：

```bash
mysql -u root -p < helenedu-backend/src/main/resources/db/schema.sql
```

### 2. 修改数据库配置

编辑 `helenedu-backend/src/main/resources/application.yml`，更新数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/helen_edu?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: 你的数据库密码
```

### 3. 启动后端

```bash
cd helenedu-backend
mvn spring-boot:run
```

启动成功后访问：
- 服务地址：`http://localhost:8080`
- API 文档：`http://localhost:8080/doc.html`

### 4. 启动前端

```bash
cd helenedu-frontend
npm install
npm run dev:h5        # H5 模式（浏览器调试）
# 或
npm run dev:mp-weixin # 微信小程序模式（需微信开发者工具）
```

H5 模式访问：`http://localhost:5173`

---

## 测试账号（H5 开发模式）

在 H5 模式下，输入以下手机号登录不同角色：

| 手机号 | 角色 |
|--------|------|
| 13800000001 | 学生 |
| 13800000002 | 教师 |

> 微信小程序模式通过 `wx.login` 自动登录，需在 `application.yml` 中配置真实的 appid 和 secret。

---

## 项目结构

```
HelenEdu/
├── helenedu-backend/          # 后端 Spring Boot 项目
│   └── src/main/java/com/helen/eduedu/
│       ├── common/            # 通用工具类（R、异常、分页等）
│       ├── config/            # 配置类（CORS、MyBatis、Web）
│       ├── security/          # JWT 鉴权（Token、拦截器、注解）
│       ├── entity/            # 数据库实体类
│       ├── mapper/            # MyBatis-Plus Mapper 接口
│       ├── service/           # 业务逻辑层
│       ├── controller/        # REST API 控制器
│       ├── dto/               # 请求 DTO
│       └── vo/                # 响应 VO
│
└── helenedu-frontend/         # 前端 uni-app 项目
    └── src/
        ├── pages/             # 页面（login/student/teacher/admin）
        ├── api/               # API 接口封装
        ├── store/             # Pinia 状态管理
        ├── utils/             # 工具类（request 封装）
        └── assets/            # 静态资源（Logo 等）
```

---

## API 接口概览

| 模块 | 端点 | 说明 |
|------|------|------|
| 认证 | `POST /api/auth/wx-login` | 微信/H5 登录 |
| 班级 | `GET/POST/PUT/DELETE /api/class` | 班级 CRUD |
| 作业 | `GET/POST/PUT/DELETE /api/homework` | 作业管理 |
| 提交 | `POST /api/homework/submit` | 学生提交作业 |
| 批改 | `POST /api/homework/review` | 教师批改作业 |
| 预习资料 | `GET/POST/DELETE /api/preview` | 预习资料管理 |
| 看板 | `GET /api/dashboard/overview` | 数据统计总览 |
| 文件 | `POST /api/file/upload` | 文件上传 |

---

## 部署

服务器部署详细步骤请参考 [DEPLOY.md](DEPLOY.md)

---

## 注意事项

- 微信小程序登录需在 `application.yml` 中配置真实的 `appid` 和 `secret`
- 生产环境需将 JWT 密钥更换为强密码
- 文件上传目录需确保服务器有写入权限
