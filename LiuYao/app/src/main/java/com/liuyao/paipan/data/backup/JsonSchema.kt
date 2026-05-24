package com.liuyao.paipan.data.backup

import com.liuyao.paipan.data.db.Converters
import com.liuyao.paipan.data.db.entity.CaseEntity
import com.liuyao.paipan.data.db.entity.CaseFeedbackEntity
import com.liuyao.paipan.domain.model.DivinationCategory
import com.liuyao.paipan.domain.model.SixKin
import com.liuyao.paipan.domain.rule.DivinationRule
import com.liuyao.paipan.domain.rule.RulePolarity
import com.liuyao.paipan.domain.rule.RuleTarget
import org.json.JSONArray
import org.json.JSONObject

/**
 * 备份文件的 JSON 结构约定与编解码。
 *
 * 顶层信封:
 * ```
 * { "schema": "liuyao.backup", "version": 1, "type": "rules|cases", "exportedAt": <epoch>, "items": [ ... ] }
 * ```
 * RuleCondition 复用 [Converters] 的编解码,保证结构化条件无损往返。
 *
 * 纯函数,独立可测,不依赖 Android/DB。
 */
object JsonSchema {

    const val SCHEMA = "liuyao.backup"
    const val VERSION = 1
    const val TYPE_RULES = "rules"
    const val TYPE_CASES = "cases"

    // ─────────────── 信封 ───────────────

    fun envelope(type: String, items: JSONArray): JSONObject = JSONObject()
        .put("schema", SCHEMA)
        .put("version", VERSION)
        .put("type", type)
        .put("exportedAt", System.currentTimeMillis() / 1000)
        .put("count", items.length())
        .put("items", items)

    /** 校验并取出 items;schema/type 不符则抛异常 */
    fun readItems(root: JSONObject, expectedType: String): JSONArray {
        val schema = root.optString("schema")
        require(schema == SCHEMA) { "不是有效的备份文件(schema=$schema)" }
        val type = root.optString("type")
        require(type == expectedType) { "备份类型不符:期望 $expectedType,实际 $type" }
        return root.optJSONArray("items") ?: JSONArray()
    }

    // ─────────────── 断语 ───────────────

    fun rulesToJson(rules: List<DivinationRule>): String {
        val arr = JSONArray()
        rules.forEach { arr.put(ruleToJson(it)) }
        return envelope(TYPE_RULES, arr).toString(2)
    }

    fun ruleToJson(r: DivinationRule): JSONObject {
        val matches = JSONArray().apply { r.matchConditions.forEach { put(Converters.encodeCondition(it)) } }
        val excludes = JSONArray().apply { r.excludeConditions.forEach { put(Converters.encodeCondition(it)) } }
        return JSONObject()
            .put("id", r.id)
            .put("sourceId", r.sourceId)
            .put("sourceName", r.sourceName)
            .put("category", r.category.name)
            .put("targetType", r.target.type.name)
            .put("targetKin", r.target.kin?.name ?: JSONObject.NULL)
            .put("targetPosition", r.target.position ?: JSONObject.NULL)
            .put("originalText", r.originalText)
            .put("plainExplanation", r.plainExplanation)
            .put("conditionText", r.conditionText)
            .put("positiveMeaning", r.positiveMeaning ?: JSONObject.NULL)
            .put("negativeMeaning", r.negativeMeaning ?: JSONObject.NULL)
            .put("matchConditions", matches)
            .put("excludeConditions", excludes)
            .put("polarity", r.polarity.name)
            .put("priority", r.priority)
            .put("confidenceWeight", r.confidenceWeight)
            .put("tags", JSONArray(r.tags))
    }

    fun ruleFromJson(o: JSONObject): DivinationRule {
        val matches = o.optJSONArray("matchConditions") ?: JSONArray()
        val excludes = o.optJSONArray("excludeConditions") ?: JSONArray()
        val tagsArr = o.optJSONArray("tags") ?: JSONArray()
        return DivinationRule(
            id = o.getString("id"),
            sourceId = o.optString("sourceId", "src-import"),
            sourceName = o.optString("sourceName", "导入"),
            category = runCatching { DivinationCategory.valueOf(o.getString("category")) }.getOrDefault(DivinationCategory.OTHER),
            target = RuleTarget(
                type = runCatching { RuleTarget.Type.valueOf(o.getString("targetType")) }.getOrDefault(RuleTarget.Type.USE_GOD),
                kin = o.optStringOrNull("targetKin")?.let { runCatching { SixKin.valueOf(it) }.getOrNull() },
                position = if (o.isNull("targetPosition")) null else o.optInt("targetPosition").takeIf { o.has("targetPosition") },
            ),
            originalText = o.optString("originalText"),
            plainExplanation = o.optString("plainExplanation"),
            conditionText = o.optString("conditionText"),
            positiveMeaning = o.optStringOrNull("positiveMeaning"),
            negativeMeaning = o.optStringOrNull("negativeMeaning"),
            matchConditions = (0 until matches.length()).mapNotNull { Converters.decodeCondition(matches.getJSONObject(it)) },
            excludeConditions = (0 until excludes.length()).mapNotNull { Converters.decodeCondition(excludes.getJSONObject(it)) },
            polarity = runCatching { RulePolarity.valueOf(o.getString("polarity")) }.getOrDefault(RulePolarity.NEUTRAL),
            priority = o.optInt("priority", 50),
            confidenceWeight = o.optDouble("confidenceWeight", 0.5),
            tags = (0 until tagsArr.length()).map { tagsArr.getString(it) },
        )
    }

    // ─────────────── 案例 ───────────────

    fun casesToJson(cases: List<Pair<CaseEntity, CaseFeedbackEntity?>>): String {
        val arr = JSONArray()
        cases.forEach { (c, fb) -> arr.put(caseToJson(c, fb)) }
        return envelope(TYPE_CASES, arr).toString(2)
    }

    fun caseToJson(c: CaseEntity, fb: CaseFeedbackEntity?): JSONObject {
        val obj = JSONObject()
            .put("id", c.id)
            .put("chartId", c.chartId)
            .put("title", c.title)
            .put("createdEpoch", c.createdEpoch)
            .put("category", c.category ?: JSONObject.NULL)
            .put("question", c.question)
            .put("castEpoch", c.castEpoch)
            .put("favorite", c.favorite)
            .put("hitRuleIdsCsv", c.hitRuleIdsCsv)
        if (fb != null) {
            obj.put(
                "feedback",
                JSONObject()
                    .put("verdict", fb.verdict)
                    .put("actualResult", fb.actualResult)
                    .put("note", fb.note)
                    .put("feedbackEpoch", fb.feedbackEpoch)
                    .put("hitRuleIdsCsv", fb.hitRuleIdsCsv)
                    .put("missRuleIdsCsv", fb.missRuleIdsCsv),
            )
        }
        return obj
    }

    fun caseFromJson(o: JSONObject): Pair<CaseEntity, CaseFeedbackEntity?> {
        val c = CaseEntity(
            id = o.getString("id"),
            chartId = o.optString("chartId"),
            title = o.optString("title", "未命名案例"),
            createdEpoch = o.optLong("createdEpoch"),
            category = o.optStringOrNull("category"),
            question = o.optString("question"),
            castEpoch = o.optLong("castEpoch"),
            favorite = o.optBoolean("favorite", false),
            hitRuleIdsCsv = o.optString("hitRuleIdsCsv"),
        )
        val fbObj = o.optJSONObject("feedback")
        val fb = fbObj?.let {
            CaseFeedbackEntity(
                caseId = c.id,
                verdict = it.optString("verdict"),
                actualResult = it.optString("actualResult"),
                note = it.optString("note"),
                feedbackEpoch = it.optLong("feedbackEpoch"),
                hitRuleIdsCsv = it.optString("hitRuleIdsCsv"),
                missRuleIdsCsv = it.optString("missRuleIdsCsv"),
            )
        }
        return c to fb
    }
}

/** org.json 的 optString 对 null 返回 "null" 字符串,这里返回真正的 null */
private fun JSONObject.optStringOrNull(key: String): String? =
    if (isNull(key) || !has(key)) null else optString(key).takeIf { it.isNotEmpty() }
