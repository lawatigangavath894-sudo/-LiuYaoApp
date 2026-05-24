package com.liuyao.paipan.domain.engine

import com.liuyao.paipan.domain.model.Hexagram

/**
 * 9. 变卦生成。
 *
 * 动爻(老阳/老阴)变为其反,得变卦。动爻位以 1..6 表示(1=初爻)。
 * 无动爻时返回 null(静卦)。
 */
object ChangedHexagramCalculator {

    /**
     * @param movingPositions 动爻位集合(1..6)
     */
    fun changedOf(original: Hexagram, movingPositions: Set<Int>): Hexagram? =
        original.transform(movingPositions)

    /**
     * @param movingFlags 长度 6 的布尔列表,index0=初爻,true=该爻为动爻
     */
    fun changedOf(original: Hexagram, movingFlags: List<Boolean>): Hexagram? {
        require(movingFlags.size == 6) { "动爻标志须 6 位" }
        val positions = movingFlags.mapIndexedNotNull { i, moving -> if (moving) i + 1 else null }.toSet()
        return original.transform(positions)
    }
}
