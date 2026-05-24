package com.liuyao.paipan.domain.imports

import com.liuyao.paipan.domain.model.DivinationCategory
import com.liuyao.paipan.domain.model.SixKin
import com.liuyao.paipan.domain.rule.RuleTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleImportParserTest {

    @Test
    fun splitsByBlankLineAndNumbering() {
        val text = """
            婚姻占官鬼为用,官鬼旺相婚易成。

            1. 财运求财,妻财持世得日生,主进财。
            2. 父母发动,文书有变。
        """.trimIndent()
        val drafts = RuleImportParser.parse(text)
        assertEquals(3, drafts.size)
    }

    @Test
    fun recognizesCategoryAndTarget() {
        val drafts = RuleImportParser.parse("婚姻占,官鬼为用神,旺相则成。")
        val d = drafts.first()
        assertEquals(DivinationCategory.MARRIAGE, d.category)
        assertEquals(RuleTarget.Type.SPECIFIC_KIN, d.target?.type)
        assertEquals(SixKin.OFFICIAL, d.target?.kin)
    }

    @Test
    fun recognizesWorldResponseTargets() {
        val d1 = RuleImportParser.parse("世爻旬空,求测人心不定。").first()
        assertEquals(RuleTarget.Type.WORLD, d1.target?.type)
        val d2 = RuleImportParser.parse("应爻发动,对方有变。").first()
        assertEquals(RuleTarget.Type.RESPONSE, d2.target?.type)
    }

    @Test
    fun unrecognized_marksNeedsReview() {
        // 无占类/类神关键词 → 占类与类神均为 null,待确认
        val d = RuleImportParser.parse("此爻动而化空,其象待考。").first()
        assertNull(d.category)
        assertNull(d.target)
        assertTrue(d.needsReview)
        assertTrue(d.pendingFields.contains("占类"))
        assertTrue(d.pendingFields.contains("类神"))
    }

    @Test
    fun stripsMarkdownMarkers() {
        val drafts = RuleImportParser.parse("# 婚姻篇\n\n- **官鬼**持世,婚事由己。")
        // 标题段 + 列表段
        assertTrue(drafts.any { it.originalText.contains("婚姻篇") })
        assertTrue(drafts.any { !it.originalText.contains("**") && it.originalText.contains("官鬼") })
    }

    @Test
    fun draftToRule_safeDefaults() {
        val d = RuleImportParser.parse("此象待考。").first()
        val rule = d.toRule()
        // 未识别占类 → OTHER;未识别类神 → 用神
        assertEquals(DivinationCategory.OTHER, rule.category)
        assertEquals(RuleTarget.Type.USE_GOD, rule.target.type)
        assertTrue(rule.id.startsWith("rule-"))
    }
}
