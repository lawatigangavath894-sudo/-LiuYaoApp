package com.liuyao.paipan.data.db

import com.liuyao.paipan.data.db.entity.CaseEntity
import com.liuyao.paipan.data.db.entity.CaseFeedbackEntity
import com.liuyao.paipan.data.db.entity.RuleSourceEntity
import com.liuyao.paipan.domain.model.DivinationCategory
import com.liuyao.paipan.domain.model.mockChart
import com.liuyao.paipan.domain.rule.MockRules
import java.time.Instant

/**
 * 示例插入/查询数据(供联调与手动验证;非生产逻辑)。
 *
 * 演示:规则、排盘、案例反馈三类数据的持久化与读取。
 * 这些方法应在协程作用域中调用(DAO 为 suspend)。
 */
object DbSamples {

    private fun now() = Instant.now().epochSecond

    /** 插入示例规则(含来源)。 */
    suspend fun seedRules(repo: LiuYaoRepository) {
        val src = MockRules.source
        val sourceEntity = RuleSourceEntity(
            id = src.id, author = src.author, book = src.book, locator = src.locator,
        )
        MockRules.rules.forEachIndexed { i, rule ->
            // 仅第一条带 source 实体,避免重复(REPLACE 也安全)
            repo.saveRule(rule, source = if (i == 0) sourceEntity else null)
            repo.ensureStats(rule.id, now())
        }
    }

    /** 保存一个排盘(用领域层 mockChart)。 */
    suspend fun seedChart(repo: LiuYaoRepository): String {
        val chart = mockChart()
        repo.saveChart(chart)
        return chart.id
    }

    /** 建案例 + 写反馈,并累加命中统计。 */
    suspend fun seedCaseAndFeedback(repo: LiuYaoRepository, chartId: String) {
        val caseId = "case-001"
        repo.saveCase(
            CaseEntity(
                id = caseId,
                chartId = chartId,
                title = "文章投此期刊能否录用",
                createdEpoch = now(),
            ),
        )
        repo.saveFeedback(
            CaseFeedbackEntity(
                caseId = caseId,
                verdict = "部分",
                actualResult = "返修后录用",
                note = "出空日附近收到通知,与断语应期相符。",
                feedbackEpoch = now(),
                hitRuleIdsCsv = "rule-005",
                missRuleIdsCsv = "rule-003",
            ),
        )
        // 命中/误判计数累加(供权重修正)
        repo.bumpStats("rule-005", dMatched = 1, dHit = 1, epoch = now())
        repo.bumpStats("rule-003", dMatched = 1, dMiss = 1, epoch = now())
    }

    /** 一键播种全部示例。 */
    suspend fun seedAll(repo: LiuYaoRepository) {
        seedRules(repo)
        val chartId = seedChart(repo)
        seedCaseAndFeedback(repo, chartId)
    }

    /** 示例查询:返回一段可读的核对文本。 */
    suspend fun sampleQueries(repo: LiuYaoRepository): String = buildString {
        val fameRules = repo.rulesByCategory(DivinationCategory.FAME)
        appendLine("求名类规则数: ${fameRules.size}")
        fameRules.forEach { appendLine("  - ${it.id} 优先级${it.priority} ${it.originalText}") }

        val charts = repo.allCharts()
        appendLine("已存排盘数: ${charts.size}")
        charts.firstOrNull()?.let { c ->
            val lines = repo.loadChartLines(c.id)
            appendLine("排盘 ${c.id}:${c.originalHexName} → ${c.changedLower?.let { "有变卦" } ?: "静卦"},爻数 ${lines.size}")
        }

        val cases = repo.allCases()
        appendLine("案例数: ${cases.size}")
        cases.firstOrNull()?.let { k ->
            val fb = repo.feedbackOf(k.id)
            appendLine("案例 ${k.id} 反馈:${fb?.verdict} 验中[${fb?.hitRuleIdsCsv}] 误判[${fb?.missRuleIdsCsv}]")
        }

        val stats = repo.allStats()
        appendLine("规则统计行数: ${stats.size}")
        stats.forEach { appendLine("  - ${it.ruleId}: 命中${it.matchedCount} 验中${it.hitCount} 误判${it.missCount}") }
    }
}
