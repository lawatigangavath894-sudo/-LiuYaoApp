package com.liuyao.paipan.ui.screens

import com.liuyao.paipan.domain.model.DivinationCategory

/**
 * 列表页占类筛选项(题目指定的 10 类)。
 * 供 RulesScreen / CasesScreen 共用,避免重复定义。
 */
val FILTER_CATEGORIES: List<Pair<String, DivinationCategory>> = listOf(
    "婚姻" to DivinationCategory.MARRIAGE,
    "财运" to DivinationCategory.WEALTH,
    "考试" to DivinationCategory.STUDY,
    "工作" to DivinationCategory.CAREER,
    "疾病" to DivinationCategory.HEALTH,
    "寻物" to DivinationCategory.LOST,
    "孕产" to DivinationCategory.PREGNANCY,
    "官司" to DivinationCategory.LAWSUIT,
    "房宅" to DivinationCategory.HOUSE,
    "合作" to DivinationCategory.COOPERATION,
)
