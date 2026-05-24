package com.liuyao.paipan.domain.match

import com.liuyao.paipan.domain.rule.DivinationRule
import com.liuyao.paipan.domain.rule.RuleCondition

/**
 * 命中原因文本构建。纯函数,便于测试与 UI 复用。
 */
object MatchExplanationBuilder {

    /**
     * 构建一条结果的解释。
     * @param matched  是否命中
     * @param excluded 命中的排除条件(非空表示被排除)
     */
    fun build(
        rule: DivinationRule,
        matched: Boolean,
        matchedConditions: List<RuleCondition>,
        failedConditions: List<RuleCondition>,
        excluded: List<RuleCondition>,
    ): String = buildString {
        when {
            excluded.isNotEmpty() -> {
                append("已排除:命中排除条件 ")
                append(excluded.joinToString("、") { it.cn })
                append("。")
            }
            matched -> {
                append("命中:")
                append(matchedConditions.joinToString("、") { it.cn })
                append("。")
                if (rule.plainExplanation.isNotBlank()) {
                    append(" ")
                    append(rule.plainExplanation)
                }
            }
            else -> {
                append("未命中。")
                if (failedConditions.isNotEmpty()) {
                    append("缺少条件:")
                    append(failedConditions.joinToString("、") { it.cn })
                    append("。")
                }
            }
        }
    }
}
