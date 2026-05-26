package com.liuyao.paipan.domain.imports

import com.liuyao.paipan.domain.model.DivinationCategory
import com.liuyao.paipan.domain.model.SixKin
import com.liuyao.paipan.domain.rule.DivinationRule
import com.liuyao.paipan.domain.rule.RulePolarity
import com.liuyao.paipan.domain.rule.RuleTarget
import java.util.UUID

/**
 * 导入草稿规则。
 *
 * 由 [RuleImportParser] 从一段文本生成:原文必有,占类/类神等为"自动识别结果",
 * 识别不到时为 null —— UI 以"待人工确认"呈现。用户在预览页编辑后,经 [toRule] 落库。
 *
 * 字段尽量与 [DivinationRule] 对齐,但全部可空/可改,体现"半自动、可人工修正"。
 */
data class DraftRule(
    val id: String = "draft-${UUID.randomUUID().toString().take(8)}",
    val originalText: String,
    val sourceName: String = "刘昌明《象断六爻》(导入)",
    val category: DivinationCategory? = null,   // null = 待确认
    val target: RuleTarget? = null,             // null = 待确认
    val plainExplanation: String = "",
    val conditionText: String = "",
    val tagsText: String = "",
    val polarity: RulePolarity = RulePolarity.NEUTRAL,
    val priority: Int = 50,
    val confidenceWeight: Double = 0.5,
    // —— 本轮新增:导入预览/选择相关 ——
    val sourceFileName: String = "",
    val detectedEncoding: String = "",
    val selectedForImport: Boolean = true,
    val maybeGarbled: Boolean = false,          // 该条疑似乱码
) {
    /** 导入状态(供预览页展示徽标) */
    val importStatus: String
        get() = when {
            maybeGarbled -> "可能乱码"
            needsReview -> "待确认"
            else -> "可导入"
        }

    val warningMessage: String?
        get() = if (maybeGarbled) "内容疑似编码异常,请核对原文" else null

    /** 是否仍有"待人工确认"的关键字段 */
    val needsReview: Boolean
        get() = category == null || target == null || plainExplanation.isBlank()

    /** 待确认字段清单(用于 UI 提示) */
    val pendingFields: List<String>
        get() = buildList {
            if (category == null) add("占类")
            if (target == null) add("类神")
            if (plainExplanation.isBlank()) add("白话解释")
        }

    /**
     * 转为可入库的 [DivinationRule]。
     * 占类/类神为空时给安全默认(占类→OTHER,类神→用神),避免坏数据入库;
     * 但 UI 应在 [needsReview] 为真时提示用户先确认。
     */
    fun toRule(): DivinationRule = DivinationRule(
        id = id.replaceFirst("draft-", "rule-"),
        sourceId = "src-import",
        sourceName = sourceName,
        category = category ?: DivinationCategory.OTHER,
        target = target ?: RuleTarget.UseGod,
        originalText = originalText.trim(),
        plainExplanation = plainExplanation.trim(),
        conditionText = conditionText.trim(),
        positiveMeaning = null,
        negativeMeaning = null,
        matchConditions = emptyList(),   // 第一版导入不做结构化条件
        excludeConditions = emptyList(),
        polarity = polarity,
        priority = priority,
        confidenceWeight = confidenceWeight,
        tags = tagsText.split(",", "，").map { it.trim() }.filter { it.isNotEmpty() },
    )
}
