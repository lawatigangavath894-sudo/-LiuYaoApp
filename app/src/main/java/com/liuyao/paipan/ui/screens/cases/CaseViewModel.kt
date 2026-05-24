package com.liuyao.paipan.ui.screens.cases

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.liuyao.paipan.data.db.AppDatabase
import com.liuyao.paipan.data.db.LiuYaoRepository
import com.liuyao.paipan.data.db.entity.CaseEntity
import com.liuyao.paipan.data.db.entity.CaseFeedbackEntity
import com.liuyao.paipan.domain.match.MatchReport
import com.liuyao.paipan.domain.model.CaseVerdict
import com.liuyao.paipan.domain.model.DivinationCategory
import com.liuyao.paipan.domain.model.LiuYaoChart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneOffset
import java.util.UUID

data class CasesUiState(
    val cases: List<CaseEntity> = emptyList(),
    val filter: DivinationCategory? = null,
    val query: String = "",
    val isLoading: Boolean = false,
)

/** 案例详情聚合 */
data class CaseDetail(
    val case: CaseEntity,
    val feedback: CaseFeedbackEntity?,
    val chart: com.liuyao.paipan.data.db.entity.ChartEntity? = null,
    /** 当时命中的断语:(id, 原文) */
    val hitRules: List<Pair<String, String>> = emptyList(),
)

class CaseViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = LiuYaoRepository(AppDatabase.get(app))

    private val _ui = MutableStateFlow(CasesUiState())
    val ui: StateFlow<CasesUiState> = _ui.asStateFlow()

    private val _detail = MutableStateFlow<CaseDetail?>(null)
    val detail: StateFlow<CaseDetail?> = _detail.asStateFlow()

    init { load() }

    fun load() {
        _ui.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val list = resolveList()
            _ui.update { it.copy(cases = list, isLoading = false) }
        }
    }

    private suspend fun resolveList(): List<CaseEntity> {
        val st = _ui.value
        val base = when {
            st.query.isNotBlank() -> repo.searchCases(st.query)
            st.filter != null -> repo.casesByCategory(st.filter.name)
            else -> repo.allCases()
        }
        return base
    }

    fun setFilter(category: DivinationCategory?) {
        _ui.update { it.copy(filter = category, query = "") }
        load()
    }

    fun setQuery(q: String) {
        _ui.update { it.copy(query = q) }
        load()
    }

    fun toggleFavorite(id: String) {
        viewModelScope.launch {
            val cur = _ui.value.cases.firstOrNull { it.id == id } ?: return@launch
            repo.setCaseFavorite(id, !cur.favorite)
            load()
        }
    }

    fun deleteCase(id: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repo.deleteCase(id)
            load()
            onDone()
        }
    }

    /**
     * 保存当前排盘为案例。
     * @param report 当前断语匹配结果(用于记录"当时命中的断语" id 快照),可空。
     */
    fun saveCurrentChartAsCase(chart: LiuYaoChart, report: MatchReport?, onDone: (String) -> Unit = {}) {
        viewModelScope.launch {
            repo.saveChart(chart) // 确保原盘已持久化
            val hitIds = report?.all?.map { it.rule.id }?.distinct().orEmpty()
            val caseId = "case-${UUID.randomUUID().toString().take(8)}"
            val now = System.currentTimeMillis() / 1000
            repo.saveCase(
                CaseEntity(
                    id = caseId,
                    chartId = chart.id,
                    title = chart.question.ifBlank { "未命名案例" },
                    createdEpoch = now,
                    category = chart.category?.name,
                    question = chart.question,
                    castEpoch = chart.dateTime.toEpochSecond(ZoneOffset.UTC),
                    favorite = false,
                    hitRuleIdsCsv = hitIds.joinToString(","),
                ),
            )
            load()
            onDone(caseId)
        }
    }

    fun openDetail(id: String) {
        viewModelScope.launch {
            val c = repo.caseById(id) ?: return@launch
            val fb = repo.feedbackOf(id)
            val chart = repo.loadChartEntity(c.chartId)
            val hitIds = c.hitRuleIdsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val hitRules = hitIds.mapNotNull { rid ->
                repo.loadRule(rid)?.let { rid to it.originalText }
            }
            _detail.update { CaseDetail(c, fb, chart, hitRules) }
        }
    }

    /** 提交/更新反馈 */
    fun submitFeedback(
        caseId: String,
        verdict: CaseVerdict,
        actualResult: String,
        note: String,
        hitRuleIds: List<String>,
        missRuleIds: List<String>,
        onDone: () -> Unit = {},
    ) {
        viewModelScope.launch {
            repo.saveFeedback(
                CaseFeedbackEntity(
                    caseId = caseId,
                    verdict = verdict.name,
                    actualResult = actualResult,
                    note = note,
                    feedbackEpoch = System.currentTimeMillis() / 1000,
                    hitRuleIdsCsv = hitRuleIds.joinToString(","),
                    missRuleIdsCsv = missRuleIds.joinToString(","),
                ),
            )
            // 权重修正:验中的规则记成功,误判的记失败
            val now = System.currentTimeMillis() / 1000
            hitRuleIds.forEach { repo.recordRuleFeedback(it, CaseVerdict.SUCCESS, now) }
            missRuleIds.forEach { repo.recordRuleFeedback(it, CaseVerdict.FAILURE, now) }
            openDetail(caseId)
            onDone()
        }
    }
}
