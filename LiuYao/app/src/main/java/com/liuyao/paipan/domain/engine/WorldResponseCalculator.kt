package com.liuyao.paipan.domain.engine

import com.liuyao.paipan.domain.model.EightTrigram
import com.liuyao.paipan.domain.model.Hexagram

/**
 * 6. 世应定位。
 *
 * 世爻由八宫卦序确定(本宫世六、一世世一……游魂世四、归魂世三);
 * 应爻恒为与世爻相隔三位者(世≤3 则应=世+3,否则应=世−3)。
 */
object WorldResponseCalculator {

    data class WorldResponse(val worldPosition: Int, val responsePosition: Int)

    fun of(hexagram: Hexagram): WorldResponse {
        val info = HexagramRepository.lookup(hexagram)
        return WorldResponse(info.worldPosition, info.responsePosition)
    }

    fun of(inner: EightTrigram, outer: EightTrigram): WorldResponse {
        val info = HexagramRepository.lookup(inner, outer)
        return WorldResponse(info.worldPosition, info.responsePosition)
    }
}
