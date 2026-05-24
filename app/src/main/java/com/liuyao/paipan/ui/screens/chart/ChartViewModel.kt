package com.liuyao.paipan.ui.screens.chart

import androidx.lifecycle.ViewModel
import com.liuyao.paipan.domain.engine.ChartInput
import com.liuyao.paipan.domain.engine.LiuYaoChartEngine
import com.liuyao.paipan.domain.model.DivinationMethod
import com.liuyao.paipan.domain.model.LiuYaoChart
import com.liuyao.paipan.domain.model.YinYang
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime
import kotlin.random.Random

/**
 * 排盘页 UI 状态。
 *
 * UI 仅读取本状态;一切排盘计算由 [LiuYaoChartEngine] 完成,ViewModel 不含术数逻辑。
 */
data class ChartUiState(
    val chart: LiuYaoChart? = null,
    val warnings: List<String> = emptyList(),
    val isLoading: Boolean = false,
) {
    val hasChart: Boolean get() = chart != null
}

/**
 * 起卦/排盘的用户意图。Cast 页通过这些 Action 驱动 ViewModel,不直接碰引擎。
 */
sealed interface ChartAction {
    /** 快速起卦:用当前时间随机生成六爻与动爻 */
    data class QuickCast(val question: String) : ChartAction

    /** 手动起卦:显式给六爻阴阳与动爻 */
    data class ManualCast(
        val question: String,
        val yangFlags: List<Boolean>, // 长度6,index0=初爻,true=阳
        val moving: List<Boolean>,    // 长度6,true=动爻
    ) : ChartAction

    /** 清空当前排盘 */
    data object Clear : ChartAction
}

/**
 * 排盘 ViewModel。
 *
 * 提升到导航图作用域共享:Cast 触发起卦写入状态,Chart 读取同一状态渲染。
 * 无 Hilt、无 Room。
 */
class ChartViewModel : ViewModel() {

    private val _ui = MutableStateFlow(ChartUiState())
    val ui: StateFlow<ChartUiState> = _ui.asStateFlow()

    fun dispatch(action: ChartAction) {
        when (action) {
            is ChartAction.QuickCast -> quickCast(action.question)
            is ChartAction.ManualCast -> manualCast(action.question, action.yangFlags, action.moving)
            ChartAction.Clear -> _ui.update { ChartUiState() }
        }
    }

    private fun quickCast(question: String) {
        // 随机六爻:每爻独立"摇"出老阴/少阴/少阳/老阳,老变为动爻。
        val yang = ArrayList<Boolean>(6)
        val moving = ArrayList<Boolean>(6)
        repeat(6) {
            when (Random.nextInt(4)) {
                0 -> { yang.add(false); moving.add(true) }  // 老阴(动)
                1 -> { yang.add(false); moving.add(false) } // 少阴
                2 -> { yang.add(true); moving.add(false) }  // 少阳
                else -> { yang.add(true); moving.add(true) } // 老阳(动)
            }
        }
        runEngine(question.ifBlank { "快速起卦" }, yang, moving, DivinationMethod.SolarTime)
    }

    private fun manualCast(question: String, yangFlags: List<Boolean>, moving: List<Boolean>) {
        runEngine(
            question.ifBlank { "手动起卦" },
            yangFlags,
            moving,
            DivinationMethod.Manual(raw = encode(yangFlags, moving)),
        )
    }

    private fun runEngine(
        question: String,
        yangFlags: List<Boolean>,
        moving: List<Boolean>,
        method: DivinationMethod,
    ) {
        _ui.update { it.copy(isLoading = true) }
        val input = ChartInput(
            dateTime = LocalDateTime.now(),
            lines = yangFlags.map { if (it) YinYang.YANG else YinYang.YIN },
            moving = moving,
            question = question,
            method = method,
        )
        val result = LiuYaoChartEngine.build(input)
        _ui.update {
            it.copy(chart = result.chart, warnings = result.warnings, isLoading = false)
        }
    }

    private fun encode(yang: List<Boolean>, moving: List<Boolean>): String =
        (0 until 6).joinToString("") { i ->
            when {
                yang[i] && moving[i] -> "O"  // 老阳
                yang[i] -> "+"               // 少阳
                !yang[i] && moving[i] -> "X" // 老阴
                else -> "-"                  // 少阴
            }
        }
}
