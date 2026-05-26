# FUNCTION_TEST_REPORT

## 测试日期

2026-05-26

## 测试环境

- Windows 本地构建环境
- JDK: Android Studio bundled JBR (`C:\Program Files\Android\Android Studio\jbr`)
- Android SDK: `C:\Users\Administrator\AppData\Local\Android\Sdk`
- Gradle: Wrapper
- Gradle 缓存: `C:\Users\Administrator\LiuYaoAppGradleHome`
- 构建命令: `.\gradlew.bat assembleDebug --stacktrace --no-daemon`

## 本轮修复范围

本轮只修复“导入断语文件后预览全是乱码”的问题，没有改动断语库架构或其他功能模块。

## 修改文件

- `app/src/main/java/com/liuyao/paipan/domain/imports/ImportedTextReader.kt`
- `app/src/main/java/com/liuyao/paipan/ui/screens/imports/RuleImportViewModel.kt`
- `app/src/main/java/com/liuyao/paipan/ui/screens/imports/RuleImportScreen.kt`
- `app/src/main/java/com/liuyao/paipan/ui/screens/imports/ImportPreviewScreen.kt`
- `FUNCTION_TEST_REPORT.md`

## 编码修复结果

- 导入链路改为先读取原始 bytes，再按候选编码解码。
- 不再在断语导入链路中使用默认 `reader().readText()` 或未指定 Charset 的字符串构造。
- 支持 BOM 检测：UTF-8 BOM、UTF-16LE、UTF-16BE。
- 支持候选编码：UTF-8、UTF-8 BOM、UTF-16LE、UTF-16BE、GB18030、GBK、GB2312、Big5（系统可用时）。
- 增加乱码评分：降低 `�`、控制字符、典型乱码片段的分数，提高中文字符、中文标点、六爻术语的分数。
- 解码后清理 BOM、异常控制字符和换行格式。

## 预览页结果

- 预览页现在显示文件名、当前编码、自动识别编码、原文预览、拆分条目数量和条目列表。
- 增加“手动选择编码”，用户可在自动、UTF-8、UTF-8 BOM、UTF-16LE、UTF-16BE、GB18030、GBK、GB2312、Big5 间切换。
- 切换编码时使用同一份 raw bytes 重新解码并重新生成 DraftRule，不重新打开文件。
- 疑似乱码时显示提示：“当前预览可能仍存在乱码，请尝试切换 GB18030 / GBK / UTF-8 编码。”
- 确认导入使用当前预览页选中编码生成的 DraftRule，避免“预览正常、入库乱码”。
- 取消导入不会写入数据库。

## 已验证项

- 断语导入代码路径已确认不再使用默认编码读取文本。
- 文件选择取消分支不会触发读取或入库。
- 预览页手动编码切换会刷新原文预览和拆分条目。
- 确认导入只导入当前选中的条目。

## 仍保留的 TODO

- OCR/PDF 正文抽取不在本轮范围内。
- Big5 依赖系统 Charset 支持；不可用时不会显示该选项。
- 本轮未连接真机逐个导入样例文件验证，已通过代码审计和 debug 构建验证。

## 构建结果

- 结果: 成功
- APK 大小: 14317905 bytes
- APK 生成时间: 2026-05-26 20:29:45
- APK 路径: `C:\Users\Administrator\LiuYaoAppWork\app\build\outputs\apk\debug\app-debug.apk`

## GitHub Actions

- 本轮以本地 debug APK 构建为准；推送后以 GitHub Actions 远端运行结果为准。
