package com.liuyao.paipan.domain.match

import com.liuyao.paipan.domain.analysis.MatchLayer
import com.liuyao.paipan.domain.model.SixKin
import com.liuyao.paipan.domain.rule.DivinationRule
import com.liuyao.paipan.domain.rule.RuleCondition
import com.liuyao.paipan.domain.rule.RulePolarity

/**
 * 一条规则对当前卦的匹配结果。
 *
 * 字段对应需求清单:
 *  - [rule] 规则本身
 *  - [matched] 是否命中(占类+类神通过、且 match 条件满足、未被 exclude 命中)
 *  - [score] 匹配分(0..100),条件越具体越高
 *  - [polarity] 该规则的吉凶极性(决定归入"支持成/不成/中性")
 *  - [matchedConditions] 命中的 match 条件
 *  - [failedConditions] 未命中的 match 条件
 *  - [excludedByConditions] 命中的 exclude 条件(命中即导致整体被排除)
 *  - [explanation] 人类可读的命中原因
 *  - [relatedLineIndexes] 牵涉到的爻位(1..6),便于 UI 高亮
 */
data class RuleMatchResult(
    val rule: DivinationRule,
    val matched: Boolean,
    val score: Int,
    val polarity: RulePolarity,
    val matchedConditions: List<RuleCondition>,
    val failedConditions: List<RuleCondition>,
    val excludedByConditions: List<RuleCondition>,
    val explanation: String,
    val relatedLineIndexes: List<Int>,
    val matchLayer: MatchLayer = MatchLayer.SIDE_REFERENCE,
    val lockReason: String = "",
    val sourceSnippetIds: List<String> = emptyList(),
    val sourceOriginalText: String? = null,
    val relatedUsefulGod: SixKin? = null,
    val relatedLineIndex: Int? = null,
    val excludeReason: String? = null,
) {
    /** 归类:支持成 / 支持不成 / 中性提示 */
    val bucket: ResultBucket
        get() = when (polarity) {
            RulePolarity.AUSPICIOUS -> ResultBucket.SUPPORT_YES
            RulePolarity.INAUSPICIOUS -> ResultBucket.SUPPORT_NO
            RulePolarity.MIXED, RulePolarity.NEUTRAL -> ResultBucket.NEUTRAL
        }
}

/** 结果三分类 */
enum class ResultBucket(val cn: String) {
    SUPPORT_YES("支持成"),
    SUPPORT_NO("支持不成"),
    NEUTRAL("中性提示"),
}

/**
 * 匹配器的整体输出:已分好三类、各自按分数(权重)降序。
 * 原则 7:支持成与支持不成并存时不强行合并,各自独立呈现。
 */
data class MatchReport(
    val supportYes: List<RuleMatchResult>,
    val supportNo: List<RuleMatchResult>,
    val neutral: List<RuleMatchResult>,
) {
    val all: List<RuleMatchResult> get() = supportYes + supportNo + neutral
    val hasConflict: Boolean get() = supportYes.isNotEmpty() && supportNo.isNotEmpty()
    val mainResult: List<RuleMatchResult> get() = all.filter { it.matchLayer == MatchLayer.MAIN_RESULT }
    val processOrCondition: List<RuleMatchResult>
        get() = all.filter { it.matchLayer == MatchLayer.PROCESS || it.matchLayer == MatchLayer.CONDITION }
    val sideReference: List<RuleMatchResult> get() = all.filter { it.matchLayer == MatchLayer.SIDE_REFERENCE }
}
