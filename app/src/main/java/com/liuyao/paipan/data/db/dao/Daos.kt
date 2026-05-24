package com.liuyao.paipan.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.liuyao.paipan.data.db.entity.CaseEntity
import com.liuyao.paipan.data.db.entity.CaseFeedbackEntity
import com.liuyao.paipan.data.db.entity.ChartEntity
import com.liuyao.paipan.data.db.entity.ChartLineEntity
import com.liuyao.paipan.data.db.entity.RuleConditionEntity
import com.liuyao.paipan.data.db.entity.RuleEntity
import com.liuyao.paipan.data.db.entity.RuleExcludeConditionEntity
import com.liuyao.paipan.data.db.entity.RuleSourceEntity
import com.liuyao.paipan.data.db.entity.RuleStatsEntity
import com.liuyao.paipan.data.db.entity.RuleTagEntity

@Dao
interface RuleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSource(source: RuleSourceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRule(rule: RuleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConditions(items: List<RuleConditionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExcludeConditions(items: List<RuleExcludeConditionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(items: List<RuleTagEntity>)

    @Query("SELECT * FROM divination_rules WHERE id = :id")
    suspend fun ruleById(id: String): RuleEntity?

    @Query("SELECT * FROM divination_rules WHERE category = :category ORDER BY priority DESC")
    suspend fun rulesByCategory(category: String): List<RuleEntity>

    @Query("SELECT * FROM rule_conditions WHERE ruleId = :ruleId ORDER BY orderIndex")
    suspend fun conditionsOf(ruleId: String): List<RuleConditionEntity>

    @Query("SELECT * FROM rule_exclude_conditions WHERE ruleId = :ruleId ORDER BY orderIndex")
    suspend fun excludeConditionsOf(ruleId: String): List<RuleExcludeConditionEntity>

    @Query(
        "SELECT r.* FROM divination_rules r " +
            "INNER JOIN rule_tags t ON r.id = t.ruleId WHERE t.tag = :tag ORDER BY r.priority DESC",
    )
    suspend fun rulesByTag(tag: String): List<RuleEntity>

    @Query("SELECT * FROM divination_rules ORDER BY priority DESC")
    suspend fun allRules(): List<RuleEntity>

    @Query("DELETE FROM divination_rules WHERE id = :id")
    suspend fun deleteRule(id: String)

    @Query("DELETE FROM rule_conditions WHERE ruleId = :ruleId")
    suspend fun deleteConditions(ruleId: String)

    @Query("DELETE FROM rule_exclude_conditions WHERE ruleId = :ruleId")
    suspend fun deleteExcludeConditions(ruleId: String)

    @Query("DELETE FROM rule_tags WHERE ruleId = :ruleId")
    suspend fun deleteTags(ruleId: String)
}

@Dao
interface ChartDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertChart(chart: ChartEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLines(lines: List<ChartLineEntity>)

    @Transaction
    suspend fun saveChart(chart: ChartEntity, lines: List<ChartLineEntity>) {
        upsertChart(chart)
        insertLines(lines)
    }

    @Query("SELECT * FROM charts WHERE id = :id")
    suspend fun chartById(id: String): ChartEntity?

    @Query("SELECT * FROM chart_lines WHERE chartId = :chartId ORDER BY position")
    suspend fun linesOf(chartId: String): List<ChartLineEntity>

    @Query("SELECT * FROM charts ORDER BY dateTimeEpoch DESC")
    suspend fun allCharts(): List<ChartEntity>
}

@Dao
interface CaseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCase(case: CaseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFeedback(feedback: CaseFeedbackEntity)

    @Query("SELECT * FROM cases ORDER BY createdEpoch DESC")
    suspend fun allCases(): List<CaseEntity>

    @Query("SELECT * FROM cases WHERE id = :id")
    suspend fun caseById(id: String): CaseEntity?

    @Query("SELECT * FROM cases WHERE category = :category ORDER BY createdEpoch DESC")
    suspend fun casesByCategory(category: String): List<CaseEntity>

    @Query(
        "SELECT * FROM cases WHERE title LIKE '%' || :q || '%' OR question LIKE '%' || :q || '%' " +
            "ORDER BY createdEpoch DESC",
    )
    suspend fun searchCases(q: String): List<CaseEntity>

    @Query("UPDATE cases SET favorite = :fav WHERE id = :id")
    suspend fun setFavorite(id: String, fav: Boolean)

    @Query("DELETE FROM cases WHERE id = :id")
    suspend fun deleteCase(id: String)

    @Query("SELECT * FROM case_feedback WHERE caseId = :caseId LIMIT 1")
    suspend fun feedbackOf(caseId: String): CaseFeedbackEntity?
}

@Dao
interface StatsDao {
    @Upsert
    suspend fun upsert(stats: RuleStatsEntity)

    @Query("SELECT * FROM rule_stats WHERE ruleId = :ruleId")
    suspend fun statsOf(ruleId: String): RuleStatsEntity?

    @Query("SELECT * FROM rule_stats ORDER BY hitCount DESC")
    suspend fun allStats(): List<RuleStatsEntity>

    /** 累加命中/验中/误判计数(不存在则由 Repository 先建) */
    @Query(
        "UPDATE rule_stats SET matchedCount = matchedCount + :dMatched, " +
            "hitCount = hitCount + :dHit, missCount = missCount + :dMiss, " +
            "lastUpdatedEpoch = :epoch WHERE ruleId = :ruleId",
    )
    suspend fun bump(ruleId: String, dMatched: Int, dHit: Int, dMiss: Int, epoch: Long)

    /** 记录"被使用一次"(匹配命中),更新使用次数与最近使用时间 */
    @Query(
        "UPDATE rule_stats SET matchedCount = matchedCount + 1, " +
            "lastUsedEpoch = :epoch, lastUpdatedEpoch = :epoch WHERE ruleId = :ruleId",
    )
    suspend fun markUsed(ruleId: String, epoch: Long)

    /** 记录一次反馈结果:按结果类型累加对应计数,并记最近反馈 */
    @Query(
        "UPDATE rule_stats SET " +
            "hitCount = hitCount + :dHit, missCount = missCount + :dMiss, " +
            "partialCount = partialCount + :dPartial, unknownCount = unknownCount + :dUnknown, " +
            "lastVerdict = :verdict, lastUpdatedEpoch = :epoch WHERE ruleId = :ruleId",
    )
    suspend fun recordFeedback(
        ruleId: String,
        dHit: Int,
        dMiss: Int,
        dPartial: Int,
        dUnknown: Int,
        verdict: String,
        epoch: Long,
    )
}
