package com.liuyao.paipan.domain.analysis

import com.liuyao.paipan.domain.model.DivinationCategory
import com.liuyao.paipan.domain.model.EarthlyBranch
import com.liuyao.paipan.domain.model.FiveElement
import com.liuyao.paipan.domain.model.GanZhi
import com.liuyao.paipan.domain.model.HeavenlyStem
import com.liuyao.paipan.domain.model.Palace
import com.liuyao.paipan.domain.model.SixGod
import com.liuyao.paipan.domain.model.SixKin
import com.liuyao.paipan.domain.model.StrengthLevel
import com.liuyao.paipan.domain.model.YinYang

fun DivinationCategory.displayName(): String = when (this) {
    DivinationCategory.CAREER -> "工作"
    DivinationCategory.WEALTH -> "财运"
    DivinationCategory.MARRIAGE -> "婚姻"
    DivinationCategory.HEALTH -> "疾病"
    DivinationCategory.STUDY -> "考试"
    DivinationCategory.FAME -> "学业"
    DivinationCategory.LOST -> "失物"
    DivinationCategory.TRAVEL -> "行人"
    DivinationCategory.LAWSUIT -> "官事"
    DivinationCategory.PREGNANCY -> "孕产"
    DivinationCategory.HOUSE -> "房宅"
    DivinationCategory.COOPERATION -> "合作"
    DivinationCategory.FORTUNE -> "运势"
    DivinationCategory.OTHER -> "其他"
}

fun SixKin.displayName(): String = when (this) {
    SixKin.PARENT -> "父母"
    SixKin.SIBLING -> "兄弟"
    SixKin.OFFICIAL -> "官鬼"
    SixKin.WEALTH -> "妻财"
    SixKin.OFFSPRING -> "子孙"
}

fun SixGod.displayName(): String = when (this) {
    SixGod.AZURE_DRAGON -> "青龙"
    SixGod.VERMILION_BIRD -> "朱雀"
    SixGod.HOOK_EARTH -> "勾陈"
    SixGod.SOARING_SNAKE -> "腾蛇"
    SixGod.WHITE_TIGER -> "白虎"
    SixGod.BLACK_TORTOISE -> "玄武"
}

fun YinYang.displayName(): String = if (this == YinYang.YANG) "阳" else "阴"

fun EarthlyBranch.displayName(): String = when (this) {
    EarthlyBranch.ZI -> "子"
    EarthlyBranch.CHOU -> "丑"
    EarthlyBranch.YIN -> "寅"
    EarthlyBranch.MAO -> "卯"
    EarthlyBranch.CHEN -> "辰"
    EarthlyBranch.SI -> "巳"
    EarthlyBranch.WU -> "午"
    EarthlyBranch.WEI -> "未"
    EarthlyBranch.SHEN -> "申"
    EarthlyBranch.YOU -> "酉"
    EarthlyBranch.XU -> "戌"
    EarthlyBranch.HAI -> "亥"
}

fun FiveElement.displayName(): String = when (this) {
    FiveElement.WOOD -> "木"
    FiveElement.FIRE -> "火"
    FiveElement.EARTH -> "土"
    FiveElement.METAL -> "金"
    FiveElement.WATER -> "水"
}

fun StrengthLevel.displayName(): String = when (this) {
    StrengthLevel.PROSPEROUS -> "旺"
    StrengthLevel.STRONG -> "相"
    StrengthLevel.RESTING -> "休"
    StrengthLevel.TRAPPED -> "囚"
    StrengthLevel.DEAD -> "死"
}

fun HeavenlyStem.displayName(): String = when (this) {
    HeavenlyStem.JIA -> "甲"
    HeavenlyStem.YI -> "乙"
    HeavenlyStem.BING -> "丙"
    HeavenlyStem.DING -> "丁"
    HeavenlyStem.WU -> "戊"
    HeavenlyStem.JI -> "己"
    HeavenlyStem.GENG -> "庚"
    HeavenlyStem.XIN -> "辛"
    HeavenlyStem.REN -> "壬"
    HeavenlyStem.GUI -> "癸"
}

fun GanZhi.displayName(): String = stem.displayName() + branch.displayName()

fun Palace.displayName(): String = when (this) {
    Palace.QIAN_PALACE -> "乾宫"
    Palace.KAN_PALACE -> "坎宫"
    Palace.GEN_PALACE -> "艮宫"
    Palace.ZHEN_PALACE -> "震宫"
    Palace.XUN_PALACE -> "巽宫"
    Palace.LI_PALACE -> "离宫"
    Palace.KUN_PALACE -> "坤宫"
    Palace.DUI_PALACE -> "兑宫"
}
