package com.liuyao.paipan.domain.model

data class Hexagram(
    val lowerTrigram: EightTrigram,
    val upperTrigram: EightTrigram,
) {
    val lines: List<YinYang>
        get() = lowerTrigram.lines + upperTrigram.lines

    val name: String
        get() = HEXAGRAM_NAMES[upperTrigram to lowerTrigram]
            ?: "${upperTrigram.nature}${lowerTrigram.nature}卦"

    val isSixClash: Boolean get() = name in SIX_CLASH
    val isSixCombine: Boolean get() = name in SIX_COMBINE

    fun transform(movingPositions: Set<Int>): Hexagram? {
        if (movingPositions.isEmpty()) return null
        val changed = lines.toMutableList()
        movingPositions.forEach { pos ->
            if (pos in 1..6) changed[pos - 1] = changed[pos - 1].flip()
        }
        val lower = EightTrigram.fromLines(changed.subList(0, 3))
        val upper = EightTrigram.fromLines(changed.subList(3, 6))
        return Hexagram(lower, upper)
    }

    companion object {
        fun of(lowerCn: String, upperCn: String): Hexagram =
            Hexagram(EightTrigram.fromCn(lowerCn), EightTrigram.fromCn(upperCn))

        private val HEXAGRAM_NAMES: Map<Pair<EightTrigram, EightTrigram>, String> = buildMap {
            val q = EightTrigram.QIAN
            val k = EightTrigram.KUN
            val zh = EightTrigram.ZHEN
            val x = EightTrigram.XUN
            val ka = EightTrigram.KAN
            val l = EightTrigram.LI
            val g = EightTrigram.GEN
            val d = EightTrigram.DUI
            put(q to q, "乾为天"); put(q to k, "天地否"); put(q to zh, "天雷无妄"); put(q to x, "天风姤")
            put(q to ka, "天水讼"); put(q to l, "天火同人"); put(q to g, "天山遁"); put(q to d, "天泽履")
            put(k to q, "地天泰"); put(k to k, "坤为地"); put(k to zh, "地雷复"); put(k to x, "地风升")
            put(k to ka, "地水师"); put(k to l, "地火明夷"); put(k to g, "地山谦"); put(k to d, "地泽临")
            put(zh to q, "雷天大壮"); put(zh to k, "雷地豫"); put(zh to zh, "震为雷"); put(zh to x, "雷风恒")
            put(zh to ka, "雷水解"); put(zh to l, "雷火丰"); put(zh to g, "雷山小过"); put(zh to d, "雷泽归妹")
            put(x to q, "风天小畜"); put(x to k, "风地观"); put(x to zh, "风雷益"); put(x to x, "巽为风")
            put(x to ka, "风水涣"); put(x to l, "风火家人"); put(x to g, "风山渐"); put(x to d, "风泽中孚")
            put(ka to q, "水天需"); put(ka to k, "水地比"); put(ka to zh, "水雷屯"); put(ka to x, "水风井")
            put(ka to ka, "坎为水"); put(ka to l, "水火既济"); put(ka to g, "水山蹇"); put(ka to d, "水泽节")
            put(l to q, "火天大有"); put(l to k, "火地晋"); put(l to zh, "火雷噬嗑"); put(l to x, "火风鼎")
            put(l to ka, "火水未济"); put(l to l, "离为火"); put(l to g, "火山旅"); put(l to d, "火泽睽")
            put(g to q, "山天大畜"); put(g to k, "山地剥"); put(g to zh, "山雷颐"); put(g to x, "山风蛊")
            put(g to ka, "山水蒙"); put(g to l, "山火贲"); put(g to g, "艮为山"); put(g to d, "山泽损")
            put(d to q, "泽天夬"); put(d to k, "泽地萃"); put(d to zh, "泽雷随"); put(d to x, "泽风大过")
            put(d to ka, "泽水困"); put(d to l, "泽火革"); put(d to g, "泽山咸"); put(d to d, "兑为泽")
        }

        private val SIX_CLASH = setOf(
            "乾为天",
            "坎为水",
            "艮为山",
            "震为雷",
            "巽为风",
            "离为火",
            "坤为地",
            "兑为泽",
            "天雷无妄",
            "雷天大壮",
        )
        private val SIX_COMBINE = setOf(
            "天地否",
            "地天泰",
            "山火贲",
            "火山旅",
            "水泽节",
            "泽水困",
            "雷地豫",
            "火天大有",
        )
    }
}
