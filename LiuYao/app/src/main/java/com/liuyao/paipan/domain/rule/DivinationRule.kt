package com.liuyao.paipan.domain.rule

import com.liuyao.paipan.domain.model.DivinationCategory

/**
 * 1. 断语规则 DivinationRule —— 一条结构化的刘昌明六爻断语。
 *
 * 字段严格对应需求清单;在其后追加两个**可空扩展位**
 * ([explanation]、[matchHint]),用于承载分层解释与匹配提示,默认 null,
 * 不影响既有字段语义,后续无需改签名即可丰富。
 *
 * 说明:
 *  - [matchConditions] 命中条件、[excludeConditions] 排除条件,均为结构化
 *    [RuleCondition];而 [conditionText] 保留原始/人读的条件描述,二者并存,
 *    便于"先有文本、后结构化"的渐进式录入。
 *  - [priority] 用 Int(可参考 [RulePriority] 档位);[confidenceWeight] 为
 *    0.0..1.0 的置信权重,供未来匹配打分使用(本轮不计算)。
 *  - [tags] 用 List<String>,标准标签取值见 [RuleTag.code]。
 */
data class DivinationRule(
    val id: String,
    val sourceId: String,
    val sourceName: String,
    val category: DivinationCategory,
    val target: RuleTarget,
    val originalText: String,
    val plainExplanation: String,
    val conditionText: String,
    val positiveMeaning: String?,
    val negativeMeaning: String?,
    val matchConditions: List<RuleCondition>,
    val excludeConditions: List<RuleCondition>,
    val polarity: RulePolarity,
    val priority: Int,
    val confidenceWeight: Double,
    val tags: List<String>,
    // —— 可空扩展位(默认 null,保持与需求签名兼容) ——
    val explanation: RuleExplanation? = null,
    val matchHint: RuleMatchHint? = null,
) {
    init {
        require(confidenceWeight in 0.0..1.0) { "confidenceWeight 须在 0.0..1.0,当前 $confidenceWeight" }
    }

    /** 标签是否含某标准标签 */
    fun hasTag(tag: RuleTag): Boolean = tag.code in tags

    /** 优先级档位(由 Int 反查,便于展示) */
    val priorityLevel: RulePriority get() = RulePriority.nearest(priority)
}
