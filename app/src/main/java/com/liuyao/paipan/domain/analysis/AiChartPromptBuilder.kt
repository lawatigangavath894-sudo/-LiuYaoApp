package com.liuyao.paipan.domain.analysis

import com.liuyao.paipan.domain.match.MatchReport
import com.liuyao.paipan.domain.match.RuleMatchResult
import com.liuyao.paipan.domain.model.LiuYaoChart

object AiChartPromptBuilder {
    fun build(chart: LiuYaoChart, lock: AnalysisLock, report: MatchReport?): String = buildString {
        appendLine("【角色】")
        appendLine("你是六爻排盘与刘昌明断语资料辅助解析助手。必须严格依据当前排盘、占事问题、分析锁定结果和下方资料片段分析。不要脱离资料自由发挥。资料不足时必须说明“资料不足”。")
        appendLine()
        appendLine("【占事】")
        appendLine("占事类别：${lock.category.displayName()}")
        appendLine("占事问题：${lock.question}")
        appendLine("主变量：${lock.mainVariable}")
        appendLine()
        appendLine("【分析锁定】")
        appendLine("主用神：${lock.primaryUsefulGod?.displayName() ?: "未锁定"}")
        appendLine("辅助用神：${lock.secondaryUsefulGods.joinToString("、") { it.displayName() }.ifBlank { "无" }}")
        appendLine("世爻：${lock.worldLineIndex ?: "未定"}")
        appendLine("应爻：${lock.responseLineIndex ?: "未定"}")
        appendLine("关键爻：${lock.keyLineIndexes.joinToString("、").ifBlank { "未定" }}")
        appendLine("动爻：${lock.movingLineIndexes.joinToString("、").ifBlank { "无" }}")
        appendLine("伏神/飞神相关爻：${(lock.hiddenSpiritLineIndexes + lock.flyingSpiritLineIndexes).distinct().joinToString("、").ifBlank { "无" }}")
        appendLine("锁定理由：${lock.lockReason}")
        appendLine("资料不足：${lock.uncertainReason ?: "否"}")
        appendLine("资料来源：${lock.knowledgeSnippets.joinToString("、") { it.sourceName }.ifBlank { "未检索到" }}")
        appendLine()
        appendLine("【排盘信息】")
        appendLine("起卦时间：${chart.dateTime}")
        appendLine("年月日时干支：${chart.yearGanZhi.displayName()} ${chart.monthGanZhi.displayName()} ${chart.dayGanZhi.displayName()} ${chart.hourGanZhi.displayName()}")
        appendLine("旬空：${chart.xunKong.joinToString("、") { it.displayName() }}")
        appendLine("本卦：${chart.originalHexagram.name}")
        appendLine("变卦：${chart.changedHexagram?.name ?: "无变卦"}")
        appendLine("卦宫：${chart.palace.displayName()}")
        appendLine("六冲六合：${if (chart.isSixClash) "六冲" else if (chart.isSixCombine) "六合" else "无"}")
        appendLine("动爻：${chart.movingLines.joinToString("、") { it.index.toString() }.ifBlank { "无" }}")
        appendLine("起卦方式：${chart.method.cn}")
        appendLine("六爻明细（上爻到初爻）：")
        chart.lines.sortedByDescending { it.index }.forEach { line ->
            appendLine(
                "${line.index}爻 ${line.sixGod.displayName()} ${line.sixKin.displayName()} " +
                    "${line.naJia.displayName()} ${line.element.displayName()} " +
                    "${if (line.isWorld) "世" else if (line.isResponse) "应" else ""} " +
                    "${if (line.isMoving) "动" else "静"} " +
                    "变爻=${line.changedLine?.sixKin?.displayName() ?: "无"} " +
                    "伏神=${line.hiddenSpirit?.sixKin?.displayName() ?: "无"} " +
                    "飞神=${line.flyingSpirit?.sixKin?.displayName() ?: "无"} " +
                    "空亡=${line.status.isVoid} 月破=${line.status.isMonthBroken} 日冲=${line.status.isDayClashed} " +
                    "合=${line.status.isCombined} 冲=${line.status.isClashed} 刑=${line.status.isPunished} 害=${line.status.isHarmed} " +
                    "旺衰=${line.status.strength.displayName()}",
            )
        }
        appendLine()
        appendLine("【检索到的资料片段】")
        if (lock.knowledgeSnippets.isEmpty()) {
            appendLine("当前未检索到本地刘昌明资料片段，请明确说明资料不足。")
        } else {
            lock.knowledgeSnippets.take(10).forEachIndexed { index, item ->
                appendLine("${index + 1}. 来源：${item.sourceName}")
                appendLine("标题：${item.sectionTitle ?: "无"}")
                appendLine("关键词：${item.matchedKeywords.joinToString("、")}")
                appendLine("原文片段：${item.originalText}")
            }
        }
        appendLine()
        appendLine("【命中断语】")
        appendLayer("主结果断语", report?.mainResult.orEmpty())
        appendLayer("过程条件断语", report?.processOrCondition.orEmpty())
        appendLayer("旁参考断语", report?.sideReference.orEmpty())
        appendLine()
        appendLine("【输出要求】")
        appendLine("一、占事与主变量")
        appendLine("二、用神与世应锁定")
        appendLine("三、资料原文依据")
        appendLine("四、排盘关键矛盾")
        appendLine("五、主结果判断")
        appendLine("六、过程条件判断")
        appendLine("七、资料不足或不能断处")
        appendLine()
        appendLine("【禁止项】")
        appendLine("1. 不得分析无关占类；")
        appendLine("2. 不得把旁参考当主判断；")
        appendLine("3. 不得脱离资料乱编；")
        appendLine("4. 不得把过程象当最终结果；")
        appendLine("5. 不得忽略资料不足。")
    }

    private fun StringBuilder.appendLayer(title: String, items: List<RuleMatchResult>) {
        appendLine("$title：")
        if (items.isEmpty()) {
            appendLine("无")
        } else {
            items.take(8).forEachIndexed { index, item ->
                appendLine("${index + 1}. ${item.rule.originalText}")
                appendLine("   来源：${item.rule.sourceName}；理由：${item.lockReason}")
            }
        }
    }
}
