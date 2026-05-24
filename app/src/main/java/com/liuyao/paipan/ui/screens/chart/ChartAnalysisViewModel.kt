package com.liuyao.paipan.ui.screens.chart

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.liuyao.paipan.data.db.AppDatabase
import com.liuyao.paipan.data.db.LiuYaoRepository
import com.liuyao.paipan.domain.match.MatchReport
import com.liuyao.paipan.domain.match.MockMatchResults
import com.liuyao.paipan.domain.match.RuleMatcher
import com.liuyao.paipan.domain.model.DivinationCategory
import com.liuyao.paipan.domain.model.LiuYaoChart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 断语分析状态:对当前卦跑匹配器,产出三分类 [MatchReport];
 * 并维护"收藏到批注"的规则 id 集合(本轮轻量内存态,不写案例反馈)。
 */
data class AnalysisUiState(
    val report: MatchReport? = null,
    val isLoading: Boolean = false,
    val favoriteRuleIds: Set<String> = emptySet(),
    val usingMock: Boolean = false,
)

class ChartAnalysisViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = LiuYaoRepository(AppDatabase.get(app))

    private val _ui = MutableStateFlow(AnalysisUiState())
    val ui: StateFlow<AnalysisUiState> = _ui.asStateFlow()

    private var analyzedFor: String? = null

    /**
     * 对给定卦与占类分析。重复同一卦不重算。
     * 规则来自 Room;库为空时回退到示例结果以便预览。
     */
    fun analyze(chart: LiuYaoChart, category: DivinationCategory) {
        if (analyzedFor == chart.id && _ui.value.report != null) return
        analyzedFor = chart.id
        _ui.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val rules = repo.loadAllRules()
            val report = if (rules.isEmpty()) {
                MockMatchResults.report
            } else {
                val relMap = repo.reliabilityMap()
                val r = RuleMatcher.match(chart, category, rules) { ruleId -> relMap[ruleId] ?: 1.0 }
                // 记录命中规则"被使用一次"(供使用次数/最近使用时间统计)
                val now = System.currentTimeMillis() / 1000
                r.all.forEach { repo.markRuleUsed(it.rule.id, now) }
                r
            }
            _ui.update {
                it.copy(report = report, isLoading = false, usingMock = rules.isEmpty())
            }
        }
    }

    /** 收藏 / 取消收藏某断语到批注(内存态) */
    fun toggleFavorite(ruleId: String) {
        _ui.update {
            val cur = it.favoriteRuleIds
            it.copy(favoriteRuleIds = if (ruleId in cur) cur - ruleId else cur + ruleId)
        }
    }
}
