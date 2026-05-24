package com.liuyao.paipan.data.db

import com.liuyao.paipan.data.db.entity.RuleConditionEntity
import com.liuyao.paipan.data.db.entity.RuleEntity
import com.liuyao.paipan.data.db.entity.RuleSourceEntity
import com.liuyao.paipan.data.db.entity.RuleTagEntity

/**
 * DAO 调用示例(供联调/参考)。展示绕过 Repository、直接用 DAO 的增删查写法。
 * 实际业务建议走 [LiuYaoRepository](自带领域映射)。所有方法需在协程中调用。
 */
object RuleDaoUsage {

    /** 插入一条规则 + 来源 + 一个条件 + 一个标签。 */
    suspend fun insertExample(db: AppDatabase) {
        val dao = db.ruleDao()
        dao.upsertSource(RuleSourceEntity("src-demo", "刘昌明", "象断六爻", "婚姻篇"))
        dao.upsertRule(
            RuleEntity(
                id = "demo-001",
                sourceId = "src-demo",
                sourceName = "刘昌明《象断六爻》· 婚姻篇",
                category = "MARRIAGE",
                targetType = "USE_GOD",
                targetKin = null,
                targetPosition = null,
                originalText = "世应相生,婚事易成。",
                plainExplanation = "世爻与应爻相生,双方有意,婚姻多能成。",
                conditionText = "世应相生",
                positiveMeaning = "婚成",
                negativeMeaning = null,
                polarity = "AUSPICIOUS",
                priority = 60,
                confidenceWeight = 0.7,
                tagsCsv = "world_response",
                explPlain = "世应相生主和合。",
                explMechanism = null, explExample = null, explCaveat = null,
                hintRequireAll = true, hintMinScore = 55, hintApplyTiming = null,
            ),
        )
        dao.insertConditions(
            listOf(
                RuleConditionEntity(ruleId = "demo-001", orderIndex = 0, payloadJson = """{"t":"WorldResponseGenerate"}"""),
            ),
        )
        dao.insertTags(listOf(RuleTagEntity(ruleId = "demo-001", tag = "world_response")))
    }

    /** 按占类查询。 */
    suspend fun queryByCategory(db: AppDatabase): List<RuleEntity> =
        db.ruleDao().rulesByCategory("MARRIAGE")

    /** 按标签查询。 */
    suspend fun queryByTag(db: AppDatabase): List<RuleEntity> =
        db.ruleDao().rulesByTag("world_response")

    /** 删除(外键 CASCADE 会清子表,这里显式删一遍更稳)。 */
    suspend fun deleteExample(db: AppDatabase) {
        val dao = db.ruleDao()
        dao.deleteConditions("demo-001")
        dao.deleteExcludeConditions("demo-001")
        dao.deleteTags("demo-001")
        dao.deleteRule("demo-001")
    }
}
