# FUNCTION_TEST_REPORT

## 测试日期

2026-05-26

## 测试环境

- Windows 本地构建环境
- JDK: Android Studio bundled JBR (`C:\Program Files\Android\Android Studio\jbr`)
- Android SDK: `C:\Users\Administrator\AppData\Local\Android\Sdk`
- Gradle: Wrapper
- 构建命令: `.\gradlew.bat assembleDebug --stacktrace --no-daemon`
- Gradle 缓存: `C:\Users\Administrator\LiuYaoAppGradleHome`

## 底部导航闪退原因

- 底部 Tab 点击虽然已有 `launchSingleTop / restoreState / popUpTo(saveState)`，但缺少当前页判断，快速重复点击当前 Tab 时仍可能触发重复导航。
- AI 对话页实际 destination route 可能是 `ai_chat?chartId={chartId}`，与底部 Tab route `ai_chat` 不完全一致，容易造成 selected/showBar 状态判断不同步。
- 首页快捷入口进入 AI 对话使用普通 `navigate`，可能绕开底部 Tab 的安全切换逻辑。
- 底部主页面系统返回没有统一规则，反复切换后返回栈恢复状态可能表现异常。

## 修改文件

- `app/src/main/java/com/liuyao/paipan/nav/AppNavigation.kt`

## Safe Navigation 逻辑

- 新增 `navigateToBottomTab(route)`：
  - 当前 route 归一化后已经是目标 Tab 时直接 return。
  - 使用 `launchSingleTop = true`。
  - 使用 `restoreState = true`。
  - 使用 `popUpTo(graph.findStartDestination().id) { saveState = true }`。
  - 导航异常写入 Logcat，不让 route 异常直接崩溃。
- 新增 `asBottomTabRoute()`：
  - 将 `ai_chat` 和 `ai_chat?chartId={chartId}` 统一识别为 AI Tab。
  - 首页、断语、案例、设置均使用固定主 Tab route。
- 底部 Tab 点击增加 300ms debounce。
- 当前 Tab 再次点击不执行任何 navigate。
- 首页快捷入口切换到 AI / 断语 / 案例 / 设置改用同一套 `navigateToBottomTab()`。

## 返回逻辑

- 五个底部主页面共用单一主 `NavHostController`。
- 在非首页底部 Tab 按系统返回时，统一回到首页。
- 首页按系统返回不拦截，由系统处理退出。
- 子页面仍使用 `safeBack(fallback)` 回到来源页或合理兜底页。

## 反复切换测试结果

- 代码层已覆盖：
  - 首页 <-> AI 对话重复切换不会重复入栈。
  - 首页 -> 断语 -> 案例 -> 设置 -> 首页使用同一安全 Tab 导航。
  - 快速连续点击同一 Tab 被 current-route 判断和 debounce 拦截。
  - AI Tab 的 route pattern 与底部 selected 状态已归一化。
  - 切换回 AI 对话不会因 Tab 点击重复创建 NavController。
- 本轮未连接真机手动点击 20 轮；已通过代码巡检和 debug 构建验证。

## 仍保留风险

- 真机极限快速点击压力测试尚未执行。
- AI 对话内部自动发送仍依赖 ViewModel 内 chartId 去重；跨进程重启后会重新判断。

## 构建结果

- 结果: 成功
- APK 路径: `C:\Users\Administrator\LiuYaoAppWork\app\build\outputs\apk\debug\app-debug.apk`
- APK 大小: 14317905 bytes

## GitHub Actions

- 本轮已完成本地 debug APK 构建。
- 推送后以 GitHub Actions 远端运行结果为准。
