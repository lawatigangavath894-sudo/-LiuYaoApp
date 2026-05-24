package com.liuyao.paipan.domain.model

/**
 * 爻位序号 → 中文名(初/二/三/四/五/上)。
 * 统一此前散落在 CastScreen / MatchResultCard / MarkdownExporter 的同名私有函数。
 */
fun yaoPositionName(position: Int): String = when (position) {
    1 -> "初"
    2 -> "二"
    3 -> "三"
    4 -> "四"
    5 -> "五"
    6 -> "上"
    else -> position.toString()
}
