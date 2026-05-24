package com.liuyao.paipan.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room 实体(10 表)。
 *
 * 拆表/JSON 取舍:
 *  - [RuleConditionEntity] / [RuleExcludeConditionEntity]:条件本身用 JSON 列 [payloadJson]
 *    整存(条件异构、不需 SQL 单独检索);每条规则的条件可多行,故独立成表并外键关联规则。
 *  - 排盘拆为 [ChartEntity] + [ChartLineEntity](六爻每爻一行),便于按爻查询/统计;
 *    爻内异构小结构(状态/伏神/变爻)以少量 JSON 列承载。
 *  - [RuleStatsEntity] 独立累计命中/落空次数,服务后续权重修正。
 */

// 5. rule_sources
@Entity(tableName = "rule_sources")
data class RuleSourceEntity(
    @PrimaryKey val id: String,
    val author: String,
    val book: String,
    val locator: String?,
)

// 1. divination_rules
@Entity(
    tableName = "divination_rules",
    foreignKeys = [
        ForeignKey(
            entity = RuleSourceEntity::class,
            parentColumns = ["id"],
            childColumns = ["sourceId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("sourceId"), Index("category"), Index("targetType")],
)
data class RuleEntity(
    @PrimaryKey val id: String,
    val sourceId: String?,
    val sourceName: String,
    val category: String,          // DivinationCategory.name
    val targetType: String,        // RuleTarget.Type.name
    val targetKin: String?,        // SixKin.name 或 null
    val targetPosition: Int?,      // 爻位 或 null
    val originalText: String,
    val plainExplanation: String,
    val conditionText: String,
    val positiveMeaning: String?,
    val negativeMeaning: String?,
    val polarity: String,          // RulePolarity.name
    val priority: Int,
    val confidenceWeight: Double,
    val tagsCsv: String,           // 标签,分隔串
    // 分层解释(可空,JSON 或扁平列;此处扁平)
    val explPlain: String?,
    val explMechanism: String?,
    val explExample: String?,
    val explCaveat: String?,
    // 匹配提示(扁平)
    val hintRequireAll: Boolean?,
    val hintMinScore: Int?,
    val hintApplyTiming: String?,
)

// 2. rule_conditions(命中条件,JSON 整存)
@Entity(
    tableName = "rule_conditions",
    foreignKeys = [
        ForeignKey(
            entity = RuleEntity::class,
            parentColumns = ["id"],
            childColumns = ["ruleId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("ruleId")],
)
data class RuleConditionEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val ruleId: String,
    val orderIndex: Int,
    val payloadJson: String, // 单个 RuleCondition 的 JSON
)

// 3. rule_exclude_conditions(排除条件,结构同上)
@Entity(
    tableName = "rule_exclude_conditions",
    foreignKeys = [
        ForeignKey(
            entity = RuleEntity::class,
            parentColumns = ["id"],
            childColumns = ["ruleId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("ruleId")],
)
data class RuleExcludeConditionEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val ruleId: String,
    val orderIndex: Int,
    val payloadJson: String,
)

// 4. rule_tags(规则↔标签,便于按标签查规则)
@Entity(
    tableName = "rule_tags",
    foreignKeys = [
        ForeignKey(
            entity = RuleEntity::class,
            parentColumns = ["id"],
            childColumns = ["ruleId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("ruleId"), Index("tag")],
)
data class RuleTagEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val ruleId: String,
    val tag: String,
)

// 6. charts(排盘标量)
@Entity(tableName = "charts", indices = [Index("dateTimeEpoch")])
data class ChartEntity(
    @PrimaryKey val id: String,
    val question: String,
    val category: String?,         // DivinationCategory.name 或 null
    val dateTimeEpoch: Long,       // LocalDateTime → epochSecond(UTC 视为本地秒)
    val yearGanZhi: String,
    val monthGanZhi: String,
    val dayGanZhi: String,
    val hourGanZhi: String,
    val xunKong: String,           // 地支串
    val originalHexName: String,
    val originalLower: String,     // 八卦 name
    val originalUpper: String,
    val changedLower: String?,     // 变卦内卦 name(无变卦则 null)
    val changedUpper: String?,
    val palace: String,            // Palace.name
    val isSixClash: Boolean,
    val isSixCombine: Boolean,
    val worldLineIndex: Int,
    val responseLineIndex: Int,
    val methodCn: String,          // 起卦方式中文
    val notesCsv: String,
)

// 7. chart_lines(六爻,每爻一行)
@Entity(
    tableName = "chart_lines",
    primaryKeys = ["chartId", "position"],
    foreignKeys = [
        ForeignKey(
            entity = ChartEntity::class,
            parentColumns = ["id"],
            childColumns = ["chartId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("chartId")],
)
data class ChartLineEntity(
    val chartId: String,
    val position: Int,             // 1..6
    val yinYang: String,           // YINYANG name
    val isMoving: Boolean,
    val sixGod: String,
    val sixKin: String,
    val naJiaStem: String,         // HeavenlyStem.name
    val naJiaBranch: String,       // EarthlyBranch.name
    val element: String,           // FiveElement.name
    val isWorld: Boolean,
    val isResponse: Boolean,
    // 异构小结构用 JSON / 扁平
    val changedJson: String?,      // ChangedLine JSON 或 null
    val hiddenJson: String?,       // HiddenSpirit JSON 或 null
    val flyingJson: String?,       // FlyingSpirit JSON 或 null
    val statusJson: String,        // LineStatus JSON
)

// 8. cases(案例 = 关联一个排盘 + 占类 + 创建时间)
@Entity(
    tableName = "cases",
    foreignKeys = [
        ForeignKey(
            entity = ChartEntity::class,
            parentColumns = ["id"],
            childColumns = ["chartId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("chartId"), Index("createdEpoch")],
)
data class CaseEntity(
    @PrimaryKey val id: String,
    val chartId: String,
    val title: String,
    val createdEpoch: Long,
    // —— 本轮新增 ——
    val category: String? = null,        // DivinationCategory.name,占类筛选用
    val question: String = "",           // 占事(冗余自 chart,便于列表/搜索免 join)
    val castEpoch: Long = 0,             // 起卦时间(冗余自 chart)
    val favorite: Boolean = false,       // 收藏
    val hitRuleIdsCsv: String = "",      // 保存时"当时命中的断语"id 快照
)

// 9. case_feedback(案例反馈,1 案例对 1 反馈)
@Entity(
    tableName = "case_feedback",
    foreignKeys = [
        ForeignKey(
            entity = CaseEntity::class,
            parentColumns = ["id"],
            childColumns = ["caseId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["caseId"], unique = true)],
)
data class CaseFeedbackEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val caseId: String,
    val verdict: String,           // 准 / 部分 / 不准 / 待验证
    val actualResult: String,
    val note: String,
    val feedbackEpoch: Long,
    val hitRuleIdsCsv: String,     // 验中断语 id
    val missRuleIdsCsv: String,    // 误判断语 id
)

// 10. rule_stats(规则命中统计,服务权重修正)
@Entity(tableName = "rule_stats")
data class RuleStatsEntity(
    @PrimaryKey val ruleId: String,
    val matchedCount: Int,         // 被匹配命中的累计次数(=使用次数)
    val hitCount: Int,             // 验中次数
    val missCount: Int,            // 误判次数
    val lastUpdatedEpoch: Long,
    // —— 本轮新增 ——
    val partialCount: Int = 0,     // 部分验中次数
    val unknownCount: Int = 0,     // 无法判断次数
    val lastUsedEpoch: Long = 0,   // 最近一次使用(被匹配)时间
    val lastVerdict: String? = null, // 最近一次反馈结果(CaseVerdict.name)
)
