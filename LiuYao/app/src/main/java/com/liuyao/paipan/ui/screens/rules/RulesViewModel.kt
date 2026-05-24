package com.liuyao.paipan.ui.screens.rules

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.liuyao.paipan.data.db.AppDatabase
import com.liuyao.paipan.data.db.LiuYaoRepository
import com.liuyao.paipan.data.db.entity.RuleSourceEntity
import com.liuyao.paipan.domain.model.DivinationCategory
import com.liuyao.paipan.domain.rule.DivinationRule
import com.liuyao.paipan.domain.rule.RulePolarity
import com.liuyao.paipan.domain.rule.RuleTarget
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * 断语库列表/详情/编辑的状态。
 *
 * UI 仅读本状态;持久化全部经 [LiuYaoRepository] → Room。本轮不含匹配算法。
 */
data class RuleUiState(
    val rules: List<DivinationRule> = emptyList(),
    val filter: DivinationCategory? = null, // null = 全部
    val isLoading: Boolean = false,
    val selectedId: String? = null,
    /** 规则历史统计缓存:ruleId → stats */
    val statsById: Map<String, com.liuyao.paipan.data.db.entity.RuleStatsEntity> = emptyMap(),
) {
    /** 按筛选后的可见列表 */
    val visibleRules: List<DivinationRule>
        get() = filter?.let { f -> rules.filter { it.category == f } } ?: rules

    val selectedRule: DivinationRule?
        get() = rules.firstOrNull { it.id == selectedId }
}

/**
 * 编辑表单状态(新增/编辑共用)。字段为录入用的字符串/枚举,
 * 通过 [toRule] 转为领域 [DivinationRule];[from] 由现有规则回填。
 *
 * 本轮"适用条件/不适用条件"以文本录入(对应 conditionText / 反面说明),
 * 结构化 RuleCondition 列表暂留空,符合"先文本后结构化"的渐进路线。
 */
data class RuleFormState(
    val id: String? = null,             // null = 新增
    val sourceName: String = "刘昌明《象断六爻》",
    val category: DivinationCategory = DivinationCategory.MARRIAGE,
    val targetType: RuleTarget.Type = RuleTarget.Type.USE_GOD,
    val originalText: String = "",
    val plainExplanation: String = "",
    val conditionText: String = "",      // 适用条件(文本)
    val excludeText: String = "",        // 不适用条件(文本)
    val tagsText: String = "",           // 逗号分隔
    val priority: Int = 50,
    val confidenceWeight: Double = 0.6,
) {
    val isEditing: Boolean get() = id != null
    val canSave: Boolean get() = originalText.isNotBlank() && plainExplanation.isNotBlank()

    fun toRule(): DivinationRule = DivinationRule(
        id = id ?: "rule-${UUID.randomUUID().toString().take(8)}",
        sourceId = "src-manual",
        sourceName = sourceName,
        category = category,
        target = RuleTarget(type = targetType),
        originalText = originalText.trim(),
        plainExplanation = plainExplanation.trim(),
        conditionText = conditionText.trim(),
        positiveMeaning = null,
        negativeMeaning = excludeText.trim().ifBlank { null },
        matchConditions = emptyList(),   // 本轮文本录入,不做结构化条件
        excludeConditions = emptyList(),
        polarity = RulePolarity.NEUTRAL,
        priority = priority,
        confidenceWeight = confidenceWeight,
        tags = tagsText.split(",", "，").map { it.trim() }.filter { it.isNotEmpty() },
    )

    companion object {
        fun from(rule: DivinationRule): RuleFormState = RuleFormState(
            id = rule.id,
            sourceName = rule.sourceName,
            category = rule.category,
            targetType = rule.target.type,
            originalText = rule.originalText,
            plainExplanation = rule.plainExplanation,
            conditionText = rule.conditionText,
            excludeText = rule.negativeMeaning ?: "",
            tagsText = rule.tags.joinToString(", "),
            priority = rule.priority,
            confidenceWeight = rule.confidenceWeight,
        )
    }
}

class RulesViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = LiuYaoRepository(AppDatabase.get(app))

    private val _ui = MutableStateFlow(RuleUiState())
    val ui: StateFlow<RuleUiState> = _ui.asStateFlow()

    init {
        load()
    }

    fun load() {
        _ui.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val rules = repo.loadAllRules()
            _ui.update { it.copy(rules = rules, isLoading = false) }
        }
    }

    fun setFilter(category: DivinationCategory?) {
        _ui.update { it.copy(filter = category) }
    }

    fun select(id: String?) {
        _ui.update { it.copy(selectedId = id) }
    }

    /** 加载某规则历史统计,存入缓存供详情页展示 */
    fun loadStats(ruleId: String) {
        viewModelScope.launch {
            val s = repo.statsOf(ruleId) ?: return@launch
            _ui.update { it.copy(statsById = it.statsById + (ruleId to s)) }
        }
    }

    fun save(form: RuleFormState, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            val source = RuleSourceEntity("src-manual", "手动录入", form.sourceName, null)
            repo.saveRule(form.toRule(), source = source)
            val rules = repo.loadAllRules()
            _ui.update { it.copy(rules = rules) }
            onDone()
        }
    }

    fun delete(id: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repo.deleteRule(id)
            val rules = repo.loadAllRules()
            _ui.update { it.copy(rules = rules, selectedId = null) }
            onDone()
        }
    }
}
