package com.liuyao.paipan.domain.analysis

import com.liuyao.paipan.domain.model.DivinationCategory
import com.liuyao.paipan.domain.model.SixGod
import com.liuyao.paipan.domain.model.SixKin

data class AnalysisLock(
    val chartId: String,
    val question: String,
    val category: DivinationCategory,
    val mainVariable: String,
    val primaryUsefulGod: SixKin?,
    val secondaryUsefulGods: List<SixKin>,
    val worldLineIndex: Int?,
    val responseLineIndex: Int?,
    val usefulGodLineIndexes: List<Int>,
    val keyLineIndexes: List<Int>,
    val movingLineIndexes: List<Int>,
    val changedLineIndexes: List<Int>,
    val hiddenSpiritLineIndexes: List<Int>,
    val flyingSpiritLineIndexes: List<Int>,
    val voidLineIndexes: List<Int>,
    val monthBrokenLineIndexes: List<Int>,
    val dayClashedLineIndexes: List<Int>,
    val combinedLineIndexes: List<Int>,
    val clashedLineIndexes: List<Int>,
    val strongLineIndexes: List<Int>,
    val weakLineIndexes: List<Int>,
    val relatedSixGods: List<SixGod>,
    val relatedShenSha: List<String>,
    val focusKeywords: List<String>,
    val knowledgeSnippets: List<KnowledgeSnippet>,
    val analysisWarnings: List<String>,
    val analysisDirection: String,
    val lockReason: String,
    val uncertainReason: String?,
    val excludedScopes: List<String>,
    val usedFallback: Boolean,
    val analysisScope: AnalysisScope,
) {
    val fallbackUsed: Boolean get() = usedFallback
}

data class AnalysisContext(
    val lock: AnalysisLock,
    val matchedRulesCount: Int = 0,
    val relatedCasesCount: Int = 0,
) {
    val chartId: String get() = lock.chartId
    val question: String get() = lock.question
    val category: DivinationCategory get() = lock.category
    val mainVariable: String get() = lock.mainVariable
}

data class KnowledgeSnippet(
    val id: String,
    val sourceName: String,
    val sectionTitle: String?,
    val originalText: String,
    val matchedKeywords: List<String>,
    val relevanceScore: Double,
)

enum class AnalysisScope {
    MAIN_RESULT,
    PROCESS,
    CONDITION,
    FOLLOW_UP,
    RISK,
    INSUFFICIENT_DATA,
}

enum class MatchLayer(val title: String) {
    MAIN_RESULT("主结果断语"),
    PROCESS("过程断语"),
    CONDITION("条件断语"),
    RISK_WARNING("风险提示"),
    SIDE_REFERENCE("旁参考断语"),
    INSUFFICIENT_DATA("资料不足"),
}
