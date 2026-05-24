package com.liuyao.paipan.data.db

import androidx.room.TypeConverter
import com.liuyao.paipan.domain.model.EarthlyBranch
import com.liuyao.paipan.domain.model.SixGod
import com.liuyao.paipan.domain.model.SixKin
import com.liuyao.paipan.domain.rule.RuleCondition
import com.liuyao.paipan.domain.rule.RuleTarget
import org.json.JSONArray
import org.json.JSONObject

/**
 * Room 类型转换器。
 *
 * 取舍:
 *  - 简单集合(List<String>、List<EarthlyBranch>)用分隔符字符串,轻量直观。
 *  - 复杂异构结构([RuleCondition] 列表)用 JSON 文本整存(org.json,Android 内置,
 *    不引第三方序列化依赖)。条件不需被 SQL 单独检索,故无需拆表。
 */
class Converters {

    // —— List<String> ——
    @TypeConverter
    fun stringListToText(list: List<String>?): String = list?.joinToString(SEP) ?: ""

    @TypeConverter
    fun textToStringList(text: String?): List<String> =
        if (text.isNullOrEmpty()) emptyList() else text.split(SEP)

    // —— List<EarthlyBranch>(存中文单字) ——
    @TypeConverter
    fun branchListToText(list: List<EarthlyBranch>?): String =
        list?.joinToString("") { it.cn } ?: ""

    @TypeConverter
    fun textToBranchList(text: String?): List<EarthlyBranch> =
        if (text.isNullOrEmpty()) emptyList() else text.map { EarthlyBranch.fromCn(it.toString()) }

    // —— List<RuleCondition> as JSON ——
    @TypeConverter
    fun conditionsToJson(list: List<RuleCondition>?): String {
        val arr = JSONArray()
        list?.forEach { arr.put(encodeCondition(it)) }
        return arr.toString()
    }

    @TypeConverter
    fun jsonToConditions(json: String?): List<RuleCondition> {
        if (json.isNullOrEmpty()) return emptyList()
        val arr = JSONArray(json)
        return (0 until arr.length()).mapNotNull { decodeCondition(arr.getJSONObject(it)) }
    }

    companion object {
        private const val SEP = "\u001F" // 单元分隔符,避免与内容冲突

        // ---- RuleCondition 编解码(覆盖 20 种) ----
        fun encodeCondition(c: RuleCondition): JSONObject {
            val o = JSONObject()
            when (c) {
                is RuleCondition.KinPresent -> o.put("t", "KinPresent").put("kin", c.kin.name)
                is RuleCondition.KinAbsent -> o.put("t", "KinAbsent").put("kin", c.kin.name)
                RuleCondition.UseGodMoving -> o.put("t", "UseGodMoving")
                RuleCondition.UseGodVoid -> o.put("t", "UseGodVoid")
                RuleCondition.UseGodMonthBroken -> o.put("t", "UseGodMonthBroken")
                RuleCondition.UseGodDayClashed -> o.put("t", "UseGodDayClashed")
                RuleCondition.UseGodSupportedByMonth -> o.put("t", "UseGodSupportedByMonth")
                RuleCondition.UseGodSupportedByDay -> o.put("t", "UseGodSupportedByDay")
                RuleCondition.WorldVoid -> o.put("t", "WorldVoid")
                RuleCondition.ResponseVoid -> o.put("t", "ResponseVoid")
                RuleCondition.WorldResponseGenerate -> o.put("t", "WorldResponseGenerate")
                RuleCondition.WorldResponseRestrain -> o.put("t", "WorldResponseRestrain")
                RuleCondition.BackGenerate -> o.put("t", "BackGenerate")
                RuleCondition.BackRestrain -> o.put("t", "BackRestrain")
                is RuleCondition.SixGodPresent -> {
                    o.put("t", "SixGodPresent").put("god", c.sixGod.name)
                    c.onTarget?.let { o.put("target", encodeTarget(it)) }
                }
                is RuleCondition.BranchClash -> o.put("t", "BranchClash")
                    .put("a", c.a?.cn ?: "").put("b", c.b?.cn ?: "")
                is RuleCondition.BranchCombine -> o.put("t", "BranchCombine")
                    .put("a", c.a?.cn ?: "").put("b", c.b?.cn ?: "")
                is RuleCondition.AtPosition -> {
                    o.put("t", "AtPosition").put("pos", c.position)
                    c.inner?.let { o.put("inner", encodeCondition(it)) }
                }
                is RuleCondition.HiddenSpiritPresent -> o.put("t", "HiddenSpiritPresent")
                    .put("kin", c.kin?.name ?: "")
                is RuleCondition.FlyingSuppressesHidden -> o.put("t", "FlyingSuppressesHidden")
                    .put("kin", c.hiddenKin?.name ?: "")
            }
            return o
        }

        fun decodeCondition(o: JSONObject): RuleCondition? {
            fun kin(key: String) = o.optString(key, "").takeIf { it.isNotEmpty() }?.let { SixKin.valueOf(it) }
            fun branch(key: String) = o.optString(key, "").takeIf { it.isNotEmpty() }?.let { EarthlyBranch.fromCn(it) }
            return when (o.getString("t")) {
                "KinPresent" -> RuleCondition.KinPresent(SixKin.valueOf(o.getString("kin")))
                "KinAbsent" -> RuleCondition.KinAbsent(SixKin.valueOf(o.getString("kin")))
                "UseGodMoving" -> RuleCondition.UseGodMoving
                "UseGodVoid" -> RuleCondition.UseGodVoid
                "UseGodMonthBroken" -> RuleCondition.UseGodMonthBroken
                "UseGodDayClashed" -> RuleCondition.UseGodDayClashed
                "UseGodSupportedByMonth" -> RuleCondition.UseGodSupportedByMonth
                "UseGodSupportedByDay" -> RuleCondition.UseGodSupportedByDay
                "WorldVoid" -> RuleCondition.WorldVoid
                "ResponseVoid" -> RuleCondition.ResponseVoid
                "WorldResponseGenerate" -> RuleCondition.WorldResponseGenerate
                "WorldResponseRestrain" -> RuleCondition.WorldResponseRestrain
                "BackGenerate" -> RuleCondition.BackGenerate
                "BackRestrain" -> RuleCondition.BackRestrain
                "SixGodPresent" -> RuleCondition.SixGodPresent(
                    SixGod.valueOf(o.getString("god")),
                    o.optJSONObject("target")?.let { decodeTarget(it) },
                )
                "BranchClash" -> RuleCondition.BranchClash(branch("a"), branch("b"))
                "BranchCombine" -> RuleCondition.BranchCombine(branch("a"), branch("b"))
                "AtPosition" -> RuleCondition.AtPosition(
                    o.getInt("pos"),
                    o.optJSONObject("inner")?.let { decodeCondition(it) },
                )
                "HiddenSpiritPresent" -> RuleCondition.HiddenSpiritPresent(kin("kin"))
                "FlyingSuppressesHidden" -> RuleCondition.FlyingSuppressesHidden(kin("kin"))
                else -> null
            }
        }

        private fun encodeTarget(t: RuleTarget): JSONObject = JSONObject()
            .put("type", t.type.name)
            .apply {
                t.kin?.let { put("kin", it.name) }
                t.position?.let { put("pos", it) }
            }

        private fun decodeTarget(o: JSONObject): RuleTarget = RuleTarget(
            type = RuleTarget.Type.valueOf(o.getString("type")),
            kin = o.optString("kin", "").takeIf { it.isNotEmpty() }?.let { SixKin.valueOf(it) },
            position = if (o.has("pos")) o.getInt("pos") else null,
        )
    }
}
