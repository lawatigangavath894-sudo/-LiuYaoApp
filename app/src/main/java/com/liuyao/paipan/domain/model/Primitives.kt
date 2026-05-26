package com.liuyao.paipan.domain.model

enum class YinYang {
    YANG,
    YIN;

    val symbol: String get() = if (this == YANG) "—" else "- -"
    fun flip(): YinYang = if (this == YANG) YIN else YANG
}

enum class FiveElement(val cn: String) {
    WOOD("木"),
    FIRE("火"),
    EARTH("土"),
    METAL("金"),
    WATER("水");

    fun generates(): FiveElement = when (this) {
        WOOD -> FIRE
        FIRE -> EARTH
        EARTH -> METAL
        METAL -> WATER
        WATER -> WOOD
    }

    fun generatedBy(): FiveElement = when (this) {
        FIRE -> WOOD
        EARTH -> FIRE
        METAL -> EARTH
        WATER -> METAL
        WOOD -> WATER
    }

    fun restrains(): FiveElement = when (this) {
        WOOD -> EARTH
        EARTH -> WATER
        WATER -> FIRE
        FIRE -> METAL
        METAL -> WOOD
    }

    fun restrainedBy(): FiveElement = when (this) {
        EARTH -> WOOD
        WATER -> EARTH
        FIRE -> WATER
        METAL -> FIRE
        WOOD -> METAL
    }

    fun relationTo(other: FiveElement): ElementRelation = when (other) {
        this -> ElementRelation.SAME
        generates() -> ElementRelation.GENERATES
        generatedBy() -> ElementRelation.GENERATED_BY
        restrains() -> ElementRelation.RESTRAINS
        else -> ElementRelation.RESTRAINED_BY
    }

    companion object {
        fun fromCn(s: String): FiveElement = entries.first { it.cn == s }
    }
}

enum class ElementRelation(val cn: String) {
    SAME("比和"),
    GENERATES("我生"),
    GENERATED_BY("生我"),
    RESTRAINS("我克"),
    RESTRAINED_BY("克我"),
}

enum class HeavenlyStem(val cn: String, val element: FiveElement, val yinYang: YinYang) {
    JIA("甲", FiveElement.WOOD, YinYang.YANG),
    YI("乙", FiveElement.WOOD, YinYang.YIN),
    BING("丙", FiveElement.FIRE, YinYang.YANG),
    DING("丁", FiveElement.FIRE, YinYang.YIN),
    WU("戊", FiveElement.EARTH, YinYang.YANG),
    JI("己", FiveElement.EARTH, YinYang.YIN),
    GENG("庚", FiveElement.METAL, YinYang.YANG),
    XIN("辛", FiveElement.METAL, YinYang.YIN),
    REN("壬", FiveElement.WATER, YinYang.YANG),
    GUI("癸", FiveElement.WATER, YinYang.YIN);

    val ordinalIndex: Int get() = ordinal

    companion object {
        fun fromCn(s: String): HeavenlyStem = entries.first { it.cn == s }
        fun fromIndex(i: Int): HeavenlyStem = entries[((i % 10) + 10) % 10]
    }
}

enum class EarthlyBranch(
    val cn: String,
    val element: FiveElement,
    val yinYang: YinYang,
) {
    ZI("子", FiveElement.WATER, YinYang.YANG),
    CHOU("丑", FiveElement.EARTH, YinYang.YIN),
    YIN("寅", FiveElement.WOOD, YinYang.YANG),
    MAO("卯", FiveElement.WOOD, YinYang.YIN),
    CHEN("辰", FiveElement.EARTH, YinYang.YANG),
    SI("巳", FiveElement.FIRE, YinYang.YIN),
    WU("午", FiveElement.FIRE, YinYang.YANG),
    WEI("未", FiveElement.EARTH, YinYang.YIN),
    SHEN("申", FiveElement.METAL, YinYang.YANG),
    YOU("酉", FiveElement.METAL, YinYang.YIN),
    XU("戌", FiveElement.EARTH, YinYang.YANG),
    HAI("亥", FiveElement.WATER, YinYang.YIN);

    val ordinalIndex: Int get() = ordinal

    fun clashWith(): EarthlyBranch = fromIndex(ordinal + 6)

    fun combineWith(): EarthlyBranch = when (this) {
        ZI -> CHOU
        CHOU -> ZI
        YIN -> HAI
        HAI -> YIN
        MAO -> XU
        XU -> MAO
        CHEN -> YOU
        YOU -> CHEN
        SI -> SHEN
        SHEN -> SI
        WU -> WEI
        WEI -> WU
    }

    fun relationsTo(other: EarthlyBranch): Set<BranchRelation> {
        val result = mutableSetOf<BranchRelation>()
        if (clashWith() == other) result += BranchRelation.CLASH
        if (combineWith() == other) result += BranchRelation.COMBINE
        if (isPunish(this, other)) result += BranchRelation.PUNISH
        if (isHarm(this, other)) result += BranchRelation.HARM
        return result
    }

    companion object {
        fun fromCn(s: String): EarthlyBranch = entries.first { it.cn == s }
        fun fromIndex(i: Int): EarthlyBranch = entries[((i % 12) + 12) % 12]

        fun isPunish(a: EarthlyBranch, b: EarthlyBranch): Boolean {
            val punishSets = listOf(setOf(YIN, SI, SHEN), setOf(CHOU, XU, WEI))
            val pair = setOf(a, b)
            if (pair == setOf(ZI, MAO)) return true
            if (a == b && a in listOf(CHEN, WU, YOU, HAI)) return true
            return punishSets.any { a in it && b in it && a != b }
        }

        fun isHarm(a: EarthlyBranch, b: EarthlyBranch): Boolean {
            val harmPairs = listOf(
                setOf(ZI, WEI),
                setOf(CHOU, WU),
                setOf(YIN, SI),
                setOf(MAO, CHEN),
                setOf(SHEN, HAI),
                setOf(YOU, XU),
            )
            return setOf(a, b) in harmPairs
        }
    }
}

enum class BranchRelation(val cn: String) {
    CLASH("冲"),
    COMBINE("合"),
    PUNISH("刑"),
    HARM("害"),
}

enum class SixKin(val cn: String) {
    PARENT("父母"),
    SIBLING("兄弟"),
    OFFICIAL("官鬼"),
    WEALTH("妻财"),
    OFFSPRING("子孙");

    companion object {
        fun of(palaceElement: FiveElement, lineElement: FiveElement): SixKin =
            when (palaceElement.relationTo(lineElement)) {
                ElementRelation.SAME -> SIBLING
                ElementRelation.GENERATES -> OFFSPRING
                ElementRelation.GENERATED_BY -> PARENT
                ElementRelation.RESTRAINS -> WEALTH
                ElementRelation.RESTRAINED_BY -> OFFICIAL
            }
    }
}

enum class SixGod(val cn: String) {
    AZURE_DRAGON("青龙"),
    VERMILION_BIRD("朱雀"),
    HOOK_EARTH("勾陈"),
    SOARING_SNAKE("腾蛇"),
    WHITE_TIGER("白虎"),
    BLACK_TORTOISE("玄武");

    companion object {
        fun sequenceByDayStem(dayStem: HeavenlyStem): List<SixGod> {
            val start = when (dayStem) {
                HeavenlyStem.JIA, HeavenlyStem.YI -> 0
                HeavenlyStem.BING, HeavenlyStem.DING -> 1
                HeavenlyStem.WU -> 2
                HeavenlyStem.JI -> 3
                HeavenlyStem.GENG, HeavenlyStem.XIN -> 4
                HeavenlyStem.REN, HeavenlyStem.GUI -> 5
            }
            return (0 until 6).map { entries[(start + it) % 6] }
        }
    }
}

enum class EightTrigram(
    val cn: String,
    val nature: String,
    val element: FiveElement,
    val lines: List<YinYang>,
) {
    QIAN("乾", "天", FiveElement.METAL, listOf(YinYang.YANG, YinYang.YANG, YinYang.YANG)),
    DUI("兑", "泽", FiveElement.METAL, listOf(YinYang.YANG, YinYang.YANG, YinYang.YIN)),
    LI("离", "火", FiveElement.FIRE, listOf(YinYang.YANG, YinYang.YIN, YinYang.YANG)),
    ZHEN("震", "雷", FiveElement.WOOD, listOf(YinYang.YANG, YinYang.YIN, YinYang.YIN)),
    XUN("巽", "风", FiveElement.WOOD, listOf(YinYang.YIN, YinYang.YANG, YinYang.YANG)),
    KAN("坎", "水", FiveElement.WATER, listOf(YinYang.YIN, YinYang.YANG, YinYang.YIN)),
    GEN("艮", "山", FiveElement.EARTH, listOf(YinYang.YIN, YinYang.YIN, YinYang.YANG)),
    KUN("坤", "地", FiveElement.EARTH, listOf(YinYang.YIN, YinYang.YIN, YinYang.YIN));

    companion object {
        fun fromCn(s: String): EightTrigram = entries.first { it.cn == s }
        fun fromLines(lines: List<YinYang>): EightTrigram = entries.first { it.lines == lines }
    }
}

enum class Palace(val cn: String, val element: FiveElement, val pureTrigram: EightTrigram) {
    QIAN_PALACE("乾宫", FiveElement.METAL, EightTrigram.QIAN),
    KAN_PALACE("坎宫", FiveElement.WATER, EightTrigram.KAN),
    GEN_PALACE("艮宫", FiveElement.EARTH, EightTrigram.GEN),
    ZHEN_PALACE("震宫", FiveElement.WOOD, EightTrigram.ZHEN),
    XUN_PALACE("巽宫", FiveElement.WOOD, EightTrigram.XUN),
    LI_PALACE("离宫", FiveElement.FIRE, EightTrigram.LI),
    KUN_PALACE("坤宫", FiveElement.EARTH, EightTrigram.KUN),
    DUI_PALACE("兑宫", FiveElement.METAL, EightTrigram.DUI);

    companion object {
        fun ofTrigram(t: EightTrigram): Palace = entries.first { it.pureTrigram == t }
    }
}

enum class StrengthLevel(val cn: String, val score: Int) {
    PROSPEROUS("旺", 5),
    STRONG("相", 4),
    RESTING("休", 3),
    TRAPPED("囚", 2),
    DEAD("死", 1);

    companion object {
        fun byMonth(monthElement: FiveElement, lineElement: FiveElement): StrengthLevel =
            when (lineElement.relationTo(monthElement)) {
                ElementRelation.SAME -> PROSPEROUS
                ElementRelation.GENERATED_BY -> STRONG
                ElementRelation.GENERATES -> RESTING
                ElementRelation.RESTRAINS -> TRAPPED
                ElementRelation.RESTRAINED_BY -> DEAD
            }
    }
}
