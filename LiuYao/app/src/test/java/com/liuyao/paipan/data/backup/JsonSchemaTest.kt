package com.liuyao.paipan.data.backup

import com.liuyao.paipan.data.db.entity.CaseEntity
import com.liuyao.paipan.data.db.entity.CaseFeedbackEntity
import com.liuyao.paipan.domain.rule.MockRules
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonSchemaTest {

    @Test
    fun rulesRoundTrip_preservesCore() {
        val rules = MockRules.rules
        val json = JsonSchema.rulesToJson(rules)
        val root = JSONObject(json)
        val items = JsonSchema.readItems(root, JsonSchema.TYPE_RULES)
        val restored = (0 until items.length()).map { JsonSchema.ruleFromJson(items.getJSONObject(it)) }
        assertEquals(rules.size, restored.size)
        assertEquals(rules.first().originalText, restored.first().originalText)
        assertEquals(rules.first().category, restored.first().category)
        // 结构化条件数量保持
        assertEquals(rules.first().matchConditions.size, restored.first().matchConditions.size)
    }

    @Test
    fun caseRoundTrip_withFeedback() {
        val c = CaseEntity(
            id = "c1", chartId = "ch1", title = "T", createdEpoch = 100,
            category = "MARRIAGE", question = "Q", castEpoch = 90, favorite = true,
            hitRuleIdsCsv = "r1,r2",
        )
        val fb = CaseFeedbackEntity(
            caseId = "c1", verdict = "SUCCESS", actualResult = "成", note = "n",
            feedbackEpoch = 200, hitRuleIdsCsv = "r1", missRuleIdsCsv = "",
        )
        val json = JsonSchema.casesToJson(listOf(c to fb))
        val items = JsonSchema.readItems(JSONObject(json), JsonSchema.TYPE_CASES)
        val (rc, rfb) = JsonSchema.caseFromJson(items.getJSONObject(0))
        assertEquals("T", rc.title)
        assertTrue(rc.favorite)
        assertEquals("SUCCESS", rfb?.verdict)
    }

    @Test
    fun wrongType_throws() {
        val json = JsonSchema.rulesToJson(MockRules.rules)
        try {
            JsonSchema.readItems(JSONObject(json), JsonSchema.TYPE_CASES)
            assertTrue("应抛类型不符异常", false)
        } catch (e: IllegalArgumentException) {
            assertTrue(true)
        }
    }

    @Test
    fun markdown_containsKeyFields() {
        val c = CaseEntity(id = "c", chartId = "ch", title = "求职", createdEpoch = 100, category = "CAREER", question = "面试")
        val md = MarkdownExporter.caseToMarkdown(c, null, null, emptyList(), listOf("官鬼旺相"))
        assertTrue(md.contains("# 求职"))
        assertTrue(md.contains("官鬼旺相"))
    }
}
