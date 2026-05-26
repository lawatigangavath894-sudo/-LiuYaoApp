package com.liuyao.paipan.nav

/** 路由集中定义,便于后续加参数化路由 */
sealed class Route(val route: String) {
    data object Home : Route("home")
    data object AiChat : Route("ai_chat") {
        const val pattern = "ai_chat?chartId={chartId}"
        fun create(chartId: String? = null) = if (chartId.isNullOrBlank()) "ai_chat" else "ai_chat?chartId=$chartId"
    }
    data object AiSettings : Route("ai_settings")
    data object Rules : Route("rules")
    data object Cases : Route("cases")
    data object Settings : Route("settings")
    data object Cast : Route("cast")
    data object Chart : Route("chart")

    /** 断语详情:rule_detail/{ruleId} */
    data object RuleDetail : Route("rule_detail/{ruleId}") {
        fun create(ruleId: String) = "rule_detail/$ruleId"
    }

    /** 断语编辑:rule_edit?ruleId={ruleId};ruleId 缺省为新增 */
    data object RuleEdit : Route("rule_edit?ruleId={ruleId}") {
        fun create(ruleId: String? = null) =
            if (ruleId == null) "rule_edit?ruleId=" else "rule_edit?ruleId=$ruleId"
    }

    /** 断语导入入口 */
    data object RuleImport : Route("rule_import")

    /** 断语导入预览 */
    data object ImportPreview : Route("import_preview")

    /** 案例详情:case_detail/{caseId} */
    data object CaseDetail : Route("case_detail/{caseId}") {
        fun create(caseId: String) = "case_detail/$caseId"
    }

    /** 数据导出与备份 */
    data object Backup : Route("backup")

    /** 从 JSON 恢复 */
    data object Restore : Route("restore")

    companion object {
        /** 显示底部 TabBar 的一级路由 */
        val tabRoutes = setOf("home", "ai_chat", "rules", "cases", "settings")
    }
}
