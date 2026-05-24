package com.liuyao.paipan.domain.engine

import com.liuyao.paipan.domain.model.EightTrigram
import com.liuyao.paipan.domain.model.Hexagram
import com.liuyao.paipan.domain.model.YinYang

/**
 * 由六爻阴阳构造卦的工厂(engine 层算法)。
 *
 * 数据结构 [Hexagram] 定义在 domain/model;此处提供"六爻 → 卦"的组装与拆分。
 * 约定:六爻列表 index0 = 初爻(最下),index5 = 上爻(最上)。
 */
object HexagramFactory {

    /**
     * 由六爻阴阳构造本卦。
     * @param lines 长度必须为 6,index0=初爻。
     */
    fun fromLines(lines: List<YinYang>): Hexagram {
        require(lines.size == 6) { "六爻须为 6 位,当前 ${lines.size}" }
        val lower = EightTrigram.fromLines(lines.subList(0, 3))
        val upper = EightTrigram.fromLines(lines.subList(3, 6))
        return Hexagram(lowerTrigram = lower, upperTrigram = upper)
    }

    /**
     * 由"阳/阴"布尔表示构造(true=阳)。便于测试直接传 6 个布尔。
     */
    fun fromBooleans(yangFlags: List<Boolean>): Hexagram =
        fromLines(yangFlags.map { if (it) YinYang.YANG else YinYang.YIN })

    /** 拆出 (内卦, 外卦) */
    fun split(hexagram: Hexagram): Pair<EightTrigram, EightTrigram> =
        hexagram.lowerTrigram to hexagram.upperTrigram
}
