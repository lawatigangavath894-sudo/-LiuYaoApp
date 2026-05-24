package com.liuyao.paipan.data.backup

import com.liuyao.paipan.data.db.LiuYaoRepository
import com.liuyao.paipan.data.db.entity.RuleSourceEntity
import com.liuyao.paipan.domain.rule.DivinationRule
import org.json.JSONObject

/** 导入模式 */
enum class ImportMode(val cn: String) {
    MERGE("合并(按 id 覆盖同项,保留其余)"),
    REPLACE("覆盖(先清空再导入)"),
}

/** 导入预览:解析结果概览(执行前展示) */
data class ImportPreview(
    val type: String,        // JsonSchema.TYPE_RULES / TYPE_CASES
    val itemCount: Int,
    val valid: Boolean,
    val message: String,
)

/**
 * 导入管理:解析备份 JSON 并恢复到 Room。
 * 先 [preview] 校验与计数(供 UI 展示并让用户选择覆盖/合并),再 [restoreRules]/[restoreCases] 执行。
 */
class ImportManager(private val repo: LiuYaoRepository) {

    /** 解析预览(不写库) */
    fun preview(jsonText: String): ImportPreview {
        return try {
            val root = JSONObject(jsonText)
            val type = root.optString("type")
            val items = root.optJSONArray("items")
            val count = items?.length() ?: 0
            val okType = type == JsonSchema.TYPE_RULES || type == JsonSchema.TYPE_CASES
            if (root.optString("schema") != JsonSchema.SCHEMA || !okType) {
                ImportPreview(type, count, false, "无法识别的备份文件")
            } else {
                ImportPreview(type, count, true, "可导入 $count 条${if (type == JsonSchema.TYPE_RULES) "断语" else "案例"}")
            }
        } catch (e: Exception) {
            ImportPreview("", 0, false, "解析失败:${e.message}")
        }
    }

    /** 恢复断语库 */
    suspend fun restoreRules(jsonText: String, mode: ImportMode): Int {
        val root = JSONObject(jsonText)
        val items = JsonSchema.readItems(root, JsonSchema.TYPE_RULES)
        val rules = (0 until items.length()).map { JsonSchema.ruleFromJson(items.getJSONObject(it)) }

        if (mode == ImportMode.REPLACE) {
            repo.loadAllRules().forEach { repo.deleteRule(it.id) }
        }
        val source = RuleSourceEntity("src-import", "导入", "备份恢复", null)
        rules.forEachIndexed { i, r: DivinationRule ->
            repo.saveRule(r, source = if (i == 0) source else null)
        }
        return rules.size
    }

    /** 恢复案例库(含反馈) */
    suspend fun restoreCases(jsonText: String, mode: ImportMode): Int {
        val root = JSONObject(jsonText)
        val items = JsonSchema.readItems(root, JsonSchema.TYPE_CASES)
        val cases = (0 until items.length()).map { JsonSchema.caseFromJson(items.getJSONObject(it)) }

        if (mode == ImportMode.REPLACE) {
            repo.allCases().forEach { repo.deleteCase(it.id) }
        }
        cases.forEach { (c, fb) ->
            repo.saveCase(c)
            fb?.let { repo.saveFeedback(it) }
        }
        return cases.size
    }
}
