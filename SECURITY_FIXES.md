# 安全修复说明

## 已完成的修复

### 1. ✅ 升级Fastjson版本
- **修改文件**: `pom.xml`
- **变更**: Fastjson 1.2.28 → 2.0.43
- **影响**: 修复多个已知反序列化安全漏洞
- **兼容性**: 已验证现有代码与Fastjson 2.0.43兼容

### 2. ✅ 启用Jasypt加密依赖
- **修改文件**: `pom.xml`
- **变更**: 取消注释 jasypt-spring-boot-starter 依赖
- **影响**: 支持配置文件敏感信息加密

### 3. ✅ 配置文件敏感信息使用环境变量
- **修改文件**: `application-test.yaml`
- **变更**:
  - `datasource.password: root` → `${DB_PASSWORD:root}`
  - `redis.password: root` → `${REDIS_PASSWORD:root}`
- **影响**: 避免敏感信息明文存储在配置文件中

## 使用方法

### 运行应用时设置环境变量

```bash
# 方式1：直接设置环境变量
export DB_PASSWORD=your_db_password
export REDIS_PASSWORD=your_redis_password
export JASYPT_PASS=your_encryption_key

# 方式2：使用环境变量文件
cat > .env << EOF
DB_PASSWORD=your_db_password
REDIS_PASSWORD=your_redis_password
JASYPT_PASS=your_encryption_key
EOF

source .env
```

### 生产环境加密配置

生产环境已使用Jasypt加密，设置JASYPT_PASS环境变量即可：

```bash
export JASYPT_PASS=your_encryption_key
```

## 未完成的修复

### ⏳ 升级Spring Boot版本
- **当前版本**: 2.3.6.RELEASE
- **建议版本**: 2.7.18（LTS）或 3.2.x
- **影响**: 需要大量兼容性测试
- **状态**: 暂缓，需要完整测试

### ⏳ 配置Jasypt加密
- **当前状态**: jasypt依赖已启用，但测试环境使用环境变量
- **建议**: 使用Jasypt加密所有配置文件的敏感信息
- **工具**: `JasyptEncryptor.java` 工具类可用于生成加密字符串

## 生成加密字符串

```bash
# 运行加密工具
cd houseAgentsBackend
mvn exec:java -Dexec.mainClass="com.house.agents.utils.JasyptEncryptor" \
  -Dexec.args="<明文密码> <加密密钥>"

# 示例
mvn exec:java -Dexec.mainClass="com.house.agents.utils.JasyptEncryptor" \
  -Dexec.args="root my-secret-key"
```

输出格式：`ENC(加密后的字符串)`

## 安全建议

1. **加密密钥管理**：
   - 不要将加密密钥提交到Git
   - 使用KMS或密钥管理服务存储加密密钥
   - 不同环境使用不同的加密密钥

2. **环境变量管理**：
   - 使用 `.env` 文件（添加到 `.gitignore`）
   - 使用Docker Secrets或Kubernetes Secrets
   - 生产环境使用云平台的密钥管理服务

3. **定期更新依赖**：
   - 使用 GitHub Dependabot 自动检测漏洞
   - 定期运行 `mvn dependency:tree` 检查依赖树
   - 关注安全公告

## 参考

- [Fastjson 安全公告](https://github.com/alibaba/fastjson/wiki/security_update_20170315)
- [Jasypt 文档](https://github.com/ulisesbocchio/jasypt-spring-boot)
- [Spring Boot 安全配置](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#features.external-config)
