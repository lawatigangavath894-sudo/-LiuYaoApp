# 六爻排盘 App

Kotlin + Jetpack Compose + Material 3 + MVVM。iOS 风视觉(Large Title / Grouped List
圆角白卡 / 浅灰背景 / 胶囊 Segmented / 轻量 Badge)。

## 当前进度

- iOS 风设计系统(ui/theme、ui/components)
- 领域模型(domain/model)+ 卦法算法 + 排盘引擎(domain/engine)
- **引擎已接入 UI**:Home →(起卦)→ Cast(手动摆爻 + 正时)→ 引擎排盘 → Chart 渲染真实盘面
- 六爻盘 / 时间卡 / 卦象卡 显示引擎实算结果;分析区(神煞/旺衰/断语等)仍为占位 mock(后续阶段)

## 运行(重要:先补 wrapper jar)

本工程不含二进制 `gradle/wrapper/gradle-wrapper.jar`(纯文本工程无法携带二进制)。
三选一补上,然后即可 Sync / Run / Build APK:

1. **用 Android Studio 打开本目录** → 它会自动补全 wrapper jar(最省事,推荐)。
2. 已装 gradle:在工程根目录运行 `gradle wrapper --gradle-version 8.9`
   或执行 `sh setup-gradle-wrapper.sh`。
3. 未安装 gradle 但可联网:执行 `sh setup-gradle-wrapper.sh`，脚本会自动下载官方 jar。

补好后:
- Android Studio:选 `app` → Run ▶(API 26+ 模拟器/真机)。
- 命令行 Debug APK:`./gradlew :app:assembleDebug`
  产物在 `app/build/outputs/apk/debug/app-debug.apk`。
- 需 JDK 17(Studio 内 Gradle JDK 设为 17)。

## 操作链路

首页「起卦」→ 手动设置六爻阴阳(点爻象切阴阳)与动爻(点「动」)→「起此卦」
→ 以当前时间为起卦时刻,引擎排盘 → 自动进入排盘页,显示四柱/旬空/本卦变卦/
卦宫/世应/纳甲/六亲/六神/伏神/旺衰/空亡等。

## 关于 java.time(minSdk 24)

引擎用到 `java.time.LocalDateTime`。本工程 minSdk 24,已在 app/build.gradle.kts
开启 core library desugaring 以兼容。若 Sync 后仍报 java.time 相关错误,确认
desugaring 依赖已下载。

## 单元测试

纯 JVM,不依赖设备:
`./gradlew :app:testDebugUnitTest`
覆盖卦法(HexagramCalcTest)与引擎(LiuYaoChartEngineTest)。

## 下一阶段

引擎产物接入断语系统 / Room 持久化 / 案例反馈闭环。
