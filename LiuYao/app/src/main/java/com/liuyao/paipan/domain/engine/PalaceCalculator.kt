package com.liuyao.paipan.domain.engine

import com.liuyao.paipan.domain.model.EightTrigram
import com.liuyao.paipan.domain.model.Hexagram
import com.liuyao.paipan.domain.model.Palace

/**
 * 5. 卦宫判断。
 *
 * 卦宫由八宫卦序决定,不能简单按"外卦/内卦"取;游魂、归魂卦尤需查表。
 * 这里委托 [HexagramRepository] 的展开结果。
 */
object PalaceCalculator {

    fun palaceOf(hexagram: Hexagram): Palace =
        HexagramRepository.lookup(hexagram).palace

    fun palaceOf(inner: EightTrigram, outer: EightTrigram): Palace =
        HexagramRepository.lookup(inner, outer).palace
}
