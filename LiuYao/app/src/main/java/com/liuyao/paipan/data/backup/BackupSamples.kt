package com.liuyao.paipan.data.backup

import com.liuyao.paipan.data.db.entity.CaseEntity
import com.liuyao.paipan.data.db.entity.CaseFeedbackEntity
import com.liuyao.paipan.domain.rule.MockRules
import org.json.JSONObject

/**
 * 备份/恢复示例代码(供参考与联调,不参与生产)。
 * 这些示例只用纯编解码,不触 DB / Android。
 */
object BackupSamples {

    /** 断语库 JSON 往返:导出→再解析回断语 */
    fun rulesRoundTrip(): String {
        val rules = MockRules.rules
        val json = JsonSchema.rulesToJson(rules)
        // 解析回来
        val root = JSONObject(json)
        val items = JsonSchema.readItems(root, JsonSchema.TYPE_RULES)
        val restored = (0 until items.length()).map { JsonSchema.ruleFromJson(items.getJSONObject(it)) }
        return "导出 ${rules.size} 条 → 解析回 ${restored.size} 条;首条原文一致=" +
            (rules.firstOrNull()?.originalText == restored.firstOrNull()?.originalText)
    }

    /** 案例 JSON 往返示例 */
    fun caseRoundTrip(): String {
        val case = CaseEntity(
            id = "case-demo", chartId = "chart-demo", title = "示例案例",
            createdEpoch = 1_700_000_000, category = "MARRIAGE", question = "婚事能否成",
            castEpoch = 1_699_990_000, favorite = true, hitRuleIdsCsv = "rule-001,rule-002",
        )
        val fb = CaseFeedbackEntity(
            caseId = "case-demo", verdict = "SUCCESS", actualResult = "已订婚",
            note = "应期相符", feedbackEpoch = 1_700_100_000,
            hitRuleIdsCsv = "rule-001", missRuleIdsCsv = "",
        )
        val json = JsonSchema.casesToJson(listOf(case to fb))
        val root = JSONObject(json)
        val items = JsonSchema.readItems(root, JsonSchema.TYPE_CASES)
        val (rc, rfb) = JsonSchema.caseFromJson(items.getJSONObject(0))
        return "案例往返:标题一致=${rc.title == case.title},反馈结果=${rfb?.verdict}"
    }

    /** 单案例 Markdown 示例 */
    fun caseMarkdownSample(): String {
        val case = CaseEntity(
            id = "case-demo", chartId = "chart-demo", title = "求职面试",
            createdEpoch = 1_700_000_000, category = "CAREER", question = "能否通过面试",
            castEpoch = 1_699_990_000,
        )
        return MarkdownExporter.caseToMarkdown(case, null, null, emptyList(), listOf("官鬼旺相,事可成。"))
    }
}
