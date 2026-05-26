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

## 本轮测试结果

- 本地 `assembleDebug` 构建通过。
- 已生成 debug APK: `C:\Users\Administrator\LiuYaoAppWork\app\build\outputs\apk\debug\app-debug.apk`
- APK 大小: 14001311 bytes
- 未执行真机长时间压力测试，本轮通过代码审计和构建验证降低长时间使用崩溃风险。

## 长时间使用崩溃风险修复

- 起卦页当前时间刷新从 `LaunchedEffect(Unit) + while(true)` 改为仅在正时起卦模式下运行，并使用协程取消状态退出，避免页面切换后长期协程堆积。
- 起卦按钮、排盘页 AI 解析按钮、保存案例按钮增加重复点击保护，降低重复导航、重复提交和返回栈异常风险。
- 排盘页分析使用 `LaunchedEffect(chart?.id)`，同一排盘不会重复触发分析。
- 排盘模型的世爻/应爻读取增加兜底，异常数据不会直接 `first()` 崩溃。
- 资料检索不再一次性把 OCR 资产全文读入内存，改为按段扫描并限制命中片段数量。

## 乱码清理情况

- 清理了领域模型、起卦页、排盘页、分析锁定卡片、分析 Tab、资料检索、导入读取、备份恢复等本轮关键链路中的可见乱码。
- 重写基础枚举中文名：天干、地支、五行、六亲、六神、八卦、卦宫、旺衰、占事类别。
- 文件导入读取改为先读 bytes，再按 BOM / UTF-8 / GBK / GB2312 / Big5 / 系统编码评分识别，并清理 BOM、控制字符和多余空行。
- 备份恢复导入也复用编码识别，不再直接用默认 `readText()` 读取中文文件。

## AnalysisLock 生成逻辑

分析锁定按以下顺序生成：

1. 读取占事类别、占事问题和排盘结果。
2. `QuestionFocusResolver` 从问题中识别主变量，例如“通过 / 不通过”“录取 / 不录取”“找回 / 找不到”。
3. `LiuYaoKnowledgeSearchService` 根据占类、问题、卦名、动爻、六亲、六神、世应、空破冲合等关键词检索刘昌明 OCR 文本和断语库，最多返回 10 条相关片段。
4. `AnalysisLockResolver` 优先从资料片段中识别“用神/取用”规则。
5. 资料不足时使用内置兜底规则，并明确标记 `usedFallback=true`。
6. 根据主用神、辅助用神、世爻、应爻、动爻、伏神、飞神生成关键爻列表和分析方向。

## 各分析 Tab 锁定方式

- 神煞：只显示关键爻上的六神，并说明该爻为何进入当前分析。
- 旺衰：只分析主用神、辅助用神、世应、动爻、伏神/飞神相关爻，显示旬空、月破、日冲、合冲刑害和旺衰。
- 批注：绑定当前 AnalysisLock，提示后续编辑能力。
- 案例：只提示同类占事、相同主变量、相近用神结构的案例检索范围，不展示无关案例。
- 占法：优先展示命中的资料片段；未命中时显示资料不足。
- 取象：只展示关键爻的六亲、六神、纳甲、伏神/飞神，不展示全量取象大全。
- 断语：继续按 AnalysisLock 过滤 RuleMatcher，分为主结果、过程条件、旁参考。
- AI 解析：沿用排盘 AI 入口，后续 Prompt 会使用 AnalysisLock、关键爻、命中断语和资料片段。

## 未导入刘昌明资料时的降级

- AnalysisLock 会正常生成，但标记为基础兜底。
- 分析锁定卡片显示“未检索到足够资料，当前为基础锁定，请导入刘昌明资料或手动调整。”
- 占法、取象和断语不会假装命中资料，显示资料不足或空结果。
- AI 解析后续 Prompt 会明确写入“本地资料不足”。

## 仍未完成的 TODO

- 分析锁定编辑 BottomSheet 仍为提示态，完整可编辑逻辑后续实现。
- AI 流式输出、Keystore/EncryptedDataStore 保存 API Key、完整真机长时间压力测试后置。
- 高级资料索引、权重学习、多端同步、云端登录后置到 V2。

## 构建结果

- 结果: 成功
- APK 路径: `C:\Users\Administrator\LiuYaoAppWork\app\build\outputs\apk\debug\app-debug.apk`
- APK 大小: 14001311 bytes

## GitHub Actions

- 本轮未等待远端 Actions 完整跑完。
- 本地构建已通过；推送后以远端 Actions 日志为准继续跟进。
