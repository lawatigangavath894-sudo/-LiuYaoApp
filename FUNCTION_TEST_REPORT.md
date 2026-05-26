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

## AI 对话页改动

- 移除了 AI 对话页中间的“操作”分组，不再长期显示“新建对话 / 清空上下文 / 删除对话”等大按钮。
- 未配置 Provider 时只显示简洁提示卡片：“请先在设置中配置 AI 模型”，并提供“去设置”按钮。
- 配置 Base URL、API Key、模型名后，AI 对话页在返回/恢复时自动刷新 Provider 状态，配置提示会自动消失。
- 配置完成后页面保留：顶部标题栏、模型 badge、消息列表、底部输入框、发送按钮、右上角三点菜单。
- 输入为空时发送按钮禁用；未配置时输入框不写入状态，发送按钮禁用。

## 三点菜单功能

- 新建对话：直接清空当前消息并创建空白对话状态。
- 清空上下文：弹出二次确认，只清空当前消息，不删除 Provider。
- 删除对话：弹出二次确认，删除当前对话后保留空白新对话，不退出 App。
- 选择模型：当前显示“模型选择将在后续版本开放。”，不会无响应。
- AI 设置：跳转到 AI 设置页，返回后自动刷新 Provider 状态。
- 危险操作“删除对话”使用警示色。

## 排盘页 AI 解析联动

- 未配置 AI Provider 时，排盘页点击“AI 解析”会先弹出“请先配置 AI 模型”，并提供“去设置”按钮。
- 已配置 AI Provider 时，排盘页跳转到 AI 对话页并携带 chartId。
- AI 对话页收到 chartId 后生成排盘 Prompt，配置有效时自动新建对话并发送；同一 chartId 在同一次 ViewModel 生命周期内不会重复自动发送。
- Prompt 内容已清理乱码，并包含占事、分析锁定、排盘信息、资料片段、命中断语和输出要求。

## 全功能巡检结果

- 首页入口：路由可用，底部 Tab 文案已清理为“首页 / AI 对话 / 断语 / 案例 / 设置”。
- 起卦页：正时、选择时间、手动起卦可用；构建通过。
- 排盘页：排盘显示、分析锁定、AI 解析、保存案例可用；未配置 AI 时安全提示。
- AI 对话：未配置安全降级，已配置后可输入发送；网络错误显示 error 消息并提供重试按钮。
- AI 设置：Provider 保存/删除/测试逻辑保留，返回 AI 对话后状态刷新。
- 断语库、案例库、设置数据管理：本轮未做架构改动，保留上一轮已通过的路径检查。
- 文件导入：继续使用 bytes + 编码识别，避免中文文件默认编码乱码。

## 已修复问题

- AI 对话页操作按钮散落在页面中部。
- 配置完成后仍显示配置提示的问题。
- Provider 删除或返回页面后状态不刷新的风险。
- 输入为空仍可点击发送的问题。
- 排盘页未配置 AI 时直接跳转 AI 页的问题。
- 排盘 AI Prompt 乱码和部分字段未插值的问题。
- 网络失败后缺少重试入口的问题。

## 仍保留 TODO

- API Key 当前仍保存在应用私有 SharedPreferences；发布前迁移到 Keystore / EncryptedDataStore。
- 完整多 Provider 列表、模型选择器、Anthropic/Gemini 原生协议后续实现。
- AI 流式输出后续实现。
- 真机长时间压力测试后续补充。

## 构建结果

- 结果: 成功
- APK 路径: `C:\Users\Administrator\LiuYaoAppWork\app\build\outputs\apk\debug\app-debug.apk`
- APK 大小: 14317905 bytes

## GitHub Actions

- 本轮已完成本地 debug APK 构建。
- 推送后以 GitHub Actions 远端运行结果为准继续跟进。
