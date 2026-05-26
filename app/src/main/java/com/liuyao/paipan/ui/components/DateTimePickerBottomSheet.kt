package com.liuyao.paipan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.liuyao.paipan.domain.engine.CalendarConversionService
import com.liuyao.paipan.domain.engine.FourPillarsReverseLookupService
import com.liuyao.paipan.ui.theme.AppTheme
import com.liuyao.paipan.ui.theme.IOSTextStyles
import com.liuyao.paipan.ui.theme.Radius
import com.liuyao.paipan.ui.theme.Spacing
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.LocalDateTime

/**
 * 校正日期时间到合法值:日超过当月最大天数时收敛到当月最后一天;闰年 2 月由 YearMonth 处理。
 */
fun normalizeDateTime(year: Int, month: Int, day: Int, hour: Int, minute: Int): LocalDateTime {
    val y = year.coerceIn(1, 9999)
    val m = month.coerceIn(1, 12)
    val maxDay = java.time.YearMonth.of(y, m).lengthOfMonth()
    val d = day.coerceIn(1, maxDay)
    return LocalDateTime.of(y, m, d, hour.coerceIn(0, 23), minute.coerceIn(0, 59))
}

private enum class CalMode { SOLAR, LUNAR, PILLARS }

/**
 * 选择起卦时间 BottomSheet。三 Tab:公历 / 农历 / 四柱。
 * 任一入口最终都回填真实公历 [LocalDateTime]。
 */
@Composable
fun DateTimePickerBottomSheet(
    dateTime: LocalDateTime,
    onConfirm: (LocalDateTime) -> Unit,
    onDismiss: () -> Unit,
) {
    var mode by remember { mutableStateOf(CalMode.SOLAR) }
    var draft by remember(dateTime) { mutableStateOf(dateTime) }

    IOSBottomSheet(onDismiss = onDismiss, title = "选择起卦时间") {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            // 顶部:Tab 切换 + 今 + 确定
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    CalTab("公历", mode == CalMode.SOLAR) { mode = CalMode.SOLAR }
                    CalTab("农历", mode == CalMode.LUNAR) { mode = CalMode.LUNAR }
                    CalTab("四柱", mode == CalMode.PILLARS) { mode = CalMode.PILLARS }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    if (mode != CalMode.PILLARS) {
                        IOSSecondaryButton(text = "今", onClick = { draft = LocalDateTime.now(CalendarConversionService.ZONE) }, modifier = Modifier.width(48.dp), filled = false)
                    }
                    IOSSecondaryButton(text = "确定", onClick = { onConfirm(draft) }, modifier = Modifier.width(64.dp))
                }
            }

            when (mode) {
                CalMode.SOLAR -> GregorianDateTimePicker(draft) { draft = it }
                CalMode.LUNAR -> LunarDateTimePicker(draft) { draft = it }
                CalMode.PILLARS -> FourPillarsReversePicker(draft) { draft = it }
            }
        }
    }
}

/** 公历:横向五列滚轮 */
@Composable
private fun GregorianDateTimePicker(draft: LocalDateTime, onChange: (LocalDateTime) -> Unit) {
    val nowYear = LocalDateTime.now(CalendarConversionService.ZONE).year
    val years = remember(nowYear) { (nowYear - 100..nowYear + 100).toList() }
    fun upd(y: Int = draft.year, m: Int = draft.monthValue, d: Int = draft.dayOfMonth, h: Int = draft.hour, mi: Int = draft.minute) =
        onChange(normalizeDateTime(y, m, d, h, mi))

    Column {
        WheelRow {
            WheelColumn(Modifier.weight(1.4f), years, years.indexOf(draft.year).coerceAtLeast(0), { "${years[it]}年" }) { upd(y = years[it]) }
            WheelColumn(Modifier.weight(1f), (1..12).toList(), draft.monthValue - 1, { "${it + 1}月" }) { upd(m = it + 1) }
            WheelColumn(Modifier.weight(1f), (1..31).toList(), draft.dayOfMonth - 1, { "${it + 1}日" }) { upd(d = it + 1) }
            WheelColumn(Modifier.weight(1f), (0..23).toList(), draft.hour, { "%02d时".format(it) }) { upd(h = it) }
            WheelColumn(Modifier.weight(1f), (0..59).toList(), draft.minute, { "%02d分".format(it) }) { upd(mi = it) }
        }
        ResultText("公历", "%04d-%02d-%02d %02d:%02d".format(draft.year, draft.monthValue, draft.dayOfMonth, draft.hour, draft.minute))
    }
}

/** 农历:年/月(含闰)/日/时/分,实时换算公历 */
@Composable
private fun LunarDateTimePicker(draft: LocalDateTime, onChange: (LocalDateTime) -> Unit) {
    val info = remember(draft) { CalendarConversionService.lunarInfoOf(draft) }
    val nowYear = info.lunarYear
    val years = remember(nowYear) { (nowYear - 100..nowYear + 100).toList() }

    // 当前农历年的月份选项:1..12,若有闰月则在闰月后插入"闰X"
    val leap = remember(info.lunarYear) { CalendarConversionService.leapMonthOf(info.lunarYear) }
    data class MonthOpt(val month: Int, val isLeap: Boolean, val label: String)
    val monthOpts = remember(info.lunarYear, leap) {
        buildList {
            for (m in 1..12) {
                add(MonthOpt(m, false, "${cnMonth(m)}月"))
                if (leap == m) add(MonthOpt(m, true, "闰${cnMonth(m)}月"))
            }
        }
    }
    val curMonthIdx = monthOpts.indexOfFirst { it.month == info.lunarMonth && it.isLeap == info.isLeapMonth }.coerceAtLeast(0)
    val maxDay = CalendarConversionService.lunarMonthDays(info.lunarYear, info.lunarMonth, info.isLeapMonth).coerceIn(29, 30)

    fun rebuild(y: Int = info.lunarYear, mIdx: Int = curMonthIdx, d: Int = info.lunarDay, h: Int = draft.hour, mi: Int = draft.minute) {
        val mo = monthOpts.getOrElse(mIdx) { monthOpts[0] }
        val md = CalendarConversionService.lunarMonthDays(y, mo.month, mo.isLeap).coerceIn(29, 30)
        val day = d.coerceIn(1, md)
        onChange(CalendarConversionService.lunarToSolar(y, mo.month, mo.isLeap, day, h, mi))
    }

    Column {
        WheelRow {
            WheelColumn(Modifier.weight(1.4f), years, years.indexOf(info.lunarYear).coerceAtLeast(0), { "${years[it]}" }) { rebuild(y = years[it]) }
            WheelColumn(Modifier.weight(1.3f), monthOpts.indices.toList(), curMonthIdx, { monthOpts[it].label }) { rebuild(mIdx = it) }
            WheelColumn(Modifier.weight(1f), (1..maxDay).toList(), (info.lunarDay - 1).coerceIn(0, maxDay - 1), { cnDay(it + 1) }) { rebuild(d = it + 1) }
            WheelColumn(Modifier.weight(1f), (0..23).toList(), draft.hour, { "%02d时".format(it) }) { rebuild(h = it) }
            WheelColumn(Modifier.weight(1f), (0..59).toList(), draft.minute, { "%02d分".format(it) }) { rebuild(mi = it) }
        }
        ResultText("农历", "${info.yearGanZhi}年 ${info.monthInChinese}月${info.dayInChinese}")
        ResultText("公历", "%04d-%02d-%02d %02d:%02d".format(draft.year, draft.monthValue, draft.dayOfMonth, draft.hour, draft.minute))
    }
}

/** 四柱:选四柱 → 反查公历候选 → 选候选回填 */
@Composable
private fun FourPillarsReversePicker(draft: LocalDateTime, onChange: (LocalDateTime) -> Unit) {
    val gan = listOf("甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸")
    val zhi = listOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")
    val ganZhi = remember { buildList { for (i in 0 until 60) add(gan[i % 10] + zhi[i % 12]) } }

    var yearGz by remember { mutableStateOf<String?>(null) }
    var monthGz by remember { mutableStateOf<String?>(null) }
    var dayGz by remember { mutableStateOf<String?>(null) }
    var timeGz by remember { mutableStateOf<String?>(null) }
    var result by remember { mutableStateOf<FourPillarsReverseLookupService.LookupResult?>(null) }
    var selected by remember { mutableStateOf<LocalDateTime?>(null) }

    val nowYear = LocalDateTime.now(CalendarConversionService.ZONE).year

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        PillarPicker("年柱", ganZhi, yearGz) { yearGz = it }
        PillarPicker("月柱", ganZhi, monthGz) { monthGz = it }
        PillarPicker("日柱", ganZhi, dayGz) { dayGz = it }
        PillarPicker("时柱", ganZhi, timeGz) { timeGz = it }

        IOSSecondaryButton(
            text = "反查时间(${nowYear - 100}–${nowYear + 100})",
            onClick = {
                result = FourPillarsReverseLookupService.lookup(
                    FourPillarsReverseLookupService.Pillars(yearGz, monthGz, dayGz, timeGz),
                    nowYear - 100, nowYear + 100,
                )
                selected = null
            },
            modifier = Modifier.fillMaxWidth(),
        )

        result?.let { r ->
            r.message?.let { Text(it, style = IOSTextStyles.Footnote, color = AppTheme.colors.secondaryLabel) }
            LazyColumn(Modifier.fillMaxWidth().heightIn(max = 220.dp), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                items(r.candidates) { c ->
                    val on = selected == c.dateTime
                    Column(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(Radius.small))
                            .background(if (on) AppTheme.colors.accentSoft.copy(alpha = 0.4f) else AppTheme.colors.cardElevated)
                            .clickableNoRipple { selected = c.dateTime; onChange(c.dateTime) }
                            .padding(Spacing.sm),
                    ) {
                        Text(c.solarText, style = IOSTextStyles.Body, color = AppTheme.colors.label)
                        Text("${c.lunarText} · ${c.pillarsText}", style = IOSTextStyles.Caption, color = AppTheme.colors.secondaryLabel)
                    }
                }
            }
        }
    }
}

@Composable
private fun PillarPicker(label: String, options: List<String>, selected: String?, onSelect: (String?) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().clickableNoRipple { open = !open }.padding(vertical = Spacing.xs), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = IOSTextStyles.Body, color = AppTheme.colors.label)
        Text(selected ?: "未选", style = IOSTextStyles.Body, color = if (selected != null) AppTheme.colors.accent else AppTheme.colors.tertiaryLabel)
    }
    if (open) {
        Box(Modifier.fillMaxWidth().height((40 * 5).dp)) {
            Box(Modifier.fillMaxWidth().height(40.dp).align(Alignment.Center).clip(RoundedCornerShape(Radius.small)).background(AppTheme.colors.accentSoft.copy(alpha = 0.3f)))
            WheelColumn(Modifier.fillMaxWidth(), options, options.indexOf(selected).coerceAtLeast(0), { options[it] }) { onSelect(options[it]) }
        }
    }
}

@Composable
private fun ResultText(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(top = Spacing.xs), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(label, style = IOSTextStyles.Footnote, color = AppTheme.colors.tertiaryLabel)
        Text(value, style = IOSTextStyles.Footnote, color = AppTheme.colors.secondaryLabel)
    }
}

@Composable
private fun WheelRow(content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) {
    Box(Modifier.fillMaxWidth().height(WHEEL_HEIGHT.dp)) {
        Box(Modifier.fillMaxWidth().height(ITEM_HEIGHT.dp).align(Alignment.Center).clip(RoundedCornerShape(Radius.small)).background(AppTheme.colors.accentSoft.copy(alpha = 0.35f)))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp), content = content)
    }
}

private const val ITEM_HEIGHT = 40
private const val VISIBLE = 5
private const val WHEEL_HEIGHT = ITEM_HEIGHT * VISIBLE

@Composable
private fun <T> WheelColumn(
    modifier: Modifier,
    data: List<T>,
    selectedIndex: Int,
    label: (Int) -> String,
    onSelect: (Int) -> Unit,
) {
    val pad = VISIBLE / 2
    val safeStart = selectedIndex.coerceIn(0, (data.size - 1).coerceAtLeast(0))
    val state = rememberLazyListState(initialFirstVisibleItemIndex = safeStart)
    val fling = rememberSnapFlingBehavior(lazyListState = state)

    LaunchedEffect(selectedIndex) {
        if (selectedIndex in data.indices && state.firstVisibleItemIndex != selectedIndex) state.scrollToItem(selectedIndex)
    }
    val centerIndex by remember { derivedStateOf { state.firstVisibleItemIndex.coerceIn(0, (data.size - 1).coerceAtLeast(0)) } }
    LaunchedEffect(state) {
        snapshotFlowIsScrolling(state).distinctUntilChanged().collect { scrolling -> if (!scrolling) onSelect(centerIndex) }
    }

    LazyColumn(
        modifier = modifier.height(WHEEL_HEIGHT.dp),
        state = state,
        flingBehavior = fling,
        contentPadding = PaddingValues(vertical = (ITEM_HEIGHT * pad).dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items(data.size) { i ->
            val distance = kotlin.math.abs(i - centerIndex)
            Box(Modifier.height(ITEM_HEIGHT.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    label(i),
                    style = IOSTextStyles.Body,
                    color = when (distance) {
                        0 -> AppTheme.colors.label
                        1 -> AppTheme.colors.secondaryLabel
                        else -> AppTheme.colors.tertiaryLabel
                    },
                    fontWeight = if (distance == 0) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}

private fun snapshotFlowIsScrolling(state: androidx.compose.foundation.lazy.LazyListState) =
    androidx.compose.runtime.snapshotFlow { state.isScrollInProgress }

@Composable
private fun CalTab(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.clip(RoundedCornerShape(Radius.badge))
            .background(if (selected) AppTheme.colors.accentSoft else AppTheme.colors.separator.copy(alpha = 0.35f))
            .clickableNoRipple(onClick)
            .padding(horizontal = Spacing.md, vertical = Spacing.xs),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = IOSTextStyles.CaptionEmphasized, color = if (selected) AppTheme.colors.accent else AppTheme.colors.secondaryLabel)
    }
}

private fun cnMonth(m: Int): String = listOf("", "正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "冬", "腊")[m]
private fun cnDay(d: Int): String {
    val num = listOf("十", "一", "二", "三", "四", "五", "六", "七", "八", "九")
    return when {
        d == 10 -> "初十"
        d == 20 -> "二十"
        d == 30 -> "三十"
        d < 10 -> "初" + num[d]
        d < 20 -> "十" + num[d - 10]
        d < 30 -> "廿" + num[d - 20]
        else -> "三" + num[d - 30]
    }
}
