package com.liuyao.paipan.domain.model

/**
 * 六爻排盘 · 基础领域枚举与关系。
 *
 * 设计原则:
 *  - 纯 Kotlin,无任何 Android / Compose 依赖,可在 JVM 单元测试中直接验证;
 *  - 枚举自带最小必要属性(如五行、阴阳),关系判断以纯函数/伴生方法给出;
 *  - 留出扩展点(如 [EarthlyBranch.hidden] 藏干、[FiveElement] 生克查询)。
 *
 * 本文件只定义结构与确定性的"静态属性/关系",不做排盘流程计算。
 */

// ════════════════════════════════════════════════════════════════════
// 4. 阴阳 YinYang
// ════════════════════════════════════════════════════════════════════

enum class YinYang {
    YANG, // 阳 —
    YIN;  // 阴 - -

    val symbol: String get() = if (this == YANG) "—" else "- -"
    fun flip(): YinYang = if (this == YANG) YIN else YANG
}

// ════════════════════════════════════════════════════════════════════
// 3. 五行 FiveElement
// ════════════════════════════════════════════════════════════════════

enum class FiveElement(val cn: String) {
    WOOD("木"), FIRE("火"), EARTH("土"), METAL("金"), WATER("水");

    /** 我生者(子) */
    fun generates(): FiveElement = when (this) {
        WOOD -> FIRE; FIRE -> EARTH; EARTH -> METAL; METAL -> WATER; WATER -> WOOD
    }

    /** 生我者(父母) */
    fun generatedBy(): FiveElement = when (this) {
        FIRE -> WOOD; EARTH -> FIRE; METAL -> EARTH; WATER -> METAL; WOOD -> WATER
    }

    /** 我克者(财) */
    fun restrains(): FiveElement = when (this) {
        WOOD -> EARTH; EARTH -> WATER; WATER -> FIRE; FIRE -> METAL; METAL -> WOOD
    }

    /** 克我者(官鬼) */
    fun restrainedBy(): FiveElement = when (this) {
        EARTH -> WOOD; WATER -> EARTH; FIRE -> WATER; METAL -> FIRE; WOOD -> METAL
    }

    /** 与另一五行的关系(以本方为主语) */
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

// ════════════════════════════════════════════════════════════════════
// 21. 五行关系 ElementRelation
// ════════════════════════════════════════════════════════════════════

/** 以"主方 → 客方"为视角的五行关系 */
enum class ElementRelation(val cn: String) {
    SAME("比和"),
    GENERATES("我生"),
    GENERATED_BY("生我"),
    RESTRAINS("我克"),
    RESTRAINED_BY("克我"),
}

// ════════════════════════════════════════════════════════════════════
// 1. 天干 HeavenlyStem
// ════════════════════════════════════════════════════════════════════

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

    /** 0..9 序号 */
    val ordinalIndex: Int get() = ordinal

    companion object {
        fun fromCn(s: String): HeavenlyStem = entries.first { it.cn == s }
        fun fromIndex(i: Int): HeavenlyStem = entries[((i % 10) + 10) % 10]
    }
}

// ════════════════════════════════════════════════════════════════════
// 2. 地支 EarthlyBranch
// ════════════════════════════════════════════════════════════════════

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

    /** 相冲地支(对冲位,+6) */
    fun clashWith(): EarthlyBranch = fromIndex(ordinal + 6)

    /** 六合地支 */
    fun combineWith(): EarthlyBranch = when (this) {
        ZI -> CHOU; CHOU -> ZI; YIN -> HAI; HAI -> YIN; MAO -> XU; XU -> MAO
        CHEN -> YOU; YOU -> CHEN; SI -> SHEN; SHEN -> SI; WU -> WEI; WEI -> WU
    }

    /** 与另一地支的关系集合(可同时成立多种,如又冲又刑) */
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

        /** 相刑(含三刑/自刑的常用判定) */
        fun isPunish(a: EarthlyBranch, b: EarthlyBranch): Boolean {
            val punishSets = listOf(
                setOf(YIN, SI, SHEN),   // 寅巳申
                setOf(CHOU, XU, WEI),   // 丑戌未
            )
            val pair = setOf(a, b)
            // 子卯相刑
            if (pair == setOf(ZI, MAO)) return true
            // 自刑:辰午酉亥
            if (a == b && a in listOf(CHEN, WU, YOU, HAI)) return true
            // 三刑组内任意两者
            return punishSets.any { a in it && b in it && a != b }
        }

        /** 六害 */
        fun isHarm(a: EarthlyBranch, b: EarthlyBranch): Boolean {
            val harmPairs = listOf(
                setOf(ZI, WEI), setOf(CHOU, WU), setOf(YIN, SI),
                setOf(MAO, CHEN), setOf(SHEN, HAI), setOf(YOU, XU),
            )
            return setOf(a, b) in harmPairs
        }
    }
}

// ════════════════════════════════════════════════════════════════════
// 20. 地支关系 BranchRelation
// ════════════════════════════════════════════════════════════════════

enum class BranchRelation(val cn: String) {
    CLASH("冲"),
    COMBINE("合"),
    PUNISH("刑"),
    HARM("害"),
}

// ════════════════════════════════════════════════════════════════════
// 5. 六亲 SixKin
// ════════════════════════════════════════════════════════════════════

enum class SixKin(val cn: String) {
    PARENT("父母"),
    SIBLING("兄弟"),
    OFFICIAL("官鬼"),
    WEALTH("妻财"),
    OFFSPRING("子孙");

    companion object {
        /**
         * 由"卦宫五行(我)"与"爻五行(他)"定六亲。
         *  生我者父母,克我者官鬼,我克者妻财,我生者子孙,比和者兄弟。
         */
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

// ════════════════════════════════════════════════════════════════════
// 6. 六神 SixGod
// ════════════════════════════════════════════════════════════════════

enum class SixGod(val cn: String) {
    AZURE_DRAGON("青龙"),
    VERMILION_BIRD("朱雀"),
    HOOK_EARTH("勾陈"),
    SOARING_SNAKE("螣蛇"),
    WHITE_TIGER("白虎"),
    BLACK_TORTOISE("玄武");

    companion object {
        /**
         * 由日干起六神,自初爻而上排六位。
         *  甲乙起青龙、丙丁起朱雀、戊起勾陈、己起螣蛇、庚辛起白虎、壬癸起玄武。
         * @return 长度 6 的列表,index0 = 初爻。
         */
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

// ════════════════════════════════════════════════════════════════════
// 7. 八卦 EightTrigram
// ════════════════════════════════════════════════════════════════════

/**
 * 八经卦。[lines] 为三爻阴阳,index0 = 初爻(下)。
 */
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
        fun fromLines(lines: List<YinYang>): EightTrigram =
            entries.first { it.lines == lines }
    }
}

// ════════════════════════════════════════════════════════════════════
// 9. 卦宫 Palace
// ════════════════════════════════════════════════════════════════════

/**
 * 八宫。[element] 决定六亲取用的"我方五行"。
 */
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

// ════════════════════════════════════════════════════════════════════
// 19. 旺衰等级 StrengthLevel
// ════════════════════════════════════════════════════════════════════

/**
 * 以月令为主的旺衰五档(旺/相/休/囚/死)。
 * 预留 [score] 供后续做强弱量化与断语权重。
 */
enum class StrengthLevel(val cn: String, val score: Int) {
    PROSPEROUS("旺", 5),
    STRONG("相", 4),
    RESTING("休", 3),
    TRAPPED("囚", 2),
    DEAD("死", 1);

    companion object {
        /** 由月令五行与爻五行定旺衰 */
        fun byMonth(monthElement: FiveElement, lineElement: FiveElement): StrengthLevel =
            when (lineElement.relationTo(monthElement)) {
                ElementRelation.SAME -> PROSPEROUS        // 当令者旺
                ElementRelation.GENERATED_BY -> STRONG    // 月生爻为相
                ElementRelation.GENERATES -> RESTING      // 爻生月为休
                ElementRelation.RESTRAINS -> TRAPPED      // 爻克月为囚
                ElementRelation.RESTRAINED_BY -> DEAD     // 月克爻为死
            }
    }
}
