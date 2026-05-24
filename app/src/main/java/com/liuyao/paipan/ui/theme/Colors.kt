package com.liuyao.paipan.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 颜色系统(iOS 风) — 设计系统冻结层。
 *
 * 命名约定:
 *  - 原始色值以 `Raw` 结尾或直接给出 Light/Dark 两套;
 *  - 对外消费请优先使用 [AppColors] 语义 token(经由 [LocalAppColors] 获取),
 *    而非直接引用这里的裸色值,便于浅/深色切换。
 *
 * 五行色与状态色均为低饱和,仅用于小字、Badge、小圆点,不做大面积铺色。
 */

// ────────────────────────────── 系统背景 ──────────────────────────────
val SystemBackgroundLight = Color(0xFFF2F2F7)
val SystemBackgroundDark = Color(0xFF000000)
/** 分组列表中"卡片之下"的更深一层背景(深色用 1C1C1E 作 base) */
val GroupedBaseDark = Color(0xFF1C1C1E)

// ────────────────────────────── 卡片背景 ──────────────────────────────
val CardLight = Color(0xFFFFFFFF)
val CardDark = Color(0xFF1C1C1E)
/** 卡内再上浮一层(如 sheet handle 区、嵌套卡)*/
val CardElevatedDark = Color(0xFF2C2C2E)

// ────────────────────────────── 主色 ──────────────────────────────
/** iOS Blue,略收敛饱和度,避免刺眼 */
val AccentLight = Color(0xFF0A6CFF)
val AccentDark = Color(0xFF0A84FF)
/** 主色淡底(Badge / 选中底色) */
val AccentSoftLight = Color(0x1A0A6CFF)
val AccentSoftDark = Color(0x290A84FF)

// ────────────────────────────── 文本 ──────────────────────────────
// iOS label 透明度阶:primary 实色,secondary 60%,tertiary 30%,quaternary/disabled 18%
val LabelLight = Color(0xFF1C1C1E)
val SecondaryLabelLight = Color(0x993C3C43)
val TertiaryLabelLight = Color(0x4D3C3C43)
val DisabledLabelLight = Color(0x2E3C3C43)

val LabelDark = Color(0xFFFFFFFF)
val SecondaryLabelDark = Color(0x99EBEBF5)
val TertiaryLabelDark = Color(0x4DEBEBF5)
val DisabledLabelDark = Color(0x2EEBEBF5)

// ────────────────────────────── 分割线 ──────────────────────────────
val SeparatorLight = Color(0x1F3C3C43) // grouped list 内缩发丝线
val SeparatorDark = Color(0x40545458)

// ────────────────────────────── 五行色(低饱和) ──────────────────────────────
// 仅用于小字 / Badge / 小圆点
val WoodColor = Color(0xFF4F8A5B)  // 木:沉静绿
val FireColor = Color(0xFFC25B4E)  // 火:砖红
val EarthColor = Color(0xFFA6814C) // 土:赭黄
val MetalColor = Color(0xFFB08A3E) // 金:暗金(偏区别于土,稍冷)
val WaterColor = Color(0xFF3F73A6) // 水:墨蓝

// ────────────────────────────── 状态色 ──────────────────────────────
// 六爻盘标记:动/世/应/空/破/合/冲。皆低饱和,配淡底使用。
val MovingColor = Color(0xFFB44A3A) // 动爻:朱砂
val WorldColor = Color(0xFF2E7D5B)  // 世:墨绿
val ResponseColor = Color(0xFF3F73A6) // 应:墨蓝
val EmptyColor = Color(0xFF8A8A8E)  // 空(旬空):中性灰
val BreakColor = Color(0xFF9B5BA0)  // 破(月破):暗紫
val CombineColor = Color(0xFFB07A2E) // 合:琥珀
val ClashColor = Color(0xFFC25B4E)  // 冲:砖红

/** 状态/五行 Badge 的淡底:统一取 12% alpha */
fun Color.asSoftContainer(alpha: Float = 0.12f): Color = copy(alpha = alpha)

// ────────────────────────────── 语义 token 容器 ──────────────────────────────

/**
 * 一组随主题切换的语义颜色。通过 [LocalAppColors] 提供给整棵 Compose 树。
 * 业务层只认这些语义名,不直接碰上面的裸色值。
 */
data class AppColors(
    val systemBackground: Color,
    val card: Color,
    val cardElevated: Color,
    val accent: Color,
    val accentSoft: Color,
    val label: Color,
    val secondaryLabel: Color,
    val tertiaryLabel: Color,
    val disabledLabel: Color,
    val separator: Color,
    // 五行
    val wood: Color,
    val fire: Color,
    val earth: Color,
    val metal: Color,
    val water: Color,
    // 状态
    val moving: Color,
    val world: Color,
    val response: Color,
    val empty: Color,
    val breakState: Color,
    val combine: Color,
    val clash: Color,
    val isDark: Boolean,
)

val LightAppColors = AppColors(
    systemBackground = SystemBackgroundLight,
    card = CardLight,
    cardElevated = CardLight,
    accent = AccentLight,
    accentSoft = AccentSoftLight,
    label = LabelLight,
    secondaryLabel = SecondaryLabelLight,
    tertiaryLabel = TertiaryLabelLight,
    disabledLabel = DisabledLabelLight,
    separator = SeparatorLight,
    wood = WoodColor, fire = FireColor, earth = EarthColor, metal = MetalColor, water = WaterColor,
    moving = MovingColor, world = WorldColor, response = ResponseColor, empty = EmptyColor,
    breakState = BreakColor, combine = CombineColor, clash = ClashColor,
    isDark = false,
)

val DarkAppColors = AppColors(
    systemBackground = SystemBackgroundDark,
    card = CardDark,
    cardElevated = CardElevatedDark,
    accent = AccentDark,
    accentSoft = AccentSoftDark,
    label = LabelDark,
    secondaryLabel = SecondaryLabelDark,
    tertiaryLabel = TertiaryLabelDark,
    disabledLabel = DisabledLabelDark,
    separator = SeparatorDark,
    // 五行/状态在深色下略提亮以保证可读
    wood = Color(0xFF6DB37C), fire = Color(0xFFE0796B), earth = Color(0xFFC6A06A),
    metal = Color(0xFFD8B458), water = Color(0xFF5C97D1),
    moving = Color(0xFFE0796B), world = Color(0xFF5CB890), response = Color(0xFF5C97D1),
    empty = Color(0xFF9C9CA1), breakState = Color(0xFFC07FC6), combine = Color(0xFFD6A04E),
    clash = Color(0xFFE0796B),
    isDark = true,
)
