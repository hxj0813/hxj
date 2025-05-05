package com.example.test2.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.test2.data.local.converter.DateConverter
import com.example.test2.data.local.dao.GoalDao
import com.example.test2.data.local.dao.NoteDao
//import com.example.test2.data.local.dao.TaskDao
import com.example.test2.data.local.entity.GoalEntity
import com.example.test2.data.local.entity.NoteEntity
//import com.example.test2.data.local.entity.TaskEntity

/**
 * 应用数据库类
 * 集中管理所有实体和数据访问对象
 */
@Database(
    entities = [
        GoalEntity::class,
        NoteEntity::class,
        //TaskEntity::class
        // 随着应用扩展，可以在这里添加更多实体
    ],
    version = 1,
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