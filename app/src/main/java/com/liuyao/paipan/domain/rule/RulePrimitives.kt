package com.liuyao.paipan.domain.rule

import com.liuyao.paipan.domain.model.SixKin

/**
 * 断语知识库 · 基础类型(纯领域模型)。
 *
 * 不含匹配算法、不含持久化、不含 UI。仅定义"刘昌明六爻断语"被结构化所需的
 * 目标对象、吉凶极性、优先级、标签、来源、白话解释、匹配提示等概念。
 *
 * 设计要点:
 *  - 复用排盘模型中的 [SixKin] / [com.liuyao.paipan.domain.model.DivinationCategory];
 *  - 枚举附带中文名 [cn],便于展示;
 *  - 留扩展位(如 RuleTarget.kin 指定具体六亲、RulePriority.value 量化)。
 */

// ════════════════════════════════════════════════════════════════════
// 4. 规则目标 RuleTarget —— 断语作用于谁
// ════════════════════════════════════════════════════════════════════

/**
 * 断语描述的对象。多数断语围绕"用神"展开,但也可能直指世/应/某六亲/某爻位。
 *
 * @property kin   当 [type] 为 [Type.SPECIFIC_KIN] 时,指定的六亲;否则为 null。
 * @property position 当 [type] 为 [Type.SPECIFIC_POSITION] 时,指定爻位(1..6);否则 null。
 */
data class RuleTarget(
    val type: Type,
    val kin: SixKin? = null,
    val position: Int? = null,
) {
    enum class Type(val cn: String) {
        USE_GOD("用神"),        // 随占类动态确定的用神
        WORLD("世爻"),
        RESPONSE("应爻"),
        SPECIFIC_KIN("指定六亲"),
        SPECIFIC_POSITION("指定爻位"),
        WHOLE_CHART("全局"),    // 针对整卦(如六冲卦/反吟卦)
    }

    val cn: String
        get() = when (type) {
            Type.SPECIFIC_KIN -> kin?.cn ?: type.cn
            Type.SPECIFIC_POSITION -> "第${position}爻"
            else -> type.cn
        }

    companion object {
        val UseGod = RuleTarget(Type.USE_GOD)
        val World = RuleTarget(Type.WORLD)
        val Response = RuleTarget(Type.RESPONSE)
        val WholeChart = RuleTarget(Type.WHOLE_CHART)
        fun kin(k: SixKin) = RuleTarget(Type.SPECIFIC_KIN, kin = k)
        fun position(p: Int) = RuleTarget(Type.SPECIFIC_POSITION, position = p)
    }
}

// ════════════════════════════════════════════════════════════════════
// 5. 吉凶极性 RulePolarity
// ════════════════════════════════════════════════════════════════════

enum class RulePolarity(val cn: String) {
    AUSPICIOUS("吉"),
    INAUSPICIOUS("凶"),
    MIXED("吉凶参半"),
    NEUTRAL("中性 / 描述"),
}

// ════════════════════════════════════════════════════════════════════
// 6. 优先级 RulePriority
// ════════════════════════════════════════════════════════════════════

/**
 * 规则优先级。[DivinationRule.priority] 用 Int 存储以便灵活排序;
 * 这里给出常用档位与 [value] 映射,作为约定俗成的取值参考。
 */
enum class RulePriority(val cn: String, val value: Int) {
    CRITICAL("决定性", 100), // 如"用神不上卦又被克"之类一票定调
    HIGH("高", 75),
    NORMAL("常规", 50),
    LOW("低", 25),
    REFERENCE("仅供参考", 10);

    companion object {
        /** 由 Int 反查最接近的档位(便于展示) */
        fun nearest(value: Int): RulePriority =
            entries.minByOrNull { kotlin.math.abs(it.value - value) } ?: NORMAL
    }
}

// ════════════════════════════════════════════════════════════════════
// 7. 标签 RuleTag
// ════════════════════════════════════════════════════════════════════

/**
 * 常用结构化标签枚举。[DivinationRule.tags] 用 List<String> 存储(允许自由标签),
 * 这里的枚举提供"标准标签"的统一取值,避免拼写发散;[code] 即存入 tags 的字符串。
 */
enum class RuleTag(val code: String, val cn: String) {
    USE_GOD_HIDDEN("use_god_hidden", "用神伏藏"),
    USE_GOD_VOID("use_god_void", "用神空亡"),
    USE_GOD_BROKEN("use_god_broken", "用神月破"),
    MOVING("moving", "动爻"),
    BACK_GENERATE("back_generate", "回头生"),
    BACK_RESTRAIN("back_restrain", "回头克"),
    SIX_CLASH("six_clash", "六冲"),
    SIX_COMBINE("six_combine", "六合"),
    WORLD_RESPONSE("world_response", "世应"),
    APPLY_TIMING("apply_timing", "应期"),
    JOB("job", "求职"),
    WEALTH("wealth", "财运"),
    DOCUMENT("document", "文书"),
    ;

    companion object {
        fun fromCode(code: String): RuleTag? = entries.firstOrNull { it.code == code }
    }
}

// ════════════════════════════════════════════════════════════════════
// 8. 来源 RuleSource
// ════════════════════════════════════════════════════════════════════

/**
 * 断语出处。一条规则用 [DivinationRule.sourceId] 关联到此。
 *
 * @param author 作者(如"刘昌明")
 * @param book   书名 / 资料名
 * @param locator 章节 / 页码 / 卦例编号等定位信息(可空)
 */
data class RuleSource(
    val id: String,
    val author: String,
    val book: String,
    val locator: String? = null,
) {
    val displayName: String
        get() = buildString {
            append(author)
            append("《").append(book).append("》")
            if (!locator.isNullOrBlank()) append(" · ").append(locator)
        }
}

// ════════════════════════════════════════════════════════════════════
// 9. 白话解释 RuleExplanation
// ════════════════════════════════════════════════════════════════════

/**
 * 对一条断语的多层解释,便于 UI 分层展示与教学。
 *
 * @param plain        白话释义(必填)
 * @param mechanism    生克机理(为什么这样断,可空)
 * @param example      举例说明(可空)
 * @param caveat       注意事项 / 例外提醒(可空)
 */
data class RuleExplanation(
    val plain: String,
    val mechanism: String? = null,
    val example: String? = null,
    val caveat: String? = null,
)

// ════════════════════════════════════════════════════════════════════
// 10. 匹配提示 RuleMatchHint
// ════════════════════════════════════════════════════════════════════

/**
 * 给(未来的)匹配器的提示信息。本轮不实现匹配算法,仅承载提示数据。
 *
 * @param requireAllMatch   true=需满足全部 matchConditions;false=满足任一即可
 * @param minScoreToShow    建议展示阈值(0..100),低于此分不在结果中显示
 * @param applyTimingNote   应期提示文本(可空)
 * @param notes             其他自由提示
 */
data class RuleMatchHint(
    val requireAllMatch: Boolean = true,
    val minScoreToShow: Int = 50,
    val applyTimingNote: String? = null,
    val notes: List<String> = emptyList(),
)
