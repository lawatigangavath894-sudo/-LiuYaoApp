package com.liuyao.paipan.domain.analysis

import com.liuyao.paipan.domain.model.DivinationCategory
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
    val keyLineIndexes: List<Int>,
    val movingLineIndexes: List<Int>,
    val hiddenSpiritLineIndexes: List<Int>,
    val flyingSpiritLineIndexes: List<Int>,
    val focusKeywords: List<String>,
    val knowledgeSnippets: List<KnowledgeSnippet>,
    val analysisDirection: String,
    val lockReason: String,
    val uncertainReason: String?,
    val excludedScopes: List<String>,
    val usedFallback: Boolean,
    val analysisScope: AnalysisScope,
)

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
    MAIN_RESULT("主结果相关断语"),
    PROCESS("过程 / 条件相关断语"),
    CONDITION("条件参考断语"),
    SIDE_REFERENCE("旁参考断语"),
}
