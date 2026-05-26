# FUNCTION_TEST_REPORT

## 测试日期

2026-05-26

## 测试环境

- Windows 本地构建环境
- JDK: Android Studio bundled JBR (`C:\Program Files\Android\Android Studio\jbr`)
- Gradle: Wrapper
- 构建命令: `.\gradlew.bat assembleDebug --stacktrace --no-daemon`
- Gradle 缓存: `C:\Users\Administrator\LiuYaoAppGradleHome`

## 各页面测试结果

- 首页: 入口路由已巡检，起卦、AI 对话、断语库、案例库、设置入口均有导航目标。最近排盘为空时显示空状态。
- 起卦页: 占事类别、占事输入、正时起卦、选择时间起卦、手动起卦、手动摆爻、时间选择面板、返回按钮均保留有效交互；数字起卦未出现在可选项中。
- 排盘页: 排盘基础信息、AI 解析入口、保存案例、反馈入口、分享/导出提示、分析锁定卡片可编译通过；无 chart 时显示空状态。
- AI 对话: 未配置 Provider 时显示配置提示；已配置后可发送 OpenAI-compatible 请求；支持新建对话、清空上下文、删除对话、复制回复、排盘 Prompt 发送。
- AI 设置: 支持 Base URL、API Key、模型名、保存配置、显示/隐藏 API Key、删除配置、真实连接测试；API Key 不写入日志。
- 断语库: 列表、搜索、分类筛选、新增、编辑、详情、删除、导入、预览确认路径已保留；导入读取使用 UTF-8 文本流程。
- 案例库: 列表、空状态、详情、保存案例、反馈面板、删除、搜索、筛选路径已保留。
- 设置页: AI 设置、数据管理、导入、导出/备份、恢复入口、清空缓存、显示偏好、默认起卦方式均有响应；默认起卦方式不包含数字起卦。

## 已修复问题列表

- 补齐 AI 设置页的 Provider 保存、删除、API Key 隐藏、连接测试逻辑。
- 补齐 AI 对话页的发送、历史保存、新建对话、清空上下文、删除对话、复制回复逻辑。
- 新增 OpenAI-compatible 网络调用，并处理 API Key、Base URL、模型名、401、403、429、5xx、网络失败等错误提示。
- 新增 Internet 权限，避免 AI 请求因权限缺失失败。
- 备份读写失败改为受控异常并回写 UI 提示。
- 保持排盘 AI 解析的 Prompt 预览和发送入口，未配置 AI 时安全提示去设置。

## 暂未实现功能列表

- API Key 当前保存于应用私有 SharedPreferences；TODO: 发布前迁移到 Keystore / EncryptedDataStore。
- AI 流式输出暂未开放；当前为非流式请求，网络层字段已预留 `stream=false`。
- 多 Provider 列表管理、复杂自定义 Header、Anthropic/Gemini 原生协议为 V2。
- 完整真机/模拟器手动点击验收未在本环境执行，当前记录为代码路径巡检 + 本地构建验证。
- 高级资料检索、权重学习、多端同步、云端登录为后续版本。

## 已知风险

- 部分源文件历史中文注释或字符串在 Windows 控制台显示为乱码，但 Kotlin 编译通过；如后续 UI 真机显示异常，需要统一转为 UTF-8 后复测。
- CalendarCalculator 使用低精度节气算法，临界日期可能存在误差，代码中已有技术债标注。
- AI 调用依赖用户自行填写可用 API Key、Base URL 和模型名。

## 本地构建结果

- 结果: 成功
- APK 路径: `C:\Users\Administrator\LiuYaoAppRepo\app\build\outputs\apk\debug\app-debug.apk`
- APK 大小: 21291886 bytes

## GitHub Actions 结果

- Workflow 文件已检查为 debug APK 构建并上传 artifact。
- 本次提交推送后需要等待 GitHub Actions 运行完成；若远端失败，以 Actions 日志为准继续修复。
