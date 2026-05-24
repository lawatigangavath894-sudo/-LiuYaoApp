package com.liuyao.paipan.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * 字体系统(iOS 风) — 不引入 SF Pro,用系统默认无衬线字体,
 * 通过 fontSize / fontWeight / lineHeight / letterSpacing 模拟 iOS 文本层级。
 *
 * 对外消费两种方式:
 *  1. 直接用 [IOSTextStyles] 里的命名样式(LargeTitle / Title1 ... Caption);
 *  2. 用 MaterialTheme.typography(已由 [AppM3Typography] 做了等价映射)。
 */
private val SystemSans = FontFamily.Default

/** iOS 文本层级原始定义。letterSpacing 用 em,贴近 iOS 的字距观感。 */
object IOSTextStyles {
    val LargeTitle = TextStyle(
        fontFamily = SystemSans, fontWeight = FontWeight.Bold,
        fontSize = 34.sp, lineHeight = 41.sp, letterSpacing = 0.004.em,
    )
    val Title1 = TextStyle(
        fontFamily = SystemSans, fontWeight = FontWeight.Bold,
        fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = 0.003.em,
    )
    val Title2 = TextStyle(
        fontFamily = SystemSans, fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = 0.002.em,
    )
    val Title3 = TextStyle(
        fontFamily = SystemSans, fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp, lineHeight = 25.sp, letterSpacing = 0.em,
    )
    val Headline = TextStyle(
        fontFamily = SystemSans, fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp, lineHeight = 22.sp, letterSpacing = (-0.006).em,
    )
    val Body = TextStyle(
        fontFamily = SystemSans, fontWeight = FontWeight.Normal,
        fontSize = 17.sp, lineHeight = 22.sp, letterSpacing = (-0.006).em,
    )
    val Callout = TextStyle(
        fontFamily = SystemSans, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 21.sp, letterSpacing = (-0.004).em,
    )
    val Subhead = TextStyle(
        fontFamily = SystemSans, fontWeight = FontWeight.Normal,
        fontSize = 15.sp, lineHeight = 20.sp, letterSpacing = (-0.002).em,
    )
    val Footnote = TextStyle(
        fontFamily = SystemSans, fontWeight = FontWeight.Normal,
        fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.em,
    )
    val Caption = TextStyle(
        fontFamily = SystemSans, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.em,
    )
    /** Badge 等小标签的强调小字 */
    val CaptionEmphasized = TextStyle(
        fontFamily = SystemSans, fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.em,
    )
}

/**
 * 将 iOS 字阶映射到 Material3 Typography,使内置 M3 组件(若被使用)也具一致观感。
 * 映射关系:
 *  headlineLarge←LargeTitle, headlineMedium←Title1, headlineSmall←Title2,
 *  titleLarge←Title3, titleMedium←Headline,
 *  bodyLarge←Body, bodyMedium←Callout, bodySmall←Subhead,
 *  labelLarge←Footnote, labelMedium←Caption, labelSmall←CaptionEmphasized
 */
val AppM3Typography = Typography(
    headlineLarge = IOSTextStyles.LargeTitle,
    headlineMedium = IOSTextStyles.Title1,
    headlineSmall = IOSTextStyles.Title2,
    titleLarge = IOSTextStyles.Title3,
    titleMedium = IOSTextStyles.Headline,
    titleSmall = IOSTextStyles.Headline,
    bodyLarge = IOSTextStyles.Body,
    bodyMedium = IOSTextStyles.Callout,
    bodySmall = IOSTextStyles.Subhead,
    labelLarge = IOSTextStyles.Footnote,
    labelMedium = IOSTextStyles.Caption,
    labelSmall = IOSTextStyles.CaptionEmphasized,
)
