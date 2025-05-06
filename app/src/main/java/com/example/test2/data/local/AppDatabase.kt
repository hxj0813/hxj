package com.example.test2.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.test2.data.local.converter.DateConverter
import com.example.test2.data.local.dao.BadgeDao
import com.example.test2.data.local.dao.GoalDao
import com.example.test2.data.local.dao.HabitDao
import com.example.test2.data.local.dao.HabitLogDao
import com.example.test2.data.local.dao.NoteDao
import com.example.test2.data.local.dao.UserBadgeDao
//import com.example.test2.data.local.dao.TaskDao
import com.example.test2.data.local.entity.BadgeEntity
import com.example.test2.data.local.entity.GoalEntity
import com.example.test2.data.local.entity.HabitEntity
import com.example.test2.data.local.entity.HabitLogEntity
import com.example.test2.data.local.entity.NoteEntity
import com.example.test2.data.local.entity.UserBadgeEntity
//import com.example.test2.data.local.entity.TaskEntity

/**
 * 应用数据库类
 * 集中管理所有实体和数据访问对象
 */
@Database(
    entities = [
        GoalEntity::class,
        NoteEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        BadgeEntity::class,
        UserBadgeEntity::class
        //TaskEntity::class
        // 随着应用扩展，可以在这里添加更多实体
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    /**
     * 获取目标DAO
     */
    abstract fun goalDao(): GoalDao
    
    /**
     * 获取笔记DAO
     */
    abstract fun noteDao(): NoteDao
    
    /**
     * 获取习惯DAO
     */
    abstract fun habitDao(): HabitDao
    
    /**
     * 获取习惯打卡记录DAO
     */
    abstract fun habitLogDao(): HabitLogDao
    
    /**
     * 获取徽章DAO
     */
    abstract fun badgeDao(): BadgeDao
    
    /**
     * 获取用户徽章DAO
     */
    abstract fun userBadgeDao(): UserBadgeDao
    
    /**
     * 获取任务DAO
     */
    //abstract fun taskDao(): TaskDao
    
    // 随着应用扩展，可以在这里添加更多DAO获取方法
    
    companion object {
        private const val DATABASE_NAME = "app_database"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        /**
         * 获取数据库实例（单例模式）
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration() // 升级数据库时删除旧数据，仅用于开发阶段
                .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
}