package com.liuyao.paipan.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.liuyao.paipan.data.WuXing
import com.liuyao.paipan.data.YaoTag

/** 五行 → 语义色(随主题切换) */
@Composable
@ReadOnlyComposable
fun wuXingColor(w: WuXing): Color = with(AppTheme.colors) {
    when (w) {
        WuXing.WOOD -> wood
        WuXing.FIRE -> fire
        WuXing.EARTH -> earth
        WuXing.METAL -> metal
        WuXing.WATER -> water
    }
}

/** 五行单字 → 语义色 */
@Composable
@ReadOnlyComposable
fun elementColor(element: String): Color = with(AppTheme.colors) {
    when (element) {
        "木" -> wood
        "火" -> fire
        "土" -> earth
        "金" -> metal
        "水" -> water
        else -> secondaryLabel
    }
}

/** 爻关系标签 → (淡底, 前景)。低饱和,仅用于小 Badge。 */
@Composable
@ReadOnlyComposable
fun tagColors(tag: YaoTag): Pair<Color, Color> = with(AppTheme.colors) {
    val fg = when (tag) {
        YaoTag.EMPTY -> empty
        YaoTag.BREAK -> breakState
        YaoTag.DAY_CLASH, YaoTag.CLASH -> clash
        YaoTag.COMBINE -> combine
        YaoTag.PUNISH -> breakState
        YaoTag.HARM -> clash
        YaoTag.HIDDEN -> secondaryLabel
    }
    val alpha = if (tag == YaoTag.EMPTY || tag == YaoTag.HIDDEN) 0.14f else 0.12f
    fg.copy(alpha = alpha) to fg
}
