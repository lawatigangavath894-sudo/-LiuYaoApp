package com.liuyao.paipan.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.liuyao.paipan.data.db.dao.CaseDao
import com.liuyao.paipan.data.db.dao.ChartDao
import com.liuyao.paipan.data.db.dao.RuleDao
import com.liuyao.paipan.data.db.dao.StatsDao
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

@Database(
    entities = [
        RuleSourceEntity::class,
        RuleEntity::class,
        RuleConditionEntity::class,
        RuleExcludeConditionEntity::class,
        RuleTagEntity::class,
        ChartEntity::class,
        ChartLineEntity::class,
        CaseEntity::class,
        CaseFeedbackEntity::class,
        RuleStatsEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ruleDao(): RuleDao
    abstract fun chartDao(): ChartDao
    abstract fun caseDao(): CaseDao
    abstract fun statsDao(): StatsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "liuyao.db",
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}
