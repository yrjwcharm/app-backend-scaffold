# App Backend Scaffold

技术栈：JDK17 + Spring Boot 4.1.0 + MyBatis-Plus + Spring Security + JWT + MySQL + Redis。

## 1. 初始化数据库

```bash
mysql -u root -p < sql/schema.sql
```

## 2. 修改数据库配置

编辑：`src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/app_backend?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
#    username: root
#    password: root
```

同时确保 Redis 已启动。数据库、Redis、JWT 和验证码摘要密钥均支持通过环境变量配置，详见 `application.yml`。

## 3. 启动项目

```bash
mvn spring-boot:run
```

## 4. 接口测试

### 注册

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"test","password":"*******","nickname":"测试用户","phone":"13800000000"}'
```

### 登录

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"test","password":"********"}'
```

### 发送手机号登录验证码

```bash
curl -X POST http://localhost:8080/api/auth/phone/code \
  -H 'Content-Type: application/json' \
  -d '{"phone":"13800000000"}'
```

当前 `TodoSmsSender` 是短信供应商的占位实现，验证码会打印在服务端日志中；生产环境接入正式短信供应商后，将
`SMS_CODE_LOG_ENABLED` 设为 `false`。

### 手机号验证码登录

```bash
curl -X POST http://localhost:8080/api/auth/phone/login \
  -H 'Content-Type: application/json' \
  -d '{"phone":"13800000000","code":"服务端日志中的6位验证码"}'
```

手机号首次通过验证码登录时会自动创建用户，后续返回与账号密码登录相同的双 Token。验证码 5 分钟有效且仅可成功使用一次。

登录响应会返回 `accessToken` 和 `refreshToken`，两者默认均为 30 天有效期。业务接口只使用 `accessToken`。

### 刷新 Token

```bash
curl -X POST http://localhost:8080/api/auth/token/refresh \
  -H 'Content-Type: application/json' \
  -d '{"refreshToken":"替换为登录或上次刷新返回的refreshToken"}'
```

刷新成功后会返回新的 `accessToken` 和 `refreshToken`，旧 `refreshToken` 会立即失效。

### 获取当前用户

```bash
curl http://localhost:8080/api/user/me \
  -H 'Authorization: Bearer 替换为登录或刷新返回的accessToken'
```

## 目录说明

```text
common      通用响应体
config      Spring Security / MyBatisPlus 配置
controller  接口层
service     业务层
mapper      MyBatis-Plus Mapper
entity      数据库实体
dto         请求参数
vo          响应对象
security    JWT 生成、解析、过滤器
properties     全局异常处理
utils     工具类
```

## 当前已实现

- 账号密码注册
- 账号密码登录
- JWT 双 Token 认证：`accessToken` / `refreshToken` 均为 30 天有效期
- refreshToken 存储在 Redis，刷新时会轮换并使旧 refreshToken 失效
- Spring Security 无状态认证
- `/api/auth/**` 免登录
- `/api/user/me` 需要登录
- user / user_auth 两张用户相关表；短信验证码只保存在 Redis，不落 MySQL
- 手机号验证码登录（首次登录自动注册）
- 验证码摘要存储、原子校验和一次性消费
- 手机号发送间隔/每日限额、IP 小时限额、错误次数限制
- MyBatis-Plus 自动填充 create_time / update_time /create_user /update_user
- 逻辑删除字段 deleted

## @Data 等价于

- @Getter
- @Setter
- @ToString
- @EqualsAndHashCode
- @RequiredArgsConstructor
  **默认@Data @EqualsAndHashCode(callSuper = false) 不比较父类字段值**

## @RequiredArgsConstructor 等价于

- Autowired

**身份信息只相信服务端自己解析出来的，不相信客户端传过来的。** JWT 认证不需要给用户返回userId

## AI 提示词

- 帮我搭建一个主要用于客户端App开发的Java企业级后端框架，要求技术栈基于SpringBoot4.1.0 +MyBatisPlus +JWT
  +SpringSecurity等技术，Token有效期为30天
  
- Synchronized 和ReentrantLock 的区别是什么？ 请简单阐述方便理解用于面试回答

  
