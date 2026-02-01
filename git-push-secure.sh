#!/bin/bash
# 安全的Git push脚本，使用环境变量

# 检查是否设置了GITHUB_TOKEN
if [ -z "$GITHUB_TOKEN" ]; then
    echo "❌ 错误：请先设置 GITHUB_TOKEN 环境变量"
    echo ""
    echo "设置方法："
    echo "  export GITHUB_TOKEN=your_personal_access_token"
    echo ""
    echo "或者一次性使用："
    echo "  GITHUB_TOKEN=your_token git push"
    echo ""
    echo "获取Token：https://github.com/settings/tokens"
    exit 1
fi

# 临时设置remote URL使用token
ORIGINAL_URL=$(git remote get-url origin)
TOKEN_URL=$(echo "$ORIGINAL_URL" | sed "s|https://|https://${GITHUB_TOKEN}@|")

git remote set-url origin "$TOKEN_URL"

echo "🚀 开始推送..."
git push "$@"

# 恢复原始URL（不暴露token）
git remote set-url origin "$ORIGINAL_URL"

echo "✅ 推送完成，token已清理"
