package com.liuyao.paipan.data.knowledge

import android.content.Context
import com.liuyao.paipan.domain.analysis.KnowledgeSnippet
import com.liuyao.paipan.domain.analysis.displayName
import com.liuyao.paipan.domain.model.DivinationCategory
import com.liuyao.paipan.domain.model.LiuYaoChart
import com.liuyao.paipan.domain.model.SixGod
import com.liuyao.paipan.domain.model.SixKin
import com.liuyao.paipan.domain.rule.DivinationRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class LiuYaoKnowledgeSearchService(private val context: Context) {
    suspend fun searchRelevantSnippets(
        category: DivinationCategory,
        question: String,
        chart: LiuYaoChart,
        rules: List<DivinationRule>,
        extraKeywords: List<String> = emptyList(),
        limit: Int = 10,
    ): List<KnowledgeSnippet> = withContext(Dispatchers.IO) {
        val keywords = buildKeywords(category, question, chart, extraKeywords)
        val assetHits = searchAssets(keywords, limit * 2)
        val ruleHits = searchRules(rules, keywords)
        (assetHits + ruleHits)
            .distinctBy { it.sourceName + it.originalText.take(80) }
            .sortedByDescending { it.relevanceScore }
            .take(limit)
    }

    fun buildKeywords(
        category: DivinationCategory,
        question: String,
        chart: LiuYaoChart,
        extraKeywords: List<String> = emptyList(),
    ): List<String> = buildList {
        add(category.displayName())
        addAll(categoryAliases(category))
        addAll(question.split(Regex("""[，。！？、\s]+""")).filter { it.length >= 2 })
        addAll(extraKeywords)
        addAll(SixKin.entries.map { it.displayName() })
        addAll(listOf("用神", "世爻", "应爻", "动爻", "变爻", "伏神", "飞神"))
        addAll(listOf("旬空", "空亡", "月破", "日冲", "月建", "日辰", "旺相休囚", "生克", "合", "冲", "刑", "害", "墓", "绝"))
        addAll(SixGod.entries.map { it.displayName() })
        if (chart.movingLines.isNotEmpty()) add("动爻")
        add(chart.originalHexagram.name)
        chart.changedHexagram?.name?.let { add(it) }
        addAll(listOf("成", "不成", "过", "不过", "录取", "不录取", "找到", "找不到", "吉", "凶"))
    }.map { it.trim() }.filter { it.isNotBlank() }.distinct()

    private fun searchAssets(keywords: List<String>, cap: Int): List<KnowledgeSnippet> {
        val root = "liuchangming/ocr_txt"
        val files = runCatching { context.assets.list(root)?.toList().orEmpty() }.getOrDefault(emptyList())
        val hits = mutableListOf<KnowledgeSnippet>()
        files.forEach { fileName ->
            runCatching {
                context.assets.open("$root/$fileName").bufferedReader(Charsets.UTF_8).useLines { lines ->
                    var index = 0
                    val buffer = StringBuilder()
                    fun flush() {
                        val segment = buffer.toString().trim()
                        buffer.clear()
                        if (segment.length < 20) return
                        val matched = keywords.filter { segment.contains(it) }
                        if (matched.isEmpty()) return
                        hits += KnowledgeSnippet(
                            id = stableId(fileName, index.toString(), segment),
                            sourceName = fileName.removeSuffix(".txt"),
                            sectionTitle = segment.lineSequence().firstOrNull()?.take(40),
                            originalText = segment.take(700),
                            matchedKeywords = matched.take(8),
                            relevanceScore = score(segment, matched),
                        )
                    }
                    lines.forEach { line ->
                        if (line.startsWith("===== Page ") || line.isBlank()) {
                            flush()
                            index++
                        } else {
                            buffer.appendLine(line)
                        }
                    }
                    flush()
                }
            }
            if (hits.size >= cap) return hits.sortedByDescending { it.relevanceScore }.take(cap)
        }
        return hits.sortedByDescending { it.relevanceScore }.take(cap)
    }

    private fun searchRules(rules: List<DivinationRule>, keywords: List<String>): List<KnowledgeSnippet> =
        rules.mapNotNull { rule ->
            val text = listOf(rule.originalText, rule.plainExplanation, rule.conditionText, rule.tags.joinToString(" "))
                .joinToString("\n")
            val matched = keywords.filter { text.contains(it) }
            if (matched.isEmpty()) return@mapNotNull null
            KnowledgeSnippet(
                id = "rule-${rule.id}",
                sourceName = rule.sourceName.ifBlank { "断语库" },
                sectionTitle = rule.category.displayName(),
                originalText = text.take(700),
                matchedKeywords = matched.take(8),
                relevanceScore = score(text, matched) + 5.0,
            )
        }

    private fun score(text: String, matched: List<String>): Double {
        var score = matched.sumOf { if (it.length >= 2) 2.0 else 0.8 }
        if (text.contains("用神") || text.contains("取用")) score += 3.0
        if (text.contains("世") || text.contains("应")) score += 1.0
        if (text.contains("断") || text.contains("占")) score += 1.0
        return score
    }

    private fun categoryAliases(category: DivinationCategory): List<String> = when (category) {
        DivinationCategory.MARRIAGE -> listOf("婚姻", "感情", "妻财", "官鬼", "世应")
        DivinationCategory.WEALTH -> listOf("财运", "求财", "妻财", "子孙", "兄弟")
        DivinationCategory.STUDY, DivinationCategory.FAME -> listOf("考试", "学业", "文书", "父母", "官鬼", "录取")
        DivinationCategory.CAREER -> listOf("工作", "求职", "官鬼", "父母", "入职", "offer")
        DivinationCategory.HEALTH -> listOf("疾病", "健康", "官鬼", "子孙", "白虎", "玄武")
        DivinationCategory.LOST -> listOf("失物", "寻物", "妻财", "父母", "子孙", "伏神", "找到")
        DivinationCategory.TRAVEL -> listOf("行人", "出行", "世爻", "应爻", "父母", "消息", "来")
        DivinationCategory.LAWSUIT -> listOf("官事", "诉讼", "官鬼", "父母", "证据")
        DivinationCategory.PREGNANCY -> listOf("孕产", "怀孕", "子孙", "父母", "白虎")
        DivinationCategory.HOUSE -> listOf("房宅", "父母", "妻财", "官鬼")
        DivinationCategory.COOPERATION -> listOf("合作", "世应", "妻财", "兄弟", "官鬼")
        DivinationCategory.FORTUNE -> listOf("运势", "世爻", "用神")
        DivinationCategory.OTHER -> listOf("用神", "世爻", "应爻")
    }

    private fun stableId(vararg parts: String): String {
        val bytes = MessageDigest.getInstance("SHA-1").digest(parts.joinToString("|").toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }.take(16)
    }
}
