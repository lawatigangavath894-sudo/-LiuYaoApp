package com.liuyao.paipan.data.db

import com.liuyao.paipan.data.db.entity.CaseEntity
import com.liuyao.paipan.data.db.entity.CaseFeedbackEntity
import com.liuyao.paipan.data.db.entity.ChartEntity
import com.liuyao.paipan.data.db.entity.ChartLineEntity
import com.liuyao.paipan.data.db.entity.RuleConditionEntity
import com.liuyao.paipan.data.db.entity.RuleEntity
import com.liuyao.paipan.data.db.entity.RuleExcludeConditionEntity
import com.liuyao.paipan.data.db.entity.RuleSourceEntity
import com.liuyao.paipan.data.db.entity.RuleStatsEntity
import com.liuyao.paipan.data.db.entity.RuleTagEntity
import com.liuyao.paipan.domain.model.DivinationCategory
import com.liuyao.paipan.domain.model.LiuYaoChart
import com.liuyao.paipan.domain.rule.DivinationRule
import com.liuyao.paipan.domain.rule.RulePolarity
import com.liuyao.paipan.domain.rule.RuleTarget
import org.json.JSONObject
import java.time.ZoneOffset

/**
 * 仓库层:领域对象 ↔ Room 实体 的映射与持久化。
 *
 * 本轮目标:规则、排盘、案例都能持久化。匹配算法、UI 不在此实现。
 * 映射尽量无损;复杂结构走 [Converters] 的 JSON 编码。
 */
class LiuYaoRepository(private val db: AppDatabase) {

    private val ruleDao = db.ruleDao()
    private val chartDao = db.chartDao()
    private val caseDao = db.caseDao()
    private val statsDao = db.statsDao()

    // ──────────────── 规则 ────────────────

    suspend fun saveRule(rule: DivinationRule, source: RuleSourceEntity? = null) {
        source?.let { ruleDao.upsertSource(it) }
        ruleDao.upsertRule(rule.toEntity())
        // 编辑场景:先清旧的条件/标签,避免重复累积
        ruleDao.deleteConditions(rule.id)
        ruleDao.deleteExcludeConditions(rule.id)
        ruleDao.deleteTags(rule.id)
        ruleDao.insertConditions(
            rule.matchConditions.mapIndexed { i, c ->
                RuleConditionEntity(ruleId = rule.id, orderIndex = i, payloadJson = Converters.encodeCondition(c).toString())
            },
        )
        ruleDao.insertExcludeConditions(
            rule.excludeConditions.mapIndexed { i, c ->
                RuleExcludeConditionEntity(ruleId = rule.id, orderIndex = i, payloadJson = Converters.encodeCondition(c).toString())
            },
        )
        ruleDao.insertTags(rule.tags.map { RuleTagEntity(ruleId = rule.id, tag = it) })
    }

    suspend fun deleteRule(id: String) {
        // 子表设了外键 CASCADE,删主表即可;为稳妥同时显式清理
        ruleDao.deleteConditions(id)
        ruleDao.deleteExcludeConditions(id)
        ruleDao.deleteTags(id)
        ruleDao.deleteRule(id)
    }

    /** 读取全部规则(领域对象),用于列表。 */
    suspend fun loadAllRules(): List<DivinationRule> =
        ruleDao.allRules().map { e ->
            val match = ruleDao.conditionsOf(e.id).sortedBy { it.orderIndex }
                .mapNotNull { Converters().jsonToConditions("[${it.payloadJson}]").firstOrNull() }
            val exclude = ruleDao.excludeConditionsOf(e.id).sortedBy { it.orderIndex }
                .mapNotNull { Converters().jsonToConditions("[${it.payloadJson}]").firstOrNull() }
            e.toDomain(match, exclude)
        }

    suspend fun rulesByCategory(category: DivinationCategory): List<RuleEntity> =
        ruleDao.rulesByCategory(category.name)

    suspend fun loadRule(id: String): DivinationRule? {
        val e = ruleDao.ruleById(id) ?: return null
        val match = ruleDao.conditionsOf(id)
            .sortedBy { it.orderIndex }
            .map { Converters().jsonToConditions("[${it.payloadJson}]").first() }
        val exclude = ruleDao.excludeConditionsOf(id)
            .sortedBy { it.orderIndex }
            .map { Converters().jsonToConditions("[${it.payloadJson}]").first() }
        return e.toDomain(match, exclude)
    }

    // ──────────────── 排盘 ────────────────

    suspend fun saveChart(chart: LiuYaoChart) {
        chartDao.saveChart(chart.toEntity(), chart.toLineEntities())
    }

    suspend fun loadChartEntity(id: String): ChartEntity? = chartDao.chartById(id)
    suspend fun loadChartLines(id: String): List<ChartLineEntity> = chartDao.linesOf(id)
    suspend fun allCharts(): List<ChartEntity> = chartDao.allCharts()

    // ──────────────── 案例 + 反馈 ────────────────

    suspend fun saveCase(case: CaseEntity) = caseDao.upsertCase(case)
    suspend fun saveFeedback(feedback: CaseFeedbackEntity) = caseDao.upsertFeedback(feedback)
    suspend fun allCases(): List<CaseEntity> = caseDao.allCases()
    suspend fun caseById(id: String): CaseEntity? = caseDao.caseById(id)
    suspend fun feedbackOf(caseId: String): CaseFeedbackEntity? = caseDao.feedbackOf(caseId)
    suspend fun casesByCategory(category: String): List<CaseEntity> = caseDao.casesByCategory(category)
    suspend fun searchCases(q: String): List<CaseEntity> = caseDao.searchCases(q)
    suspend fun setCaseFavorite(id: String, fav: Boolean) = caseDao.setFavorite(id, fav)
    suspend fun deleteCase(id: String) = caseDao.deleteCase(id)

    // ──────────────── 统计(权重修正基础) ────────────────

    suspend fun ensureStats(ruleId: String, epoch: Long) {
        if (statsDao.statsOf(ruleId) == null) {
            statsDao.upsert(RuleStatsEntity(ruleId = ruleId, matchedCount = 0, hitCount = 0, missCount = 0, lastUpdatedEpoch = epoch))
        }
    }

    suspend fun bumpStats(ruleId: String, dMatched: Int = 0, dHit: Int = 0, dMiss: Int = 0, epoch: Long) {
        ensureStats(ruleId, epoch)
        statsDao.bump(ruleId, dMatched, dHit, dMiss, epoch)
    }

    /** 标记规则被使用一次(匹配命中时调用) */
    suspend fun markRuleUsed(ruleId: String, epoch: Long) {
        ensureStats(ruleId, epoch)
        statsDao.markUsed(ruleId, epoch)
    }

    /** 记录一次反馈结果(按 CaseVerdict 累加对应计数) */
    suspend fun recordRuleFeedback(
        ruleId: String,
        verdict: com.liuyao.paipan.domain.model.CaseVerdict,
        epoch: Long,
    ) {
        ensureStats(ruleId, epoch)
        var h = 0; var m = 0; var p = 0; var u = 0
        when (verdict) {
            com.liuyao.paipan.domain.model.CaseVerdict.SUCCESS -> h = 1
            com.liuyao.paipan.domain.model.CaseVerdict.FAILURE -> m = 1
            com.liuyao.paipan.domain.model.CaseVerdict.PARTIAL -> p = 1
            com.liuyao.paipan.domain.model.CaseVerdict.UNKNOWN -> u = 1
        }
        statsDao.recordFeedback(ruleId, h, m, p, u, verdict.name, epoch)
    }

    suspend fun statsOf(ruleId: String): RuleStatsEntity? = statsDao.statsOf(ruleId)

    /** 批量取所有 stats,做成 ruleId → 排序乘子 的 map(供 RuleMatcher 注入) */
    suspend fun reliabilityMap(): Map<String, Double> =
        statsDao.allStats().associate {
            it.ruleId to com.liuyao.paipan.domain.match.RuleReliabilityCalculator.sortMultiplier(it)
        }

    suspend fun allStats() = statsDao.allStats()

    // ════════════════ 映射 ════════════════

    private fun DivinationRule.toEntity() = RuleEntity(
        id = id,
        sourceId = sourceId,
        sourceName = sourceName,
        category = category.name,
        targetType = target.type.name,
        targetKin = target.kin?.name,
        targetPosition = target.position,
        originalText = originalText,
        plainExplanation = plainExplanation,
        conditionText = conditionText,
        positiveMeaning = positiveMeaning,
        negativeMeaning = negativeMeaning,
        polarity = polarity.name,
        priority = priority,
        confidenceWeight = confidenceWeight,
        tagsCsv = tags.joinToString(","),
        explPlain = explanation?.plain,
        explMechanism = explanation?.mechanism,
        explExample = explanation?.example,
        explCaveat = explanation?.caveat,
        hintRequireAll = matchHint?.requireAllMatch,
        hintMinScore = matchHint?.minScoreToShow,
        hintApplyTiming = matchHint?.applyTimingNote,
    )

    private fun RuleEntity.toDomain(
        match: List<com.liuyao.paipan.domain.rule.RuleCondition>,
        exclude: List<com.liuyao.paipan.domain.rule.RuleCondition>,
    ) = DivinationRule(
        id = id,
        sourceId = sourceId ?: "",
        sourceName = sourceName,
        category = DivinationCategory.fromName(category),
        target = RuleTarget(
            type = runCatching { RuleTarget.Type.valueOf(targetType) }.getOrDefault(RuleTarget.Type.USE_GOD),
            kin = targetKin?.let { k -> runCatching { com.liuyao.paipan.domain.model.SixKin.valueOf(k) }.getOrNull() },
            position = targetPosition,
        ),
        originalText = originalText,
        plainExplanation = plainExplanation,
        conditionText = conditionText,
        positiveMeaning = positiveMeaning,
        negativeMeaning = negativeMeaning,
        matchConditions = match,
        excludeConditions = exclude,
        polarity = runCatching { RulePolarity.valueOf(polarity) }.getOrDefault(RulePolarity.NEUTRAL),
        priority = priority,
        confidenceWeight = confidenceWeight,
        tags = if (tagsCsv.isEmpty()) emptyList() else tagsCsv.split(","),
    )

    private fun LiuYaoChart.toEntity() = ChartEntity(
        id = id,
        question = question,
        category = category?.name,
        dateTimeEpoch = dateTime.toEpochSecond(ZoneOffset.UTC),
        yearGanZhi = yearGanZhi.cn,
        monthGanZhi = monthGanZhi.cn,
        dayGanZhi = dayGanZhi.cn,
        hourGanZhi = hourGanZhi.cn,
        xunKong = xunKong.joinToString("") { it.cn },
        originalHexName = originalHexagram.name,
        originalLower = originalHexagram.lowerTrigram.name,
        originalUpper = originalHexagram.upperTrigram.name,
        changedLower = changedHexagram?.lowerTrigram?.name,
        changedUpper = changedHexagram?.upperTrigram?.name,
        palace = palace.name,
        isSixClash = isSixClash,
        isSixCombine = isSixCombine,
        worldLineIndex = worldLineIndex,
        responseLineIndex = responseLineIndex,
        methodCn = method.cn,
        notesCsv = notes.joinToString("\u001F"),
    )

    private fun LiuYaoChart.toLineEntities(): List<ChartLineEntity> = lines.map { l ->
        ChartLineEntity(
            chartId = id,
            position = l.index,
            yinYang = l.yinYang.name,
            isMoving = l.isMoving,
            sixGod = l.sixGod.name,
            sixKin = l.sixKin.name,
            naJiaStem = l.naJia.stem.name,
            naJiaBranch = l.naJia.branch.name,
            element = l.element.name,
            isWorld = l.isWorld,
            isResponse = l.isResponse,
            changedJson = l.changedLine?.let {
                JSONObject()
                    .put("kin", it.sixKin.name)
                    .put("stem", it.naJia.stem.name)
                    .put("branch", it.naJia.branch.name)
                    .put("yinYang", it.yinYang.name)
                    .toString()
            },
            hiddenJson = l.hiddenSpirit?.let {
                JSONObject().put("kin", it.sixKin.name)
                    .put("stem", it.naJia.stem.name).put("branch", it.naJia.branch.name).toString()
            },
            flyingJson = l.flyingSpirit?.let {
                JSONObject().put("kin", it.sixKin.name)
                    .put("stem", it.naJia.stem.name).put("branch", it.naJia.branch.name).toString()
            },
            statusJson = JSONObject()
                .put("void", l.status.isVoid)
                .put("monthBroken", l.status.isMonthBroken)
                .put("dayClashed", l.status.isDayClashed)
                .put("combined", l.status.isCombined)
                .put("clashed", l.status.isClashed)
                .put("punished", l.status.isPunished)
                .put("harmed", l.status.isHarmed)
                .put("supMonth", l.status.isSupportedByMonth)
                .put("supDay", l.status.isSupportedByDay)
                .put("strength", l.status.strength.name)
                .toString(),
        )
    }
}
