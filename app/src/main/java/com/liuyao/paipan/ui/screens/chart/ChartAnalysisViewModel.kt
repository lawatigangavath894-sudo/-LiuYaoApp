package com.liuyao.paipan.ui.screens.chart

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.liuyao.paipan.data.db.AppDatabase
import com.liuyao.paipan.data.db.LiuYaoRepository
import com.liuyao.paipan.data.knowledge.LiuYaoKnowledgeSearchService
import com.liuyao.paipan.domain.analysis.AnalysisLock
import com.liuyao.paipan.domain.analysis.AnalysisLockResolver
import com.liuyao.paipan.domain.analysis.QuestionFocusResolver
import com.liuyao.paipan.domain.match.MatchReport
import com.liuyao.paipan.domain.match.RuleMatcher
import com.liuyao.paipan.domain.model.DivinationCategory
import com.liuyao.paipan.domain.model.LiuYaoChart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AnalysisUiState(
    val report: MatchReport? = null,
    val analysisLock: AnalysisLock? = null,
    val isLoading: Boolean = false,
    val favoriteRuleIds: Set<String> = emptySet(),
    val knowledgeMessage: String? = null,
)

class ChartAnalysisViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = LiuYaoRepository(AppDatabase.get(app))
    private val knowledgeSearch = LiuYaoKnowledgeSearchService(app.applicationContext)

    private val _ui = MutableStateFlow(AnalysisUiState())
    val ui: StateFlow<AnalysisUiState> = _ui.asStateFlow()

    private var analyzedFor: String? = null

    fun analyze(chart: LiuYaoChart, category: DivinationCategory) {
        if (analyzedFor == chart.id && _ui.value.report != null) return
        analyzedFor = chart.id
        _ui.update { it.copy(isLoading = true, knowledgeMessage = null) }
        viewModelScope.launch {
            runCatching {
                val rules = repo.loadAllRules()
                val mainVariable = QuestionFocusResolver.resolve(chart.question)
                val snippets = knowledgeSearch.searchRelevantSnippets(
                    category = category,
                    question = chart.question,
                    chart = chart,
                    rules = rules,
                    extraKeywords = QuestionFocusResolver.resultKeywords(mainVariable),
                    limit = 10,
                )
                val lock = AnalysisLockResolver.resolve(chart, category, snippets)
                val report = if (rules.isEmpty()) {
                    MatchReport(emptyList(), emptyList(), emptyList())
                } else {
                    val relMap = repo.reliabilityMap()
                    val matched = RuleMatcher.match(
                        chart = chart,
                        category = category,
                        rules = rules,
                        reliabilityProvider = { ruleId -> relMap[ruleId] ?: 1.0 },
                        lock = lock,
                    )
                    val now = System.currentTimeMillis() / 1000
                    matched.all.forEach { repo.markRuleUsed(it.rule.id, now) }
                    matched
                }
                _ui.update {
                    it.copy(
                        report = report,
                        analysisLock = lock,
                        isLoading = false,
                        knowledgeMessage = when {
                            snippets.isEmpty() -> "未检索到刘昌明资料片段，当前使用基础锁定。"
                            rules.isEmpty() -> "断语库为空，仅生成分析锁定与资料片段。"
                            else -> null
                        },
                    )
                }
            }.onFailure { error ->
                _ui.update {
                    it.copy(
                        isLoading = false,
                        knowledgeMessage = "分析生成失败：${error.message ?: "未知错误"}",
                    )
                }
            }
        }
    }

    fun toggleFavorite(ruleId: String) {
        _ui.update {
            val cur = it.favoriteRuleIds
            it.copy(favoriteRuleIds = if (ruleId in cur) cur - ruleId else cur + ruleId)
        }
    }
}
