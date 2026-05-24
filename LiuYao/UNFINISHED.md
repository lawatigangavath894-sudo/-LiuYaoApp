# 已知未完成项登记(非中断,均为历轮明确声明的范围外项)

本文件登记"功能未接但属有意为之"的点,供长期开发追踪。
经全量审计:所有阶段交付物齐全、导航全通、引用完整、20 种 RuleCondition 序列化无遗漏、
6 个纯逻辑模块均有单元测试。以下为刻意留待后续的项,不是中断或 bug。

## 1. 排盘分析区:除"断语"外的 6 个 Tab 仍为占位
- 位置:`ui/screens/chart/AnalysisTabs.kt`,以及 `ui/screens/chart/FeedbackPanel.kt`
- 现状:神煞 / 旺衰 / 批注 / 案例 / 占法 / 取象 这 6 个 Tab 用 `ChartMockData` 占位文本;
  "断语" Tab 已接真实 `RuleMatcher` 结果。
- 说明:历轮只承诺接入"断语"Tab,其余从未列入交付范围。
- 建议版本:v0.3(完整度)。

## 2. 显示偏好未接入渲染
- 位置:偏好已由 `data/prefs` 持久化;但 `ChartScreen`/`AnalysisTabs` 未消费
  `ChartDisplayPrefs`(伏神/飞神/神煞/旺衰/空亡/五行色/详细Badge)与
  `RulesDisplayPrefs`(只显示命中/未命中原因/冲突/按权重/按来源)。
- 说明:设置阶段明确声明"只做设置页,不做消费侧";偏好已存好,随时可接。
- 建议版本:v0.4(体验)。

## 3. gradle-wrapper.jar 需本地补
- 现状:纯文本工程不携带二进制 jar。`setup-gradle-wrapper.sh` 与 README 已说明三种补法
  (Android Studio 自动补 / `gradle wrapper --gradle-version 8.9` / curl 下载)。
- 说明:环境限制,非代码缺陷。`.gitignore` 不会误伤该 jar。

## 4. 引擎层精度技术债
- 详见 `app/src/main/java/com/liuyao/paipan/domain/engine/TECHNICAL_DEBT.md`(P1/P2/P3 分级)。

## 5. 数据库 destructive migration
- 现状:`AppDatabase` 用 `fallbackToDestructiveMigration`,开发期重装清库。
- 说明:上线前需替换为正式 Migration。建议版本:v0.2。
