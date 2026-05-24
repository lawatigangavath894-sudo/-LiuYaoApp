package com.liuyao.paipan.domain.engine

import com.liuyao.paipan.domain.model.Hexagram

/**
 * 10 & 11. 六冲卦 / 六合卦判断。
 *
 * 卦性(六冲/六合)是卦的固有属性,判定表封装在 [Hexagram];
 * 这里提供统一入口与综合结果,便于排盘流程与测试调用。
 */
object HexagramRelationCalculator {

    enum class HexNature(val cn: String) { SIX_CLASH("六冲"), SIX_COMBINE("六合"), NEUTRAL("无") }

    fun isSixClash(hexagram: Hexagram): Boolean = hexagram.isSixClash

    fun isSixCombine(hexagram: Hexagram): Boolean = hexagram.isSixCombine

    fun natureOf(hexagram: Hexagram): HexNature = when {
        hexagram.isSixClash -> HexNature.SIX_CLASH
        hexagram.isSixCombine -> HexNature.SIX_COMBINE
        else -> HexNature.NEUTRAL
    }
}
