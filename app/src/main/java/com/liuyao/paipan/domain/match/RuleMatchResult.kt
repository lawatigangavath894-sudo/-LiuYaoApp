package com.liuyao.paipan.domain.match

import com.liuyao.paipan.domain.analysis.MatchLayer
import com.liuyao.paipan.domain.model.SixKin
import com.liuyao.paipan.domain.rule.DivinationRule
import com.liuyao.paipan.domain.rule.RuleCondition
import com.liuyao.paipan.domain.rule.RulePolarity

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
    val relevanceReason: String = "",
    val conflictReason: String? = null,
    val confidenceLevel: String = "中",
) {
    val bucket: ResultBucket
        get() = when (polarity) {
            RulePolarity.AUSPICIOUS -> ResultBucket.SUPPORT_YES
            RulePolarity.INAUSPICIOUS -> ResultBucket.SUPPORT_NO
            RulePolarity.MIXED, RulePolarity.NEUTRAL -> ResultBucket.NEUTRAL
        }
}

enum class ResultBucket(val cn: String) {
    SUPPORT_YES("支持成"),
    SUPPORT_NO("支持不成"),
    NEUTRAL("中性提示"),
}

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
    val riskWarnings: List<RuleMatchResult> get() = all.filter { it.matchLayer == MatchLayer.RISK_WARNING }
    val sideReference: List<RuleMatchResult> get() = all.filter { it.matchLayer == MatchLayer.SIDE_REFERENCE }
    val insufficientData: List<RuleMatchResult> get() = all.filter { it.matchLayer == MatchLayer.INSUFFICIENT_DATA }
}
