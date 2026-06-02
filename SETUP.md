# 新电脑环境搭建指南

## 一、安装基础软件

| 软件 | 版本要求 | 下载地址 |
|------|---------|---------|
| **JDK** | 17+ | https://adoptium.net |
| **Maven** | 3.6+ | https://maven.apache.org/download.cgi |
| **Node.js** | 18+ LTS | https://nodejs.org |
| **MySQL** | 8.0 | https://dev.mysql.com/downloads/installer/ |
| **Git** | 最新版 | https://git-scm.com |

安装后确保都加入系统 **PATH** 环境变量。

---

## 二、安装并配置 MySQL

1. 安装 MySQL 8.0，**数据目录不要放 C 盘**（如 `D:\MySQL\Data`）
2. **端口设为 3307**（项目配置的是 3307，不是默认的 3306）
3. 设置 root 密码为 `helenedu`
4. Table Names Case 保持默认 **Lower Case**
5. 建议将 MySQL 服务设为**手动启动**

---

## 三、初始化数据库

启动 MySQL 后，执行项目自带的建表脚本：

```bash
mysql -u root -phelenedu -P 3307 < helenedu-backend/src/main/resources/db/schema.sql
```

或打开 MySQL 客户端手动执行该 SQL 文件内容。这会自动：
- 创建 `helen_edu` 数据库
- 创建所有表（用户、班级、作业、提交等）
- 插入 3 个测试账号

---

## 四、克隆项目

```bash
git clone https://github.com/Jian9527/HelenEdu.git
cd HelenEdu
```

---

## 五、配置 Maven 路径

项目启动脚本 `start.bat` 中硬编码了 Maven 路径：

```
D:\Environment\apache-maven-3.2.5\bin\mvn.bat
```

如果新电脑上 Maven 安装位置不同，需要修改 `start.bat` 和 `stop.bat` 中的路径。

---

## 六、启动项目

### 方式一：用启动脚本（推荐）

```bash
start.bat
```

### 方式二：手动启动

**终端1 - 后端：**
```bash
cd helenedu-backend
mvn spring-boot:run
```
启动成功后访问 `http://localhost:8888`

**终端2 - 前端：**
```bash
cd helenedu-frontend
npm install
npm run dev:h5
```
启动成功后访问 `http://localhost:5173`

---

## 七、测试账号

| 手机号 | 角色 |
|--------|------|
| 13800000000 | 管理员 |
| 13800000001 | 教师 |
| 13800000002 | 学生 |

---

## 八、项目关键端口

| 服务 | 端口 |
|------|------|
| 后端 Spring Boot | 8888 |
| 前端 H5 | 5173 |
| MySQL | 3307 |

---

## 九、手机访问配置

如需手机通过 WiFi 访问 H5 版本：

1. 前端 vite 已配置 `host: 0.0.0.0`，手机和电脑同一 WiFi 下可用 `http://电脑IP:5173` 访问
2. Windows 防火墙需放行 5173 和 8888 端口
3. WiFi 网络配置文件类型需设为 **Private**（非 Public）

---

## 十、目录说明

```
HelenEdu/
├── helenedu-backend/          # 后端代码
│   └── src/main/resources/
│       ├── application.yml    # 后端配置
│       └── db/schema.sql      # 数据库初始化脚本
├── helenedu-frontend/         # 前端代码
│   └── src/pages/             # 页面文件
├── logs/                      # 日志目录（gitignore）
├── uploads/                   # 上传文件目录（运行时自动创建）
├── start.bat                  # 一键启动脚本
├── stop.bat                   # 一键停止脚本
└── SETUP.md                   # 本文档
```

---

## 十一、常见问题

### Q: 后端启动报端口被占用
```powershell
# 查看占用端口的进程
Get-NetTCPConnection -LocalPort 8888
# 或杀掉所有 Java 进程
Get-Process -Name java -ErrorAction SilentlyContinue | Stop-Process -Force
```

### Q: 前端 npm install 报错
确保 Node.js 18+ 已正确安装并加入 PATH：
```powershell
node --version   # 应显示 v18.x 或更高
npm --version    # 应显示 9.x 或更高
```

### Q: 数据库连接失败
检查 `application.yml` 中的数据库配置：
- URL: `jdbc:mysql://localhost:3307/helen_edu`
- 用户名: `root`
- 密码: `helenedu`
- 确保 MySQL 服务已启动且端口为 3307

### Q: 手机无法访问
1. 检查电脑 IP：`ipconfig`
2. 确保 WiFi 网络类型为 Private
3. 检查防火墙是否放行 5173 端口
