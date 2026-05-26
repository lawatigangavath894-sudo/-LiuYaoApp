package com.liuyao.paipan.domain.model

import java.time.LocalDateTime

/**
 * 17. 璧峰崷鏂瑰紡 DivinationMethod
 * 鐢?sealed 浠ヤ究涓嶅悓鏂瑰紡鎼哄甫鍚勮嚜鐨勮捣鍗﹀師濮嬭緭鍏?鎵╁睍鐐?銆?
 */
sealed class DivinationMethod(val cn: String) {
    /** 姝ｆ椂(浠ヨ捣鍗︽椂闂村洓鏌? */
    data object SolarTime : DivinationMethod("正时")

    /** 閫夋嫨鏃堕棿璧峰崷(鐢ㄦ埛鎵嬪姩鎸囧畾鐨勫叕鍘嗘椂闂?姊呰姳鏃堕棿璧峰崷) */
    data object SelectedTime : DivinationMethod("选择时间")

    /** 閾滈挶鎽囧崷:鍏,姣忔璁?2/3 涓儗闈?鑳?闃撮潰),鍙敱姝ゆ帹鑰侀槾灏戦槼绛?*/
    data class Coin(val tosses: List<Int>) : DivinationMethod("閾滈挶")

    /** 鎵嬪姩鎸囧畾鍏埢鑰佸皯闃撮槼 */
    data class Manual(val raw: String) : DivinationMethod("手动")

}

/**
 * 鍗犵被 DivinationCategory 鈥斺€?鐢ㄤ簬鏂鍖归厤鐨勪簨椤瑰垎绫汇€?
 * 璁句负鏋氫妇浣嗕繚鐣?[OTHER] 鍏滃簳,鍚庣画鍙钩婊戞墿鍏呫€?
 */
enum class DivinationCategory(val cn: String) {
    CAREER("浜嬩笟姹傝亴"),
    WEALTH("璐㈣繍"),
    MARRIAGE("濠氬Щ鎰熸儏"),
    HEALTH("鐤剧梾鍋ュ悍"),
    STUDY("瀛︿笟鑰冭瘯"),
    FAME("姹傚悕鏂囦功"),
    LOST("瀵荤墿澶辩墿"),
    TRAVEL("鍑鸿"),
    LAWSUIT("瀹橀潪璇夎"),
    PREGNANCY("瀛曚骇"),
    HOUSE("鎴垮畢"),
    COOPERATION("鍚堜綔"),
    FORTUNE("杩愬娍"),
    OTHER("鍏朵粬");

    companion object {
        /** 瀹夊叏瑙ｆ瀽:闈炴硶/鑴忔暟鎹洖閫€鍒?OTHER,閬垮厤宕╂簝 */
        fun fromName(name: String?): DivinationCategory =
            entries.firstOrNull { it.name == name } ?: OTHER

        /** 瀹夊叏瑙ｆ瀽涓轰腑鏂囧悕(渚?UI 鐩存帴灞曠ず) */
        fun cnOf(name: String?): String = fromName(name).cn
    }
}

/**
 * 18. 鍗犱簨淇℃伅 ChartQuestion 鈥斺€?缁撴瀯鍖栧崰浜?鍙€変娇鐢?銆?
 * [LiuYaoChart.question] 淇濈暀绾枃鏈互璐村悎棰樼洰绛惧悕;闇€瑕佺粨鏋勫寲鏃剁敤鏈被鎵胯浇銆?
 */
data class ChartQuestion(
    val text: String,
    val category: DivinationCategory? = null,
    val askerNote: String? = null,
)

/**
 * 12. 鎺掔洏鎬绘ā鍨?LiuYaoChart銆?
 *
 * 瀛楁涓ユ牸瀵瑰簲棰樼洰瑕佹眰;[lines] 绾﹀畾 index0 = 鍒濈埢銆?
 * 涓?搴斾互"鐖诲簭 1..6"璁板綍浜?[worldLineIndex]/[responseLineIndex]銆?
 */
data class LiuYaoChart(
    val id: String,
    val question: String,
    val category: DivinationCategory?,
    val dateTime: LocalDateTime,
    val yearGanZhi: GanZhi,
    val monthGanZhi: GanZhi,
    val dayGanZhi: GanZhi,
    val hourGanZhi: GanZhi,
    val xunKong: List<EarthlyBranch>,
    val originalHexagram: Hexagram,
    val changedHexagram: Hexagram?,
    val palace: Palace,
    val isSixClash: Boolean,
    val isSixCombine: Boolean,
    val lines: List<YaoLine>,
    val worldLineIndex: Int,    // 1..6
    val responseLineIndex: Int, // 1..6
    val method: DivinationMethod,
    val notes: List<String>,
) {
    /** 涓栫埢(鎸?1..6 鍙?index 瀵瑰簲鐖?lines 浠?index0=鍒濈埢瀛樺偍) */
    val worldLine: YaoLine get() = lines.first { it.index == worldLineIndex }
    val responseLine: YaoLine get() = lines.first { it.index == responseLineIndex }

    /** 鍔ㄧ埢鍒楄〃 */
    val movingLines: List<YaoLine> get() = lines.filter { it.isMoving }

    /** 鏈堜护鍦版敮(渚夸簬鏃鸿“/鍏崇郴璁＄畻) */
    val monthBranch: EarthlyBranch get() = monthGanZhi.branch
    val dayBranch: EarthlyBranch get() = dayGanZhi.branch

    /** 鎸夋煇鍏翰鍙栫埢(鍙兘澶氱幇) */
    fun linesOf(kin: SixKin): List<YaoLine> = lines.filter { it.sixKin == kin }
}
