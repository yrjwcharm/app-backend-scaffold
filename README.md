# App Backend Scaffold

技术栈：JDK17 + Spring Boot 4.1.0 + MyBatis-Plus + Spring Security + JWT + MySQL。

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

### 获取当前用户

```bash
curl http://localhost:8080/api/user/me \
  -H 'Authorization: Bearer 替换为登录返回token'
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
- JWT Token 30 天有效期
- Spring Security 无状态认证
- `/api/auth/**` 免登录
- `/api/user/me` 需要登录
- user / user_auth / sms_code 三张表
- MyBatis-Plus 自动填充 create_time / update_time /create_user /update_user
- 逻辑删除字段 deleted

## @Data 等价于

- @Getter
- @Setter
- @ToString
- @EqualsAndHashCode
- @RequiredArgsConstructor
  **默认@Data @EqualsAndHashCode(callSuper = false) 不比较父类字段值**