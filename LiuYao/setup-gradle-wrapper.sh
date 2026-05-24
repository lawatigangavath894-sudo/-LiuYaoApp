#!/bin/sh
# ============================================================
#  补全 gradle-wrapper.jar
# ------------------------------------------------------------
#  本工程不含二进制 gradle-wrapper.jar(无法随源码文本一起分发)。
#  运行下面任一方式即可补上,之后就能正常 Sync / Run / Build APK。
# ============================================================

# 方式 A:已装有 Gradle(brew install gradle 或 SDKMAN)
#   gradle wrapper --gradle-version 8.9
#
# 方式 B:用 Android Studio 打开本工程
#   Studio 会自动检测并补全 wrapper jar,无需手动操作。
#
# 方式 C:直接下载官方 jar(需联网)
#   curl -L -o gradle/wrapper/gradle-wrapper.jar \
#     https://raw.githubusercontent.com/gradle/gradle/v8.9.0/gradle/wrapper/gradle-wrapper.jar

set -e
if command -v gradle >/dev/null 2>&1; then
  echo "检测到 gradle,正在生成 wrapper(8.9)..."
  gradle wrapper --gradle-version 8.9
  echo "完成:gradle/wrapper/gradle-wrapper.jar 已生成。"
else
  echo "未检测到 gradle 命令。"
  echo "请改用方式 B(Android Studio 打开工程会自动补全),"
  echo "或先安装 gradle 后重跑本脚本。"
  exit 1
fi
