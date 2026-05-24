package com.liuyao.paipan.ui.theme

import androidx.compose.ui.unit.dp

/**
 * 间距 / 圆角 / 尺寸系统 — 设计系统冻结层。
 * 全部为常量 token,组件只引用这里,不写魔法数字。
 */
object Spacing {
    // 基础步进(4 的倍数)
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp

    /** 页面左右安全边距 */
    val pageHorizontal = 16.dp
    /** Section 之间的纵向间距 */
    val sectionGap = 16.dp
    /** Section 头/脚文字与卡片的间距 */
    val sectionHeaderGap = 6.dp
    /** 卡片内边距(14–18 取 16) */
    val cardPadding = 16.dp
    /** 列表行水平内距 */
    val rowHorizontal = 16.dp
    /** 列表行纵向内距 */
    val rowVertical = 12.dp
}

object Radius {
    /** 卡片 / 分组容器圆角(18–24 取 20) */
    val card = 20.dp
    /** sheet 顶部圆角 */
    val sheet = 24.dp
    /** 大按钮圆角 */
    val button = 14.dp
    /** Segmented 外框圆角 */
    val segmentOuter = 10.dp
    /** Segmented 选中药丸圆角 */
    val segmentPill = 8.dp
    /** Badge 全圆(胶囊) */
    val badge = 999.dp
    /** 小元件圆角 */
    val small = 8.dp
}

object Sizes {
    /** 底部主操作按钮高度(50–56 取 52) */
    val primaryButtonHeight = 52.dp
    /** 次按钮高度 */
    val secondaryButtonHeight = 44.dp
    /** 列表行最小高度(48–56 取 48) */
    val listRowMinHeight = 48.dp
    /** 导航栏高度(不含状态栏) */
    val navBarHeight = 44.dp
    /** 分割线粗细 */
    val hairline = 0.5.dp
    /** sheet 顶部 grabber 尺寸 */
    val sheetGrabberWidth = 36.dp
    val sheetGrabberHeight = 5.dp
    /** 五行/状态小圆点直径 */
    val dot = 8.dp
}
