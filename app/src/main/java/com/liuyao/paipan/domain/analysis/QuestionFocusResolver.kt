package com.liuyao.paipan.domain.analysis

object QuestionFocusResolver {
    fun resolve(question: String): String {
        val q = question.trim()
        return when {
            q.contains("录取") -> "录取 / 不录取"
            q.contains("入职") || q.contains("上岸") -> "正式入职 / 不入职"
            q.contains("能不能过") || q.contains("能过") || q.contains("过吗") -> "通过 / 不通过"
            q.contains("联系") || q.contains("消息") -> "有联系消息 / 无联系消息"
            q.contains("找到") || q.contains("找回") || q.contains("失物") -> "找回 / 找不到"
            q.contains("怀孕") || q.contains("有孕") -> "有孕 / 无孕"
            q.contains("安全") || q.contains("平安") -> "安危"
            q.contains("会不会来") || q.contains("来不来") || q.contains("回来") -> "来 / 不来"
            q.contains("成") || q.contains("成功") -> "成 / 不成"
            q.contains("吉") || q.contains("凶") -> "吉凶方向"
            q.isBlank() -> "未命名占事的主结果"
            else -> q.take(18)
        }
    }

    fun resultKeywords(mainVariable: String): List<String> =
        mainVariable.split("/", " / ", "、", " ")
            .map { it.trim() }
            .filter { it.length >= 1 }
            .distinct()
}
