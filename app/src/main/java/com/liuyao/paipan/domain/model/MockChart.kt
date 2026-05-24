package com.liuyao.paipan.domain.model

import java.time.LocalDateTime

/**
 * 领域模型示例数据 —— 仅供 UI 预览 / 单元测试,不参与真实排盘。
 *
 * [mockChart] 取「乾为天 → 天风姤,巳月丙申日,初爻动」。
 * 其纳甲、六亲、六神、旺衰、旬空、世应均经独立核对,自洽可验。
 * 乾宫首卦六亲俱全,故主卦各爻无伏神;伏神/飞神结构的演示见 [mockHiddenSpiritExample]。
 */

private fun gz(s: String) = GanZhi.fromCn(s)

fun mockChart(): LiuYaoChart {
    val palace = Palace.QIAN_PALACE
    val original = Hexagram.of(lowerCn = "乾", upperCn = "乾") // 内乾外乾 = 乾为天
    val changed = original.transform(setOf(1))                 // 初爻动 → 天风姤

    // index0 = 初爻。纳甲:内甲子寅辰,外壬午申戌。六神丙日起朱雀。
    val lines = listOf(
        YaoLine(
            index = 1, yinYang = YinYang.YANG, isMoving = true,
            sixGod = SixGod.VERMILION_BIRD, sixKin = SixKin.OFFSPRING,
            naJia = gz("甲子"), element = FiveElement.WATER,
            isWorld = false, isResponse = false,
            changedLine = ChangedLine(YinYang.YIN, SixKin.PARENT, gz("辛丑")),
            status = LineStatus(
                isCombined = true, // 子丑合(化爻)
                strength = StrengthLevel.TRAPPED,
            ),
        ),
        YaoLine(
            index = 2, yinYang = YinYang.YANG, isMoving = false,
            sixGod = SixGod.HOOK_EARTH, sixKin = SixKin.WEALTH,
            naJia = gz("甲寅"), element = FiveElement.WOOD,
            isWorld = false, isResponse = false,
            status = LineStatus(strength = StrengthLevel.RESTING),
        ),
        YaoLine(
            index = 3, yinYang = YinYang.YANG, isMoving = false,
            sixGod = SixGod.SOARING_SNAKE, sixKin = SixKin.PARENT,
            naJia = gz("甲辰"), element = FiveElement.EARTH,
            isWorld = false, isResponse = true,
            status = LineStatus(isVoid = true, strength = StrengthLevel.STRONG), // 辰旬空
        ),
        YaoLine(
            index = 4, yinYang = YinYang.YANG, isMoving = false,
            sixGod = SixGod.WHITE_TIGER, sixKin = SixKin.OFFICIAL,
            naJia = gz("壬午"), element = FiveElement.FIRE,
            isWorld = false, isResponse = false,
            status = LineStatus(strength = StrengthLevel.PROSPEROUS),
        ),
        YaoLine(
            index = 5, yinYang = YinYang.YANG, isMoving = false,
            sixGod = SixGod.BLACK_TORTOISE, sixKin = SixKin.SIBLING,
            naJia = gz("壬申"), element = FiveElement.METAL,
            isWorld = false, isResponse = false,
            status = LineStatus(
                isDayClashed = true,  // 日辰申…(示意:与日支同气,实际冲否由引擎判,此处仅演示字段)
                isSupportedByDay = true,
                strength = StrengthLevel.DEAD,
            ),
        ),
        YaoLine(
            index = 6, yinYang = YinYang.YANG, isMoving = false,
            sixGod = SixGod.AZURE_DRAGON, sixKin = SixKin.PARENT,
            naJia = gz("壬戌"), element = FiveElement.EARTH,
            isWorld = true, isResponse = false,
            status = LineStatus(isSupportedByMonth = true, strength = StrengthLevel.STRONG),
        ),
    )

    return LiuYaoChart(
        id = "mock-qian-2026-05-22",
        question = "文章投此期刊能否录用",
        category = DivinationCategory.FAME,
        dateTime = LocalDateTime.of(2026, 5, 22, 14, 9),
        yearGanZhi = gz("丙午"),
        monthGanZhi = gz("癸巳"),
        dayGanZhi = gz("丙申"),
        hourGanZhi = gz("乙未"),
        xunKong = listOf(EarthlyBranch.CHEN, EarthlyBranch.SI), // 辰、巳
        originalHexagram = original,
        changedHexagram = changed,
        palace = palace,
        isSixClash = original.isSixClash,   // 乾为天为六冲
        isSixCombine = original.isSixCombine,
        lines = lines,
        worldLineIndex = 6,
        responseLineIndex = 3,
        method = DivinationMethod.SolarTime,
        notes = listOf(
            "乾为天六冲,初爻子水发动化丑土回头合。",
            "用神取父母,辰土临应旬空,待出空之日见分晓。",
        ),
    )
}

/**
 * 伏神 / 飞神结构演示。
 * 取一处假想:某爻本宫缺"妻财",妻财卯木伏于飞神兄弟酉金之下。
 * 仅用于 UI 渲染伏飞神栏位的测试,不代表上面乾卦的真实排盘。
 */
val mockHiddenSpiritExample: YaoLine = YaoLine(
    index = 2, yinYang = YinYang.YANG, isMoving = false,
    sixGod = SixGod.HOOK_EARTH, sixKin = SixKin.SIBLING,
    naJia = gz("辛酉"), element = FiveElement.METAL,
    isWorld = false, isResponse = false,
    hiddenSpirit = HiddenSpirit(SixKin.WEALTH, gz("乙卯")),
    flyingSpirit = FlyingSpirit(SixKin.SIBLING, gz("辛酉")),
    status = LineStatus(strength = StrengthLevel.PROSPEROUS),
)
