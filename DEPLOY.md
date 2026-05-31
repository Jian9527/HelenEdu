# 阿里云部署指南

本文档记录将「海伦云作业」部署到阿里云 ECS 服务器的完整步骤。

---

## 服务器信息

| 项目 | 配置 |
|------|------|
| 平台 | 阿里云 ECS |
| 实例类型 | 通用算力型 u1 |
| CPU | 2 核 |
| 内存 | 4 GB |
| 带宽 | 5 Mbps（固定） |
| 系统盘 | 80GB ESSD Entry |
| 推荐系统 | Ubuntu 22.04 LTS |

---

## 一、服务器环境搭建

### 1. 连接服务器

```bash
ssh root@你的服务器公网IP
```

### 2. 系统更新

```bash
apt update && apt upgrade -y
```

### 3. 安装 JDK 17

```bash
apt install openjdk-17-jdk -y
java -version   # 验证
```

### 4. 安装 MySQL 8.0

```bash
apt install mysql-server-8.0 -y

# 启动并设置开机自启
systemctl start mysql
systemctl enable mysql

# 安全初始化（设置root密码、移除匿名用户等）
mysql_secure_installation
```

**创建项目数据库：**

```bash
mysql -u root -p
```

```sql
CREATE DATABASE helen_edu CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- 将 schema.sql 内容粘贴执行，或从本地导入
```

**从本地导入建表脚本：**

```bash
# 在服务器上用 scp 接收，或直接在 MySQL 客户端粘贴执行
mysql -u root -p helen_edu < schema.sql
```

### 5. 安装 Nginx（用于部署前端 H5）

```bash
apt install nginx -y
systemctl start nginx
systemctl enable nginx
```

### 6. 配置防火墙 / 安全组

在阿里云控制台 → 安全组规则中开放以下端口：

| 端口 | 用途 |
|------|------|
| 22 | SSH |
| 80 | Nginx（前端 H5） |
| 443 | HTTPS（如需配置证书） |
| 8080 | 后端 API（可选，建议通过 Nginx 反向代理） |
| 3306 | MySQL（**仅内网开放，不对外暴露**） |

---

## 二、部署后端

### 1. 本地打包

在开发机器上：

```bash
cd helenedu-backend
mvn clean package -DskipTests
```

生成 `target/helenedu-backend-1.0.0.jar`

### 2. 上传到服务器

```bash
scp target/helenedu-backend-1.0.0.jar root@你的服务器IP:/opt/helenedu/
```

### 3. 修改生产环境配置

在服务器上创建生产配置文件 `/opt/helenedu/application-prod.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/helen_edu?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: 你的数据库密码
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 50MB

# JWT 密钥（务必换成强密码，至少32位）
jwt:
  secret: 换成一个足够长的随机字符串至少32个字符
  expiration: 604800000

# 微信小程序配置（上线前必须填写）
wechat:
  appid: 你的小程序appid
  secret: 你的小程序secret

# 文件上传存储目录
file:
  upload-dir: /opt/helenedu/uploads
```

### 4. 启动后端

```bash
cd /opt/helenedu
java -jar helenedu-backend-1.0.0.jar --spring.config.additional-location=./application-prod.yml
```

**后台运行（推荐）：**

```bash
nohup java -jar helenedu-backend-1.0.0.jar --spring.config.additional-location=./application-prod.yml > app.log 2>&1 &
```

### 5. 设置 systemd 服务（推荐）

创建 `/etc/systemd/system/helenedu.service`：

```ini
[Unit]
Description=HelenEdu Backend
After=network.target mysql.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/helenedu
ExecStart=/usr/bin/java -jar helenedu-backend-1.0.0.jar --spring.config.additional-location=./application-prod.yml
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
systemctl daemon-reload
systemctl start helenedu
systemctl enable helenedu   # 开机自启
systemctl status helenedu   # 查看状态
```

---

## 三、部署前端（H5 模式）

### 1. 本地打包

```bash
cd helenedu-frontend
npm run build:h5
```

生成 `dist/build/h5` 目录

### 2. 上传到服务器

```bash
scp -r dist/build/h5/* root@你的服务器IP:/var/www/helenedu/
```

### 3. 配置 Nginx

编辑 `/etc/nginx/sites-available/helenedu`：

```nginx
server {
    listen 80;
    server_name 你的域名或公网IP;

    # 前端 H5
    root /var/www/helenedu;
    index index.html;

    # 前端路由（SPA）
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 后端 API 反向代理
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    # 上传文件访问
    location /uploads/ {
        alias /opt/helenedu/uploads/;
    }
}
```

```bash
# 启用配置
ln -s /etc/nginx/sites-available/helenedu /etc/nginx/sites-enabled/
nginx -t          # 验证配置
systemctl reload nginx
```

---

## 四、部署微信小程序

### 1. 本地打包

```bash
npm run build:mp-weixin
```

### 2. 上传发布

1. 打开**微信开发者工具**
2. 导入 `dist/build/mp-weixin` 目录
3. 在 `project.config.json` 中填写真实的小程序 `appid`
4. 点击**上传** → 登录微信公众平台 → **版本管理** → **提交审核** → 审核通过后**发布**

### 3. 小程序后端地址配置

在微信公众平台 → 开发管理 → 开发设置 → 服务器域名：

| 配置项 | 地址 |
|--------|------|
| request 合法域名 | `https://你的域名` |
| uploadFile 合法域名 | `https://你的域名` |
| downloadFile 合法域名 | `https://你的域名` |

> 小程序正式环境必须使用 HTTPS，需申请 SSL 证书并配置到 Nginx。

---

## 五、HTTPS 配置（可选但推荐）

```bash
# 安装 certbot
apt install certbot python3-certbot-nginx -y

# 自动申请并配置证书（需要已绑定域名）
certbot --nginx -d 你的域名
```

证书自动续期已内置，无需额外操作。

---

## 六、常用运维命令

```bash
# 后端服务
systemctl start helenedu    # 启动
systemctl stop helenedu     # 停止
systemctl restart helenedu  # 重启
systemctl status helenedu   # 状态
tail -f /opt/helenedu/app.log  # 查看日志

# Nginx
systemctl reload nginx      # 重载配置
nginx -t                    # 验证配置

# MySQL
systemctl status mysql      # 状态
mysql -u root -p helen_edu  # 登录数据库

# 磁盘 / 内存
df -h                       # 磁盘使用
free -h                     # 内存使用
```

---

## 七、注意事项

- **数据库密码**：生产环境务必修改为强密码，不要用默认空密码
- **JWT 密钥**：`jwt.secret` 换成至少 32 位的随机字符串
- **MySQL 3306 端口**：安全组中**不要对外开放**，只允许本机访问
- **SSL 证书**：微信小程序正式环境强制 HTTPS，必须配置
- **文件上传目录**：`/opt/helenedu/uploads` 需确保有写入权限
- **定期备份数据库**：建议设置 cron 每日备份
