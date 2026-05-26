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

本轮强化“排盘后分析系统”，不重写项目架构，不改起卦算法，不改变现有 iOS 风格 UI。

## AnalysisContext / AnalysisLock 生成逻辑

- 排盘后由 `ChartAnalysisViewModel` 读取当前 chart、占事类别、占事问题和断语库。
- `QuestionFocusResolver` 先识别主变量，例如通过/不通过、录取/不录取、入职、offer、怀孕、找回、安危、来不来、官司输赢、得财、康复等。
- `LiuYaoKnowledgeSearchService` 按占类、问题、六亲、世应、动变、伏飞、空亡、月破、日冲、神煞、本卦/变卦等关键词检索本地资料和断语。
- `AnalysisLockResolver` 先尝试从资料片段识别“用神/取用/为用/为主”，命中则资料优先；未命中则按占类使用基础 fallback。
- AnalysisLock 现在包含主用神、辅助用神、世应、用神爻、关键爻、动变、伏飞、空亡、月破、日冲、合冲、旺弱、相关六神/神煞、资料片段、警告和排除范围。

## 断语匹配增强逻辑

- `RuleMatcher` 不再只按占类粗匹配，改为围绕 AnalysisLock 分层评分。
- 匹配评分增加占类、主变量、主用神、关键爻、世应、动变、空破冲合刑害、旺衰、神煞、资料片段关键词等因素。
- RuleMatchResult 增加分层理由、关联依据、冲突提示、置信等级、资料片段 id 和相关爻位。
- 断语显示按主结果、过程/条件、风险提示、旁参考、资料不足分组。
- 无关断语不会进入主结果区。

## 各分析 Tab 锁定控制

- 神煞：只显示与当前占类、主用神、关键爻相关的六神/神煞，并说明关联爻位和影响方向。
- 旺衰：只分析主用神、辅助用神、世应、动变、伏飞、空亡、月破、日冲等关键爻，不再平均分析六爻。
- 批注：显示当前 AnalysisLock 摘要，后续完整编辑仍为 TODO，但按钮不会无响应。
- 案例：保留同类占事、主变量、用神结构相似匹配入口；暂无命中时显示“暂无同类案例”，不展示全量案例。
- 占法：只展示与当前占类和关键词命中的资料片段；没有资料时提示导入相关断语资料。
- 取象：只对关键爻、主用神、世应、动变、伏飞取象，不展示六神/六亲/地支大全。
- 断语：按 AnalysisLock 分层输出主结果、过程条件、风险和旁参考。
- AI：Prompt 包含 AnalysisLock、排盘明细、资料片段和分层断语；最多带 10 条资料片段，不发送整本书。
- 反馈：保留原反馈面板。

## 无资料时降级

- 未导入刘昌明资料时，AnalysisLock 会显示“未检索到刘昌明资料片段，当前使用基础锁定”。
- 用神无法从资料锁定时，使用内置基础规则 fallback，并在 UI 中标明。
- 占法、取象、案例、断语为空时显示明确空状态，不展示无关大全。
- AI Prompt 会明确写入“资料不足”，要求 AI 不得脱离资料自由发挥。

## 构建结果

- 结果: 成功
- APK 大小: 14170500 bytes
- APK 生成时间: 2026-05-26 20:49:16
- APK 路径: `C:\Users\Administrator\LiuYaoAppWork\app\build\outputs\apk\debug\app-debug.apk`

## 仍保留 TODO

- 批注完整绑定关键爻/断语的编辑器后续开放。
- 案例相似度第一版已限制为同类入口和空状态，完整数据库相似评分可继续增强。
- 神煞仍基于当前六神与占类规则做关联，后续可接入更完整的资料化神煞索引。
