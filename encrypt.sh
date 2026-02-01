#!/bin/bash
# Jasypt加密工具
# 使用方法：./encrypt.sh "要加密的密码" "加密密钥"

if [ $# -lt 2 ]; then
    echo "使用方法: ./encrypt.sh <要加密的字符串> <加密密钥>"
    echo "示例: ./encrypt.sh \"root\" \"my-secret-key\""
    exit 1
fi

PLAIN_TEXT="$1"
ENCRYPTION_PASSWORD="$2"

# 使用Spring Boot Jasypt加密工具
# 需要spring-boot-starter依赖中包含的jasypt CLI
# 或者使用Maven exec插件

echo "🔐 加密: $PLAIN_TEXT"
echo "🔑 加密密钥: $ENCRYPTION_PASSWORD"

# 方法1: 使用Maven exec插件运行Jasypt
cd /home/gavin/clawd/houseAgentsBackend

# 首先尝试使用Spring Boot的加密工具
java -cp ~/.m2/repository/org/jasypt/jasypt/1.9.3/jasypt-1.9.3.jar \
  org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI \
  input="$PLAIN_TEXT" \
  password="$ENCRYPTION_PASSWORD" \
  algorithm=PBEWITHHMACSHA512ANDAES_256 \
  ivGeneratorClassName=org.jasypt.iv.RandomIvGenerator 2>/dev/null || \
echo "❌ 加密失败，请检查jasypt库是否存在"
