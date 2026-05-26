package com.liuyao.paipan.domain.analysis

object QuestionFocusResolver {
    const val UNCLEAR = "占事问题不够明确，当前仅做基础分析"

    fun resolve(question: String): String {
        val q = question.trim()
        return when {
            q.isBlank() -> UNCLEAR
            any(q, "能不能过", "能过", "过吗", "通过", "及格", "考过") -> "PASS_OR_FAIL：通过 / 不通过"
            any(q, "录取", "录不录", "被录", "录上") -> "ADMISSION_RESULT：录取 / 不录取"
            any(q, "有没有消息", "消息", "回复", "联系", "通知") -> "MESSAGE_RESULT：有消息 / 无消息"
            any(q, "入职", "上岗", "正式上班") -> "EMPLOYMENT_ENTRY：入职 / 不入职"
            any(q, "offer", "录用", "聘用") -> "OFFER_RESULT：拿到 offer / 拿不到 offer"
            any(q, "怀孕", "有孕", "是否孕") -> "PREGNANCY_RESULT：有孕 / 无孕"
            any(q, "胎安", "胎是否安", "保胎") -> "FETAL_SAFETY：胎安 / 胎不安"
            any(q, "找到", "找回", "失物") -> "FOUND_OR_NOT：找回 / 找不到"
            any(q, "安全", "平安", "安危") -> "SAFETY_RESULT：安全 / 有风险"
            any(q, "会不会来", "来不来", "回来", "到不") -> "ARRIVAL_RESULT：来 / 不来"
            any(q, "合作", "合伙", "一起做") -> "COOPERATION_RESULT：合作成 / 合作不成"
            any(q, "官司", "输赢", "胜诉", "败诉") -> "LITIGATION_RESULT：胜 / 败"
            any(q, "财", "钱", "收益", "得财", "拿钱", "回款") -> "MONEY_GAIN_RESULT：得财 / 不得财"
            any(q, "病", "好转", "康复", "能不能好") -> "RECOVERY_RESULT：好转 / 难愈"
            any(q, "成", "成功", "能不能成") -> "RESULT：成 / 不成"
            any(q, "吉", "凶", "顺利") -> "AUSPICIOUS_RESULT：吉凶方向"
            else -> UNCLEAR
        }
    }

    fun resultKeywords(mainVariable: String): List<String> {
        if (mainVariable == UNCLEAR) return listOf("成败", "吉凶", "得失", "安危", "应期")
        return mainVariable
            .replace(Regex("""[A-Z_]+："""), "")
            .split("/", " / ", "、", " ", "：")
            .map { it.trim() }
            .filter { it.length >= 1 }
            .distinct()
    }

    private fun any(text: String, vararg needles: String): Boolean =
        needles.any { text.contains(it, ignoreCase = true) }
}
