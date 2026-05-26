package com.liuyao.paipan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.liuyao.paipan.domain.engine.ChartInput
import com.liuyao.paipan.domain.engine.MeiHuaTimeDivinationCalculator
import com.liuyao.paipan.domain.model.DivinationCategory
import com.liuyao.paipan.domain.model.DivinationMethod
import com.liuyao.paipan.domain.model.DivinationMode
import com.liuyao.paipan.domain.model.YinYang
import com.liuyao.paipan.domain.model.yaoPositionName
import com.liuyao.paipan.ui.components.IOSBottomSheet
import com.liuyao.paipan.ui.components.DateTimePickerBottomSheet
import com.liuyao.paipan.ui.components.IOSDetailScaffold
import com.liuyao.paipan.ui.components.IOSGroupedSection
import com.liuyao.paipan.ui.components.IOSListRow
import com.liuyao.paipan.ui.components.IOSPrimaryButton
import com.liuyao.paipan.ui.components.IOSSecondaryButton
import com.liuyao.paipan.ui.components.clickableNoRipple
import com.liuyao.paipan.ui.screens.chart.ChartAction
import com.liuyao.paipan.ui.screens.chart.ChartViewModel
import com.liuyao.paipan.ui.theme.AppTheme
import com.liuyao.paipan.ui.theme.IOSTextStyles
import com.liuyao.paipan.ui.theme.Radius
import com.liuyao.paipan.ui.theme.Spacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ManualYaoInput(
    val index: Int,
    val yinYang: YinYang,
    val isMoving: Boolean,
)

data class CastUiState(
    val questionText: String = "",
    val category: DivinationCategory = DivinationCategory.STUDY,
    val mode: DivinationMode = DivinationMode.CURRENT_TIME,
    val selectedDateTime: LocalDateTime = LocalDateTime.now(),
    val manualLines: List<ManualYaoInput> = defaultManualLines(),
)

class CastViewModel : ViewModel() {
    private val _ui = MutableStateFlow(CastUiState())
    val ui: StateFlow<CastUiState> = _ui.asStateFlow()

    fun updateQuestion(text: String) {
        _ui.update { it.copy(questionText = text) }
    }

    fun selectCategory(category: DivinationCategory) {
        _ui.update { it.copy(category = category) }
    }

    fun selectMode(mode: DivinationMode) {
        _ui.update { it.copy(mode = mode) }
    }

    fun setSelectedDateTime(dateTime: LocalDateTime) {
        _ui.update { it.copy(selectedDateTime = dateTime) }
    }

    fun toggleYinYang(index: Int) {
        _ui.update { state ->
            state.copy(
                manualLines = state.manualLines.map { line ->
                    if (line.index == index) line.copy(yinYang = line.yinYang.flip()) else line
                },
            )
        }
    }

    fun toggleMoving(index: Int) {
        _ui.update { state ->
            state.copy(
                manualLines = state.manualLines.map { line ->
                    if (line.index == index) line.copy(isMoving = !line.isMoving) else line
                },
            )
        }
    }

    fun buildChartInput(now: LocalDateTime = LocalDateTime.now()): ChartInput {
        val state = ui.value
        val question = state.questionText.trim().ifBlank { "未命名占事" }
        return when (state.mode) {
            DivinationMode.CURRENT_TIME -> MeiHuaTimeDivinationCalculator.chartInput(
                dateTime = now,
                question = question,
                category = state.category,
                method = DivinationMethod.SolarTime,
            )

            DivinationMode.SELECTED_TIME -> MeiHuaTimeDivinationCalculator.chartInput(
                dateTime = state.selectedDateTime,
                question = question,
                category = state.category,
                method = DivinationMethod.SelectedTime,
            )

            DivinationMode.MANUAL -> {
                val lines = state.manualLines.sortedBy { it.index }
                ChartInput(
                    dateTime = now,
                    lines = lines.map { it.yinYang },
                    moving = lines.map { it.isMoving },
                    question = question,
                    method = DivinationMethod.Manual(raw = encodeManual(lines)),
                    category = state.category,
                )
            }
        }
    }
}

@Composable
fun CastScreen(
    vm: ChartViewModel,
    onBack: () -> Unit,
    onCasted: () -> Unit,
    castVm: CastViewModel = viewModel(),
) {
    val state by castVm.ui.collectAsStateWithLifecycle()
    var activeSheet by remember { mutableStateOf<CastSheet?>(null) }
    var nowForDisplay by remember { mutableStateOf(LocalDateTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            nowForDisplay = LocalDateTime.now()
            delay(1_000)
        }
    }

    IOSDetailScaffold(title = "起卦", onBack = onBack) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap),
            modifier = Modifier.padding(bottom = Spacing.xxl),
        ) {
            item {
                IOSGroupedSection(header = "占事类别") {
                    item {
                        CategorySelectorCard(
                            category = state.category,
                            onClick = { activeSheet = CastSheet.Category },
                        )
                    }
                }
            }

            item {
                IOSGroupedSection(header = "占事") {
                    item {
                        QuestionInputCard(
                            value = state.questionText,
                            onValueChange = castVm::updateQuestion,
                        )
                    }
                }
            }

            item {
                IOSGroupedSection(header = "起卦方式") {
                    item {
                        DivinationModeCard(
                            mode = state.mode,
                            onClick = { activeSheet = CastSheet.Method },
                        )
                    }
                }
            }

            when (state.mode) {
                DivinationMode.CURRENT_TIME -> item {
                    IOSGroupedSection(header = "时间设置") {
                        item { CurrentTimeInfoCard(nowForDisplay) }
                    }
                }

                DivinationMode.SELECTED_TIME -> item {
                    IOSGroupedSection(header = "时间设置") {
                        item {
                            DateTimeSelectCard(
                                dateTime = state.selectedDateTime,
                                onClick = { activeSheet = CastSheet.DateTime },
                            )
                        }
                    }
                }

                DivinationMode.MANUAL -> Unit
            }

            if (state.mode == DivinationMode.MANUAL) {
                item {
                    ManualYaoPanel(
                        lines = state.manualLines,
                        onToggleYinYang = castVm::toggleYinYang,
                        onToggleMoving = castVm::toggleMoving,
                    )
                }
            }

            item {
                Column(Modifier.padding(horizontal = Spacing.pageHorizontal)) {
                    IOSPrimaryButton(
                        text = "起此卦",
                        onClick = {
                            vm.dispatch(ChartAction.BuildChart(castVm.buildChartInput()))
                            onCasted()
                        },
                    )
                }
            }
        }
    }

    when (activeSheet) {
        CastSheet.Category -> CategoryPickerSheet(
            selected = state.category,
            onSelect = {
                castVm.selectCategory(it)
                activeSheet = null
            },
            onDismiss = { activeSheet = null },
        )

        CastSheet.Method -> MethodPickerSheet(
            selected = state.mode,
            onSelect = {
                castVm.selectMode(it)
                activeSheet = null
            },
            onDismiss = { activeSheet = null },
        )

        CastSheet.DateTime -> DateTimePickerBottomSheet(
            dateTime = state.selectedDateTime,
            onConfirm = {
                castVm.setSelectedDateTime(it)
                activeSheet = null
            },
            onDismiss = { activeSheet = null },
        )

        null -> Unit
    }
}

@Composable
private fun CategorySelectorCard(category: DivinationCategory, onClick: () -> Unit) {
    IOSListRow(
        title = category.castLabel(),
        value = "选择",
        showChevron = true,
        onClick = onClick,
    )
}

@Composable
private fun QuestionInputCard(value: String, onValueChange: (String) -> Unit) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = IOSTextStyles.Body.copy(color = AppTheme.colors.label),
        cursorBrush = SolidColor(AppTheme.colors.accent),
        minLines = 2,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 96.dp)
            .padding(horizontal = Spacing.rowHorizontal, vertical = Spacing.rowVertical),
        decorationBox = { inner ->
            Box(Modifier.fillMaxWidth()) {
                if (value.isBlank()) {
                    Text(
                        "请输入占事，例如“明天科二能不能过”",
                        style = IOSTextStyles.Body,
                        color = AppTheme.colors.tertiaryLabel,
                    )
                }
                inner()
            }
        },
    )
}

@Composable
private fun DivinationModeCard(mode: DivinationMode, onClick: () -> Unit) {
    IOSListRow(
        title = mode.castLabel(),
        value = mode.castDescription(),
        showChevron = true,
        onClick = onClick,
    )
}

@Composable
private fun DateTimeSelectCard(dateTime: LocalDateTime, onClick: () -> Unit) {
    IOSListRow(
        title = dateTimeFormatter.format(dateTime),
        value = "调整",
        showChevron = true,
        onClick = onClick,
    )
}

@Composable
private fun CurrentTimeInfoCard(dateTime: LocalDateTime) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.rowHorizontal, vertical = Spacing.rowVertical),
    ) {
        Text(dateTimeFormatter.format(dateTime), style = IOSTextStyles.Body, color = AppTheme.colors.label)
        Text(
            "点击“起此卦”时读取当前系统时间，并按梅花易数时间起卦。",
            style = IOSTextStyles.Footnote,
            color = AppTheme.colors.secondaryLabel,
            modifier = Modifier.padding(top = Spacing.xs),
        )
    }
}

@Composable
private fun ManualYaoPanel(
    lines: List<ManualYaoInput>,
    onToggleYinYang: (Int) -> Unit,
    onToggleMoving: (Int) -> Unit,
) {
    IOSGroupedSection(
        header = "手动摆爻（上爻 → 初爻）",
        footer = "点中间切阴阳，点“动”标记动爻。",
    ) {
        lines.sortedByDescending { it.index }.forEach { line ->
            item {
                ManualYaoRow(
                    line = line,
                    onToggleYinYang = { onToggleYinYang(line.index) },
                    onToggleMoving = { onToggleMoving(line.index) },
                )
            }
        }
    }
}

@Composable
private fun ManualYaoRow(
    line: ManualYaoInput,
    onToggleYinYang: () -> Unit,
    onToggleMoving: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.rowHorizontal, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            yaoPositionName(line.index),
            style = IOSTextStyles.Caption,
            color = AppTheme.colors.tertiaryLabel,
            modifier = Modifier.width(32.dp),
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(Radius.small))
                .clickableNoRipple(onToggleYinYang)
                .padding(vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            YaoSymbol(line.yinYang)
            Spacer(Modifier.width(Spacing.md))
            Text(line.yinYang.castLabel(), style = IOSTextStyles.Body, color = AppTheme.colors.label)
        }
        Box(
            Modifier
                .clip(RoundedCornerShape(Radius.badge))
                .background(
                    if (line.isMoving) AppTheme.colors.accentSoft else AppTheme.colors.separator.copy(alpha = 0.4f),
                )
                .clickableNoRipple(onToggleMoving)
                .padding(horizontal = Spacing.md, vertical = Spacing.xs),
        ) {
            Text(
                "动",
                style = IOSTextStyles.CaptionEmphasized,
                color = if (line.isMoving) AppTheme.colors.accent else AppTheme.colors.tertiaryLabel,
                fontWeight = if (line.isMoving) FontWeight.SemiBold else FontWeight.Normal,
            )
        }
    }
}

@Composable
private fun YaoSymbol(yinYang: YinYang) {
    Row(Modifier.width(86.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (yinYang == YinYang.YANG) {
            YaoStroke(Modifier.weight(1f))
        } else {
            YaoStroke(Modifier.weight(1f))
            YaoStroke(Modifier.weight(1f))
        }
    }
}

@Composable
private fun YaoStroke(modifier: Modifier) {
    Box(
        modifier
            .height(5.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(AppTheme.colors.label.copy(alpha = 0.75f)),
    )
}

@Composable
private fun CategoryPickerSheet(
    selected: DivinationCategory,
    onSelect: (DivinationCategory) -> Unit,
    onDismiss: () -> Unit,
) {
    IOSBottomSheet(onDismiss = onDismiss, title = "占事类别") {
        Column {
            castCategoryOptions.forEach { category ->
                PickerRow(
                    label = category.castLabel(),
                    selected = selected == category,
                    onClick = { onSelect(category) },
                )
            }
        }
    }
}

@Composable
private fun MethodPickerSheet(
    selected: DivinationMode,
    onSelect: (DivinationMode) -> Unit,
    onDismiss: () -> Unit,
) {
    IOSBottomSheet(onDismiss = onDismiss, title = "起卦方式") {
        Column {
            methodOptions.forEach { mode ->
                PickerRow(
                    label = mode.castLabel(),
                    detail = mode.castDescription(),
                    selected = selected == mode,
                    onClick = { onSelect(mode) },
                )
            }
        }
    }
}

@Composable
private fun PickerRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    detail: String? = null,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = IOSTextStyles.Body, color = AppTheme.colors.label)
            if (detail != null) {
                Text(detail, style = IOSTextStyles.Footnote, color = AppTheme.colors.secondaryLabel)
            }
        }
        if (selected) {
            Text("✓", style = IOSTextStyles.Body, color = AppTheme.colors.accent)
        } else {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = AppTheme.colors.tertiaryLabel,
            )
        }
    }
}

private enum class CastSheet { Category, Method, DateTime }

private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

private val castCategoryOptions = listOf(
    DivinationCategory.MARRIAGE,
    DivinationCategory.LAWSUIT,
    DivinationCategory.WEALTH,
    DivinationCategory.STUDY,
    DivinationCategory.CAREER,
    DivinationCategory.HEALTH,
    DivinationCategory.TRAVEL,
    DivinationCategory.LOST,
    DivinationCategory.OTHER,
)

private val methodOptions = listOf(
    DivinationMode.CURRENT_TIME,
    DivinationMode.SELECTED_TIME,
    DivinationMode.MANUAL,
)

private fun defaultManualLines(): List<ManualYaoInput> =
    (1..6).map { ManualYaoInput(index = it, yinYang = YinYang.YANG, isMoving = false) }

private fun encodeManual(lines: List<ManualYaoInput>): String =
    lines.sortedBy { it.index }.joinToString("") { line ->
        when {
            line.yinYang == YinYang.YANG && line.isMoving -> "O"
            line.yinYang == YinYang.YANG -> "+"
            line.isMoving -> "X"
            else -> "-"
        }
    }

private fun DivinationMode.castLabel(): String = when (this) {
    DivinationMode.CURRENT_TIME -> "正时起卦"
    DivinationMode.SELECTED_TIME -> "选择时间起卦"
    DivinationMode.MANUAL -> "手动起卦"
}

private fun DivinationMode.castDescription(): String = when (this) {
    DivinationMode.CURRENT_TIME -> "以当前时间为起卦时刻"
    DivinationMode.SELECTED_TIME -> "手动选择起卦时间"
    DivinationMode.MANUAL -> "手动设置六爻"
}

private fun DivinationCategory.castLabel(): String = when (this) {
    DivinationCategory.MARRIAGE -> "婚姻"
    DivinationCategory.LAWSUIT -> "官事"
    DivinationCategory.WEALTH -> "财运"
    DivinationCategory.STUDY -> "学业"
    DivinationCategory.CAREER -> "求职"
    DivinationCategory.HEALTH -> "健康"
    DivinationCategory.TRAVEL -> "出行"
    DivinationCategory.LOST -> "失物"
    DivinationCategory.OTHER -> "其他"
    DivinationCategory.FAME -> "求名"
    DivinationCategory.PREGNANCY -> "孕产"
    DivinationCategory.HOUSE -> "房宅"
    DivinationCategory.COOPERATION -> "合作"
    DivinationCategory.FORTUNE -> "运势"
}

private fun YinYang.castLabel(): String = if (this == YinYang.YANG) "阳" else "阴"
