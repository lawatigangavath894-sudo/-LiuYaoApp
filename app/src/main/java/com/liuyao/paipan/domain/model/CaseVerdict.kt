package com.liuyao.paipan.domain.model

/**
 * 案例反馈的结果类型。
 */
enum class CaseVerdict(val cn: String) {
    SUCCESS("成"),
    FAILURE("不成"),
    PARTIAL("部分成"),
    UNKNOWN("未知");

    companion object {
        fun fromName(name: String?): CaseVerdict =
            entries.firstOrNull { it.name == name } ?: UNKNOWN
    }
}
