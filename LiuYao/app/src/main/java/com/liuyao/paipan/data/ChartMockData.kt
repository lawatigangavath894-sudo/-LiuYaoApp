package com.liuyao.paipan.data

/**
 * 排盘页(高保真静态 UI)专用 mock 数据。
 *
 * 与 [MockData] 分离:本文件只服务 ChartScreen 及其子组件,字段更完整。
 * 后续阶段接入排盘引擎后,这里整体由 PaiPanResult → UI 映射层取代。
 *
 * 全部为纯静态展示数据,不含任何排盘计算。
 */

/** 五行枚举,供小字/小圆点取色 */
enum class WuXing { WOOD, FIRE, EARTH, METAL, WATER }

fun wuXingOf(branch: String): WuXing = when (branch) {
    "亥", "子" -> WuXing.WATER
    "寅", "卯" -> WuXing.WOOD
    "巳", "午" -> WuXing.FIRE
    "申", "酉" -> WuXing.METAL
    else -> WuXing.EARTH // 辰戌丑未
}

/** 一行爻的完整静态信息 */
data class YaoLineData(
    val position: Int,            // 6=上爻 … 1=初爻
    val liuShen: String,          // 六神
    val liuQin: String,           // 六亲
    val ganZhi: String,           // 纳甲干支,如 "壬戌"
    val branch: String,           // 地支(用于五行取色与显示)
    val element: String,          // 五行单字:金木水火土
    val yang: Boolean,            // 阳爻/阴爻(画爻象用)
    val isWorld: Boolean,
    val isResponse: Boolean,
    val moving: Boolean,
    val changedLiuQin: String?,   // 变爻六亲
    val changedGanZhi: String?,   // 变爻纳甲
    val fuShen: String?,          // 伏神(六亲+地支),如 "妻财卯木"
    val feiShen: String?,         // 飞神(伏神所伏之飞爻),如 "兄弟酉金"
    val prosperity: String,       // 旺衰:旺/相/休/囚/死
    val relations: List<YaoTag>,  // 空破冲合刑害等关系标签
)

/** 爻行上的关系标签类型(决定 Badge 取色) */
enum class YaoTag(val label: String) {
    EMPTY("空"),    // 旬空
    BREAK("破"),    // 月破
    DAY_CLASH("日冲"),
    CLASH("冲"),
    COMBINE("合"),
    PUNISH("刑"),
    HARM("害"),
    HIDDEN("伏"),   // 有伏神
}

/** 断语预览卡数据 */
data class RulePreview(
    val id: String,
    val source: String,       // 来源
    val category: String,     // 占类
    val original: String,     // 原始断语
    val plain: String,        // 白话解释
    val condition: String,    // 命中条件
    val score: Int,           // 匹配分数 0–100
)

/** 反馈面板数据 */
data class FeedbackData(
    val result: String,       // 最终结果
    val time: String,         // 反馈时间
    val note: String,         // 备注
    val hitRules: List<String>,   // 验中断语
    val missRules: List<String>,  // 误判断语
)

object ChartMockData {

    // ───── 顶部:占事 + 时间卡 + 卦象卡 ─────
    const val question = "文章投此期刊能否录用"

    const val gregorian = "公历 2026-05-22  14:09"
    const val lunar = "农历 丙午年 四月初六 未时"
    const val ganZhiYear = "丙午"
    const val ganZhiMonth = "癸巳"
    const val ganZhiDay = "丙申"
    const val ganZhiHour = "乙未"
    const val xunKong = "辰、巳"
    const val castMethod = "正时起卦"

    const val benHex = "乾为天"
    const val bianHex = "天风姤"
    const val palace = "乾宫"
    const val palaceElement = "金"
    const val hexNature = "六冲"        // 六冲/六合/无
    const val worldResponse = "世六爻 · 应三爻"

    // ───── 六爻盘:上爻→初爻 ─────
    // 盘面取「乾为天 → 天风姤」(初爻动),父母为用神场景。
    val yaoLines: List<YaoLineData> = listOf(
        YaoLineData(
            position = 6, liuShen = "青龙", liuQin = "父母", ganZhi = "壬戌", branch = "戌", element = "土",
            yang = true, isWorld = true, isResponse = false, moving = false,
            changedLiuQin = null, changedGanZhi = null, fuShen = null, feiShen = null,
            prosperity = "相", relations = emptyList(),
        ),
        YaoLineData(
            position = 5, liuShen = "玄武", liuQin = "兄弟", ganZhi = "壬申", branch = "申", element = "金",
            yang = true, isWorld = false, isResponse = false, moving = false,
            changedLiuQin = null, changedGanZhi = null, fuShen = null, feiShen = null,
            prosperity = "旺", relations = listOf(YaoTag.DAY_CLASH),
        ),
        YaoLineData(
            position = 4, liuShen = "白虎", liuQin = "官鬼", ganZhi = "壬午", branch = "午", element = "火",
            yang = true, isWorld = false, isResponse = false, moving = false,
            changedLiuQin = null, changedGanZhi = null, fuShen = null, feiShen = null,
            prosperity = "休", relations = emptyList(),
        ),
        YaoLineData(
            position = 3, liuShen = "螣蛇", liuQin = "父母", ganZhi = "甲辰", branch = "辰", element = "土",
            yang = true, isWorld = false, isResponse = true, moving = false,
            changedLiuQin = null, changedGanZhi = null, fuShen = null, feiShen = null,
            prosperity = "相", relations = listOf(YaoTag.EMPTY),
        ),
        YaoLineData(
            position = 2, liuShen = "勾陈", liuQin = "妻财", ganZhi = "甲寅", branch = "寅", element = "木",
            yang = true, isWorld = false, isResponse = false, moving = false,
            changedLiuQin = null, changedGanZhi = null, fuShen = "子孙亥水", feiShen = "妻财寅木",
            prosperity = "囚", relations = listOf(YaoTag.HIDDEN),
        ),
        YaoLineData(
            position = 1, liuShen = "朱雀", liuQin = "子孙", ganZhi = "甲子", branch = "子", element = "水",
            yang = true, isWorld = false, isResponse = false, moving = true,
            changedLiuQin = "父母", changedGanZhi = "辛丑", fuShen = null, feiShen = null,
            prosperity = "相", relations = listOf(YaoTag.COMBINE),
        ),
    )

    // ───── 分析 Tab ─────
    val analysisTabs = listOf("神煞", "旺衰", "批注", "案例", "占法", "取象", "断语", "反馈")

    val shenSha = listOf(
        "贵人" to "酉、亥", "驿马" to "寅", "桃花" to "酉",
        "禄神" to "巳", "华盖" to "辰", "将星" to "子", "天医" to "辰",
    )

    val prosperityNotes = listOf(
        "兄弟申金临日建,旺相得令,劫财之力强。",
        "父母戌土持世得月生为相,根气尚可。",
        "用神父母辰土临应而旬空,待出空之日方可论吉凶。",
        "官鬼午火休于巳月,生世之力有限。",
    )

    const val annotation =
        "此卦六冲,初爻子水发动化丑土回头合,世临父母戌土。父母两现,用神取临应之辰土," +
            "辰土旬空待填。官鬼午火为文章录用之关键,休而不旺,生世乏力。"

    val relatedCases = listOf(
        "投稿核心期刊能否录用 — 验中" to "5 月 12 日",
        "论文返修后能否过审 — 部分验中" to "4 月 30 日",
    )

    val zhanFa = listOf(
        "以父母爻为用神(文章、文书、录用通知)。",
        "官鬼为审稿、为录用之权,宜生扶用神。",
        "子孙为忌神,克官鬼,主退稿、不录用。",
        "世为求测人,应为期刊编辑部。",
    )

    val quXiang = listOf(
        "父母辰土旬空 → 文书一时未定,结果待出空。",
        "初爻子孙发动 → 有不利录用之象,但化丑土合住,凶象被牵制。",
        "官鬼午火生世 → 编辑对作者本人尚有认可。",
        "六冲之卦 → 事多反复,难一次定局。",
    )

    val rulePreviews = listOf(
        RulePreview(
            id = "rp1",
            source = "刘昌明《象断六爻》· 文书类",
            category = "求名 / 文书",
            original = "用神临应又逢旬空,事在两可,出空之日方有定音。",
            plain = "代表录用的父母爻落在对方位置且旬空,说明结果暂未敲定,要等旬空过去那天才见分晓。",
            condition = "父母为用神 · 临应爻 · 旬空",
            score = 86,
        ),
        RulePreview(
            id = "rp2",
            source = "刘昌明《象断六爻》· 忌神类",
            category = "求名",
            original = "子孙发动本克官,化合绊住,凶不全凶。",
            plain = "子孙动本是退稿之兆,但它变出的爻与它相合、被牵制住,所以不利因素被削弱,未必落空。",
            condition = "子孙发动 · 化爻六合 · 回头牵绊",
            score = 73,
        ),
    )

    val feedback = FeedbackData(
        result = "待验证",
        time = "—",
        note = "等待编辑部回复;预计出空日(辰/巳日)前后见结果。",
        hitRules = emptyList(),
        missRules = emptyList(),
    )
}
