[前端项目](https://github.com/vectorstone/houseAgentsFront)
[后端项目](https://github.com/vectorstone/houseAgentsBackend)

# 房源管理系统 - 后端服务

## 项目背景
房源管理系统是一个独立开发的开源项目，用于管理房源信息，支持一键导入现有房源数据，方便快速查找和管理房源。

主要功能模块：
- 权限管理模块
- 房源导入导出模块
- 房源增删改查模块
- 用户管理模块
- 微信登录集成

## 技术栈

### 核心框架
- **JDK 21** - Java开发工具包
- **Spring Boot 3.1.6** - 基础框架
- **Spring Cloud 2023.0.0** - 微服务框架
- **Spring Security 6.x** - 安全框架
- **MyBatis-Plus 3.5.7** - ORM框架

### 数据存储
- **MySQL 8.0** - 关系型数据库
- **Redis** - 缓存数据库

### 文档工具
- **SpringDoc OpenAPI 2.2.0** - API文档生成（Swagger替代品）

### 工具库
- **Lombok 1.18.32** - 简化Java代码
- **Hutool** - Java工具类库
- **EasyExcel 3.3.1** - Excel操作
- **Jasypt 3.0.5** - 配置加密
- **jjwt 0.12.3** - JWT令牌生成

### 其他
- **阿里云OSS** - 文件存储
- **Dianping CAT** - 应用监控

## 环境需求

- **JDK 21+**
- **Maven 3.6.3+**
- **MySQL 8.0+**
- **Redis**

## 项目部署

### 说明
初始状态只有admin一个管理员用户，默认密码: `123456`

### 配置文件修改
将项目clone到本地后，需修改以下配置文件：

#### 1. 修改数据库连接信息
配置文件位置: `system-service/src/main/resources/application-test.yaml` 或 `application-prod.yaml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://你的数据库地址:端口/HousingAgents?serverTimezone=Asia/Shanghai
    username: 你的用户名
    password: 你的密码
  redis:
    host: 你的Redis地址
    port: 6379
    password: 你的Redis密码
```

#### 2. 敏感信息加密
项目使用jasypt保护敏感信息（数据库密码、Redis密码、微信密钥等），推荐使用加密方式。

**加密工具使用参考**: [jasypt加密工具的使用](http://wswxgpp.eu.org/2023/09/07/springboot%E9%A1%B9%E7%9B%AE%E4%B8%AD%E9%81%BF%E5%85%8D%E6%9A%B4%E9%9C%B2%E6%95%8F%E6%84%9F%E4%BF%A1%E6%81%AF%E7%9A%84%E6%96%B9%E6%B3%95/)

#### 3. 微信登录配置
如需使用微信登录功能，需配置微信开放平台参数：

```yaml
wx:
  open:
    appid: 你的微信AppID
    secret: 你的微信密钥
    redirect_uri: http://你的域名/api/core/wx/callback
```

### 编译运行

```bash
# 进入项目目录
cd houseAgentsBackend

# 编译打包
mvn clean package -Dmaven.test.skip=true

# 运行（测试环境）
cd system-service
java -jar target/system-service.jar

# 或指定profile运行
java -jar target/system-service.jar --spring.profiles.active=test
```

### 前端项目部署
clone前端项目后，在项目根目录执行：
```bash
npm run build:prod
```
打包后的文件需上传到nginx的html目录。

### Nginx配置
需要部署nginx反向代理服务器：

```nginx
server {
    listen 80;
    server_name 服务器IP地址;

    client_max_body_size 20m;

    location / {
        root /var/www/html;
        index index.html index.htm;
    }

    location /prod-api/ {
        proxy_pass http://127.0.0.1:8888/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    error_page 500 502 503 504 /50x.html;
    location = /50x.html {
        root html;
    }
}
```

## API文档

### Swagger UI
项目集成了SpringDoc OpenAPI（Swagger的替代品），提供在线API文档。

**访问地址**: `http://localhost:8888/swagger-ui/index.html`

**OpenAPI JSON**: `http://localhost:8888/v3/api-docs`

### 注解说明
SpringDoc使用Jakarta命名空间的注解：

| Swagger 2.x | SpringDoc OpenAPI |
|------------|------------------|
| `@Api` | `@Tag` |
| `@ApiOperation` | `@Operation` |
| `@ApiModel` | `@Schema` |
| `@ApiModelProperty` | `@Schema` |
| `@ApiParam` | `@Parameter` |

示例：
```java
@Tag(name = "用户管理模块")
@RestController
public class SysUserController {

    @Operation(summary = "用户登录")
    @GetMapping("/admin/user/login")
    public R login(@Parameter(description = "用户名") String username) {
        // ...
    }
}
```

## 健康检查

系统提供健康检查接口用于监控服务状态：

**接口地址**: `GET /monitor/alive`

**响应示例**:
```json
{
  "code": 200,
  "message": "成功",
  "data": {}
}
```

该接口无需认证即可访问，适合用于负载均衡器健康检查。

## 数据库初始化
请将项目根目录下的SQL脚本导入到你的数据库中。

## 主要端点

- **登录接口**: `POST /admin/user/login`
- **房源列表**: `GET /admin/house/list`
- **房源详情**: `GET /admin/house/getHouseInfo/{id}`
- **地铁线路**: `GET /admin/house/subway`
- **API文档**: `http://localhost:8888/swagger-ui/index.html`
- **健康检查**: `GET /monitor/alive`

## 更新日志

### v1.1.0 (2026-02-01)
- ✨ 升级JDK版本到21
- ✨ 升级Spring Boot到3.1.6
- ✨ 升级Spring Cloud到2023.0.0
- ✨ 升级MyBatis-Plus到3.5.7
- ✨ 迁移Swagger 2.x到SpringDoc OpenAPI
- 🐛 修复SLF4J兼容性问题
- 🐛 修复健康检查接口权限问题
- ♻️ 迁移javax.*到jakarta.*命名空间

## 许可证
Apache License 2.0

## 联系方式
如有问题或建议，欢迎提交Issue。
