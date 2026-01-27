#!/bin/bash
# Git push helper with token
# 使用方法：./git-push-with-token.sh

echo "请输入GitHub Personal Access Token:"
read -s token

# 设置远程URL包含token（仅当前仓库）
# 格式：https://<token>@github.com/user/repo.git
git remote set-url origin "https://${token}@github.com/vectorstone/houseAgentsBackend.git"

echo "✅ Token已配置，现在可以push了"
git push
