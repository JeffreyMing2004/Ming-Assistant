# Ming-Assistant 后端服务

Spring Boot 3 + MySQL 服务端，为 Android App 提供账号、直播状态代理、舰礼登记、歌单接口。

## 技术栈

- Java 21 + Spring Boot 3.3.5 + Maven
- MySQL 8、Spring Data JPA、Spring Security + JWT、BCrypt

## 本地运行

1. 确保 MySQL 已启动，建库（已配置为自动建表）：

```sql
CREATE DATABASE IF NOT EXISTS ming_assistant CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 构建并启动：

```bash
# Windows 建议显式指定 JDK 21（勿用 JDK 25 以上，Lombok 可能不兼容）
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot
mvn spring-boot:run
```

3. 服务默认监听 `http://localhost:8080`。

### 配置（application.yml）

| 配置项 | 环境变量 | 默认值 | 说明 |
|---|---|---|---|
| MySQL 用户名 | `MING_DB_USERNAME` | `root` | |
| MySQL 密码 | `MING_DB_PASSWORD` | `Aa123123` | 生产环境务必修改 |
| 服务端口 | `MING_SERVER_PORT` | `8080` | |
| JWT 密钥 | `MING_JWT_SECRET` | 开发用默认值 | 生产环境务必修改 |
| B站主播 UID | `app.bilibili.uid` | `342163344` | 明鸽子 |

## 接口一览

| 方法 | 路径 | 是否需登录 | 说明 |
|---|---|---|---|
| POST | `/api/auth/register` | 否 | 注册：`{bilibiliUid, username, password, confirmPassword}` |
| POST | `/api/auth/login` | 否 | 登录：`{username, password}` → 返回 JWT |
| GET | `/api/auth/me` | 是 | 当前用户信息 |
| GET | `/api/live/status` | 否 | 明鸽子 直播状态（代理 B站公开接口，60s 缓存） |
| POST | `/api/gifts` | 是 | 新增舰礼：`{nickname, bilibiliUid?, giftType}` |
| GET | `/api/gifts` | 是 | 我的舰礼列表（按时间倒序） |
| DELETE | `/api/gifts/{id}` | 是 | 删除舰礼 |
| POST | `/api/songs` | 是 | 新增歌：`{title, artist?, note?}` |
| GET | `/api/songs` | 是 | 我的歌单列表 |
| DELETE | `/api/songs/{id}` | 是 | 删除歌 |

登录后请求头携带 `Authorization: Bearer <token>`。

## 说明

- 直播状态通过 B站公开接口 `GET https://api.live.bilibili.com/room/v1/Room/getRoomInfoOld?mid={uid}` 获取，无需登录，返回 `liveStatus`(0未开播/1直播中)。
- 表结构由 JPA 自动创建/更新（`ddl-auto: update`）。