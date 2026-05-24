#!/bin/bash
# 脚本: 提交修复并推送触发 GitHub Actions

set -e

echo "📝 检查 git 状态..."
git status

echo ""
echo "✅ 提交修复..."
git commit -m "修复: gradle wrapper JVM参数引号问题，改为debug构建

- 修复gradlew和gradlew.bat中DEFAULT_JVM_OPTS的引号配置
- 将JVM堆内存从64m增加到256m以解决构建错误
- GitHub Actions workflow改为assembleDebug构建"

echo ""
echo "🚀 推送到远程仓库..."
git push origin main

echo ""
echo "✨ 完成！GitHub Actions 已触发"
echo "📊 查看构建状态: https://github.com/lawatigangavath894-sudo/-LiuYaoApp/actions"
