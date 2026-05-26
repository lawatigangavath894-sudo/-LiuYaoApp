package com.liuyao.paipan.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.liuyao.paipan.ui.theme.AppTheme
import com.liuyao.paipan.ui.theme.IOSTextStyles
import com.liuyao.paipan.ui.theme.Radius
import com.liuyao.paipan.ui.theme.Sizes
import com.liuyao.paipan.ui.theme.Spacing

// ════════════════════════════════════════════════════════════════════
// 通用:无水波点击
// ════════════════════════════════════════════════════════════════════

@Composable
fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier {
    val source = remember { MutableInteractionSource() }
    return this.clickable(interactionSource = source, indication = null, onClick = onClick)
}

// ════════════════════════════════════════════════════════════════════
// 1. IOSLargeTitleScaffold
// ════════════════════════════════════════════════════════════════════

/**
 * 一级页容器:Large Title 随滚动折叠为内联标题,系统浅灰背景,可选右上动作。
 * 内容区由调用方放置可滚动列表(LazyColumn),用 [PaddingValues] 套入。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IOSLargeTitleScaffold(
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val bg = AppTheme.colors.systemBackground

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = bg,
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            LargeTopAppBar(
                title = { Text(title, color = AppTheme.colors.label) },
                actions = { trailing?.invoke() },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = bg,
                    scrolledContainerColor = bg,
                ),
            )
        },
        content = content,
    )
}

// ════════════════════════════════════════════════════════════════════
// 1b. IOSDetailScaffold(次级页:Large Title + 返回箭头)
// ════════════════════════════════════════════════════════════════════

/**
 * 次级压栈页容器。视觉同 Large Title,但左上提供返回。
 * 用于 Cast / Chart 等从堆栈进入、需要回退的页面。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IOSDetailScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val bg = AppTheme.colors.systemBackground
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = bg,
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            LargeTopAppBar(
                title = { Text(title, color = AppTheme.colors.label) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = AppTheme.colors.accent)
                    }
                },
                actions = { trailing?.invoke() },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = bg, scrolledContainerColor = bg,
                ),
            )
        },
        content = content,
    )
}

// ════════════════════════════════════════════════════════════════════
// 2. IOSNavigationBar(次级页顶部导航栏)
// ════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IOSNavigationBar(
    title: String,
    onBack: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val bg = AppTheme.colors.systemBackground
    TopAppBar(
        title = {
            Text(
                title,
                style = IOSTextStyles.Headline,
                color = AppTheme.colors.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = AppTheme.colors.accent)
                }
            }
        },
        actions = { trailing?.invoke() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = bg,
            scrolledContainerColor = bg,
            titleContentColor = AppTheme.colors.label,
        ),
    )
}

// ════════════════════════════════════════════════════════════════════
// 3. IOSGroupedSection(grouped list 容器)
// ════════════════════════════════════════════════════════════════════

/**
 * 分组容器:可选 header/footer 小字 + 圆角白卡。
 * 在 [content] 中对每个表项调用 [IOSGroupedScope.item];相邻项间自动加内缩发丝线。
 */
@Composable
fun IOSGroupedSection(
    modifier: Modifier = Modifier,
    header: String? = null,
    footer: String? = null,
    content: IOSGroupedScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = Spacing.pageHorizontal)) {
        if (header != null) {
            Text(
                header.uppercase(),
                style = IOSTextStyles.Footnote,
                color = AppTheme.colors.secondaryLabel,
                modifier = Modifier.padding(start = Spacing.xs, bottom = Spacing.sectionHeaderGap, top = Spacing.sm),
            )
        }
        Surface(
            shape = RoundedCornerShape(Radius.card),
            color = AppTheme.colors.card,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            val scope = IOSGroupedScope().apply(content)
            Column {
                scope.rows.forEachIndexed { index, row ->
                    if (index > 0) {
                        HorizontalDivider(
                            thickness = Sizes.hairline,
                            color = AppTheme.colors.separator,
                            modifier = Modifier.padding(start = Spacing.rowHorizontal),
                        )
                    }
                    row()
                }
            }
        }
        if (footer != null) {
            Text(
                footer,
                style = IOSTextStyles.Footnote,
                color = AppTheme.colors.secondaryLabel,
                modifier = Modifier.padding(start = Spacing.xs, top = Spacing.sectionHeaderGap),
            )
        }
    }
}

class IOSGroupedScope {
    val rows = mutableListOf<@Composable () -> Unit>()
    fun item(content: @Composable () -> Unit) {
        rows.add(content)
    }
}

// ════════════════════════════════════════════════════════════════════
// 4. IOSListRow(左标题 / 右值 / 箭头 / Badge / 可点击)
// ════════════════════════════════════════════════════════════════════

@Composable
fun IOSListRow(
    title: String,
    modifier: Modifier = Modifier,
    value: String? = null,
    badge: String? = null,
    showChevron: Boolean = false,
    leading: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Sizes.listRowMinHeight)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = Spacing.rowHorizontal, vertical = Spacing.rowVertical),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            if (leading != null) {
                leading()
                androidx.compose.foundation.layout.Spacer(Modifier.size(Spacing.md))
            }
            Text(title, style = IOSTextStyles.Body, color = AppTheme.colors.label)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (badge != null) {
                IOSBadge(badge)
                androidx.compose.foundation.layout.Spacer(Modifier.size(Spacing.sm))
            }
            if (value != null) {
                Text(
                    value,
                    style = IOSTextStyles.Body,
                    color = AppTheme.colors.secondaryLabel,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (showChevron) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = AppTheme.colors.tertiaryLabel,
                    modifier = Modifier.padding(start = Spacing.xs),
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════
// 5. IOSSegmentedControl(胶囊分段,支持横向滚动)
// ════════════════════════════════════════════════════════════════════

/**
 * 胶囊分段选择器。
 * @param scrollable 为 true 时各段按内容宽度排布并可横向滚动(适合"神煞/旺衰/…"等多段);
 *                   为 false 时等分填满宽度(适合 2–3 段)。
 */
@Composable
fun IOSSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    scrollable: Boolean = false,
) {
    val container = AppTheme.colors.separator.copy(alpha = 0.18f)
    if (scrollable) {
        Row(
            modifier = modifier
                .horizontalScrollCompat()
                .clip(RoundedCornerShape(Radius.segmentOuter))
                .background(container)
                .padding(Spacing.xxs),
        ) {
            options.forEachIndexed { i, label -> Segment(label, i == selectedIndex, false) { onSelect(i) } }
        }
    } else {
        Row(
            modifier = modifier
                .clip(RoundedCornerShape(Radius.segmentOuter))
                .background(container)
                .padding(Spacing.xxs),
        ) {
            options.forEachIndexed { i, label ->
                Box(Modifier.weight(1f)) { Segment(label, i == selectedIndex, true) { onSelect(i) } }
            }
        }
    }
}

@Composable
private fun Segment(label: String, selected: Boolean, fill: Boolean, onClick: () -> Unit) {
    val pill by animateColorAsState(
        targetValue = if (selected) AppTheme.colors.card else Color.Transparent,
        animationSpec = tween(180), label = "seg",
    )
    Box(
        modifier = Modifier
            .then(if (fill) Modifier.fillMaxWidth() else Modifier)
            .clip(RoundedCornerShape(Radius.segmentPill))
            .background(pill)
            .clickableNoRipple(onClick)
            .padding(horizontal = Spacing.md, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = if (selected) IOSTextStyles.Subhead.copy(fontWeight = FontWeight.SemiBold) else IOSTextStyles.Subhead,
            color = if (selected) AppTheme.colors.label else AppTheme.colors.secondaryLabel,
            maxLines = 1,
        )
    }
}

/** 轻量包装,避免在多处重复 import rememberScrollState */
@Composable
private fun Modifier.horizontalScrollCompat(): Modifier {
    val state = rememberScrollState()
    return this.horizontalScroll(state)
}

// ════════════════════════════════════════════════════════════════════
// 6. IOSBadge(小型胶囊标签:世/应/动/空/破/合/冲 等)
// ════════════════════════════════════════════════════════════════════

@Composable
fun IOSBadge(
    text: String,
    modifier: Modifier = Modifier,
    container: Color = AppTheme.colors.accentSoft,
    content: Color = AppTheme.colors.accent,
) {
    Text(
        text = text,
        style = IOSTextStyles.CaptionEmphasized,
        color = content,
        modifier = modifier
            .clip(RoundedCornerShape(Radius.badge))
            .background(container)
            .padding(horizontal = Spacing.sm, vertical = 3.dp),
    )
}

// ════════════════════════════════════════════════════════════════════
// 7. IOSCard(通用内容卡片)
// ════════════════════════════════════════════════════════════════════

@Composable
fun IOSCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(Spacing.cardPadding),
    content: @Composable () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(Radius.card),
        color = AppTheme.colors.card,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        Box(Modifier.padding(contentPadding)) { content() }
    }
}

// ════════════════════════════════════════════════════════════════════
// 8. IOSBottomSheet(编辑断语 / 反馈 / 选择占类)
// ════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IOSBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppTheme.colors.card,
        shape = RoundedCornerShape(topStart = Radius.sheet, topEnd = Radius.sheet),
        dragHandle = {
            Box(Modifier.fillMaxWidth().padding(top = Spacing.md, bottom = Spacing.sm), contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .size(width = Sizes.sheetGrabberWidth, height = Sizes.sheetGrabberHeight)
                        .clip(RoundedCornerShape(Radius.badge))
                        .background(AppTheme.colors.tertiaryLabel),
                )
            }
        },
        modifier = modifier,
    ) {
        Column(Modifier.padding(horizontal = Spacing.pageHorizontal).padding(bottom = Spacing.xxl)) {
            if (title != null) {
                Text(
                    title,
                    style = IOSTextStyles.Title3,
                    color = AppTheme.colors.label,
                    modifier = Modifier.padding(bottom = Spacing.lg),
                )
            }
            content()
        }
    }
}

// ════════════════════════════════════════════════════════════════════
// 9. IOSPrimaryButton(底部主操作)
// ════════════════════════════════════════════════════════════════════

@Composable
fun IOSPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val bg = if (enabled) AppTheme.colors.accent else AppTheme.colors.disabledLabel
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Sizes.primaryButtonHeight)
            .clip(RoundedCornerShape(Radius.button))
            .background(bg)
            .then(if (enabled) Modifier.clickableNoRipple(onClick) else Modifier)
            .padding(vertical = Spacing.md),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            style = IOSTextStyles.Headline,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
    }
}

// ════════════════════════════════════════════════════════════════════
// 10. IOSSecondaryButton(轻量:灰底或纯文字)
// ════════════════════════════════════════════════════════════════════

@Composable
fun IOSSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = true,
) {
    if (filled) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = Sizes.secondaryButtonHeight)
                .clip(RoundedCornerShape(Radius.button))
                .background(AppTheme.colors.accentSoft)
                .clickableNoRipple(onClick)
                .padding(vertical = Spacing.sm),
            contentAlignment = Alignment.Center,
        ) {
            Text(text, style = IOSTextStyles.Headline, color = AppTheme.colors.accent)
        }
    } else {
        Box(
            modifier = modifier
                .heightIn(min = Sizes.secondaryButtonHeight)
                .clickableNoRipple(onClick)
                .padding(horizontal = Spacing.sm, vertical = Spacing.sm),
            contentAlignment = Alignment.Center,
        ) {
            Text(text, style = IOSTextStyles.Body, color = AppTheme.colors.accent)
        }
    }
}

// ════════════════════════════════════════════════════════════════════
// 11. IOSFormTextField(iOS 表单文本输入行)
// ════════════════════════════════════════════════════════════════════

/**
 * iOS 设置表单风格的文本输入行:左标签(可空)+ 右侧无边框输入区。
 * 用 BasicTextField 去除 Material 下划线/填充,贴近 iOS 观感。
 */
@Composable
fun IOSFormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.rowHorizontal, vertical = Spacing.rowVertical),
    ) {
        if (label != null) {
            Text(label, style = IOSTextStyles.Footnote, color = AppTheme.colors.secondaryLabel)
            androidx.compose.foundation.layout.Spacer(Modifier.size(Spacing.xs))
        }
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            minLines = minLines,
            visualTransformation = visualTransformation,
            textStyle = IOSTextStyles.Body.merge(androidx.compose.ui.text.TextStyle(color = AppTheme.colors.label)),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(AppTheme.colors.accent),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(placeholder, style = IOSTextStyles.Body, color = AppTheme.colors.tertiaryLabel)
                }
                inner()
            },
        )
    }
}

// ════════════════════════════════════════════════════════════════════
// 12. IOSFormPickerRow(点击展开选项的表单行)
// ════════════════════════════════════════════════════════════════════

/**
 * 表单选择行:左标题 + 右当前值 + chevron;点击在行下展开横向胶囊选项。
 */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun <T> IOSFormPickerRow(
    label: String,
    options: List<T>,
    selected: T,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    androidx.compose.foundation.layout.Column(modifier.fillMaxWidth()) {
        IOSListRow(
            title = label,
            value = optionLabel(selected),
            showChevron = true,
            onClick = { expanded = !expanded },
        )
        if (expanded) {
            androidx.compose.foundation.layout.FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.rowHorizontal, vertical = Spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                options.forEach { opt ->
                    val isSel = opt == selected
                    IOSBadge(
                        text = optionLabel(opt),
                        container = if (isSel) AppTheme.colors.accent else AppTheme.colors.accentSoft,
                        content = if (isSel) androidx.compose.ui.graphics.Color.White else AppTheme.colors.accent,
                        modifier = Modifier.clickableNoRipple {
                            onSelect(opt)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════
// 13. IOSToggleRow(iOS 设置开关行:左标题 右 Switch)
// ════════════════════════════════════════════════════════════════════

@Composable
fun IOSToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Sizes.listRowMinHeight)
            .padding(horizontal = Spacing.rowHorizontal, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        androidx.compose.foundation.layout.Column(Modifier.weight(1f)) {
            Text(title, style = IOSTextStyles.Body, color = AppTheme.colors.label)
            if (subtitle != null) {
                Text(subtitle, style = IOSTextStyles.Footnote, color = AppTheme.colors.secondaryLabel)
            }
        }
        androidx.compose.material3.Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = androidx.compose.material3.SwitchDefaults.colors(
                checkedTrackColor = AppTheme.colors.accent,
                checkedThumbColor = androidx.compose.ui.graphics.Color.White,
            ),
        )
    }
}
