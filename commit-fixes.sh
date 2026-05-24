#!/bin/bash
cd "$(dirname "$0")"
git add -A
git commit -m "修复: gradle wrapper JVM参数引号问题，改为debug构建

- 修复gradlew和gradlew.bat中DEFAULT_JVM_OPTS的引号配置
- 将JVM堆内存从64m增加到256m以解决构建错误  
- GitHub Actions workflow改为assembleDebug构建"
git push
