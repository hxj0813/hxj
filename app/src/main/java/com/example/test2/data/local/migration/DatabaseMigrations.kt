package com.example.test2.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 数据库迁移策略
 * 处理不同版本数据库架构之间的迁移
 */
object DatabaseMigrations {
    
    /**
     * 从版本1迁移到版本2
     * 添加任务相关表
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 创建基础任务表
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `tasks` (
                    `id` TEXT NOT NULL,
                    `title` TEXT NOT NULL,
                    `description` TEXT,
                    `taskType` INTEGER NOT NULL,
                    `priority` INTEGER NOT NULL DEFAULT 1,
                    `color` INTEGER NOT NULL DEFAULT 4280391411,
                    `icon` TEXT,
                    `isCompleted` INTEGER NOT NULL DEFAULT 0,
                    `isArchived` INTEGER NOT NULL DEFAULT 0,
                    `dueDate` INTEGER,
                    `goalId` INTEGER,
                    `createdAt` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    `completedAt` INTEGER,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`goalId`) REFERENCES `goals`(`id`) ON DELETE SET NULL
                )
            """)
            
            // 创建签到任务表
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `check_in_tasks` (
                    `taskId` TEXT NOT NULL,
                    `frequencyType` INTEGER NOT NULL DEFAULT 0,
                    `frequencyCount` INTEGER NOT NULL DEFAULT 1,
                    `frequencyDaysJson` TEXT,
                    `currentStreak` INTEGER NOT NULL DEFAULT 0,
                    `bestStreak` INTEGER NOT NULL DEFAULT 0,
                    `totalCompletions` INTEGER NOT NULL DEFAULT 0,
                    `completedToday` INTEGER NOT NULL DEFAULT 0,
                    `lastCompletedDate` INTEGER,
                    `streakStartDate` INTEGER,
                    `reminderEnabled` INTEGER NOT NULL DEFAULT 0,
                    `reminderTime` INTEGER,
                    PRIMARY KEY(`taskId`),
                    FOREIGN KEY(`taskId`) REFERENCES `tasks`(`id`) ON DELETE CASCADE
                )
            """)
            
            // 创建番茄钟任务表
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `pomodoro_tasks` (
                    `taskId` TEXT NOT NULL,
                    `estimatedPomodoros` INTEGER NOT NULL DEFAULT 1,
                    `completedPomodoros` INTEGER NOT NULL DEFAULT 0,
                    `pomodoroLength` INTEGER NOT NULL DEFAULT 25,
                    `shortBreakLength` INTEGER NOT NULL DEFAULT 5,
                    `longBreakLength` INTEGER NOT NULL DEFAULT 15,
                    `longBreakInterval` INTEGER NOT NULL DEFAULT 4,
                    `tagId` TEXT,
                    `tagCategory` INTEGER NOT NULL DEFAULT 6,
                    `customTagName` TEXT,
                    `totalFocusTime` INTEGER NOT NULL DEFAULT 0,
                    `lastSessionDate` INTEGER,
                    PRIMARY KEY(`taskId`),
                    FOREIGN KEY(`taskId`) REFERENCES `tasks`(`id`) ON DELETE CASCADE
                )
            """)
            
            // 创建任务标签表
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `task_tags` (
                    `id` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `category` INTEGER NOT NULL DEFAULT 6,
                    `color` INTEGER NOT NULL DEFAULT 4280391411,
                    `icon` TEXT,
                    `isDefault` INTEGER NOT NULL DEFAULT 0,
                    `order` INTEGER NOT NULL DEFAULT 0,
                    `createdAt` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
            """)
            
            // 创建任务日志表
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `task_logs` (
                    `id` TEXT NOT NULL,
                    `taskId` TEXT NOT NULL,
                    `taskType` INTEGER NOT NULL,
                    `completedDate` INTEGER NOT NULL,
                    `focusMinutes` INTEGER,
                    `pomodoroCount` INTEGER,
                    `note` TEXT,
                    `mood` INTEGER,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`taskId`) REFERENCES `tasks`(`id`) ON DELETE CASCADE
                )
            """)
            
            // 创建索引以提升查询性能
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_goalId` ON `tasks` (`goalId`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_taskType` ON `tasks` (`taskType`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_isCompleted` ON `tasks` (`isCompleted`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_dueDate` ON `tasks` (`dueDate`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_task_logs_taskId` ON `task_logs` (`taskId`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_task_logs_completedDate` ON `task_logs` (`completedDate`)")
        }
    }
    
    /**
     * 从版本2迁移到版本3
     * 添加时间追踪相关表
     */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 创建时间条目表
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `time_entries` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `title` TEXT NOT NULL,
                    `description` TEXT,
                    `category` TEXT NOT NULL,
                    `start_time` INTEGER NOT NULL,
                    `end_time` INTEGER,
                    `duration_seconds` INTEGER NOT NULL DEFAULT 0,
                    `task_id` INTEGER,
                    `habit_id` INTEGER,
                    `goal_id` INTEGER,
                    `pomodoro_count` INTEGER NOT NULL DEFAULT 0,
                    `is_pomodoro` INTEGER NOT NULL DEFAULT 0,
                    `is_check_in` INTEGER NOT NULL DEFAULT 0,
                    `tags` TEXT NOT NULL DEFAULT '[]',
                    `created_at` INTEGER NOT NULL,
                    `updated_at` INTEGER NOT NULL,
                    `is_manual` INTEGER NOT NULL DEFAULT 0,
                    `is_synced` INTEGER NOT NULL DEFAULT 0,
                    `sync_id` TEXT
                )
            """)
            
            // 创建时间标签表
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `time_tags` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `color_hex` TEXT NOT NULL,
                    `icon_name` TEXT,
                    `created_at` INTEGER NOT NULL,
                    `updated_at` INTEGER NOT NULL
                )
            """)
            
            // 创建时间条目-标签关联表
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `time_entry_tag_cross_ref` (
                    `time_entry_id` INTEGER NOT NULL,
                    `tag_id` INTEGER NOT NULL,
                    `created_at` INTEGER NOT NULL,
                    PRIMARY KEY(`time_entry_id`, `tag_id`),
                    FOREIGN KEY(`time_entry_id`) REFERENCES `time_entries`(`id`) ON DELETE CASCADE,
                    FOREIGN KEY(`tag_id`) REFERENCES `time_tags`(`id`) ON DELETE CASCADE
                )
            """)
            
            // 创建时间统计表
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `time_statistics` (
                    `id` TEXT NOT NULL,
                    `reference_type` TEXT NOT NULL,
                    `reference_id` INTEGER,
                    `category` TEXT,
                    `date` INTEGER NOT NULL,
                    `total_seconds` INTEGER NOT NULL DEFAULT 0,
                    `pomodoro_count` INTEGER NOT NULL DEFAULT 0,
                    `check_in_count` INTEGER NOT NULL DEFAULT 0,
                    `session_count` INTEGER NOT NULL DEFAULT 0,
                    `updated_at` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
            """)
            
            // 创建时间目标表
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `time_goals` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `title` TEXT NOT NULL,
                    `description` TEXT,
                    `target_seconds` INTEGER NOT NULL,
                    `current_seconds` INTEGER NOT NULL DEFAULT 0,
                    `category` TEXT,
                    `reference_type` TEXT,
                    `reference_id` INTEGER,
                    `start_date` INTEGER NOT NULL,
                    `end_date` INTEGER,
                    `is_completed` INTEGER NOT NULL DEFAULT 0,
                    `completion_date` INTEGER,
                    `is_recurring` INTEGER NOT NULL DEFAULT 0,
                    `recurrence_type` TEXT,
                    `created_at` INTEGER NOT NULL,
                    `updated_at` INTEGER NOT NULL
                )
            """)
            
            // 创建索引以提升查询性能
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_time_entries_start_time` ON `time_entries` (`start_time`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_time_entries_category` ON `time_entries` (`category`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_time_entries_task_id` ON `time_entries` (`task_id`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_time_entries_habit_id` ON `time_entries` (`habit_id`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_time_entries_is_pomodoro` ON `time_entries` (`is_pomodoro`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_time_entries_is_check_in` ON `time_entries` (`is_check_in`)")
            
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_time_tags_name` ON `time_tags` (`name`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_time_entry_tag_cross_ref_time_entry_id` ON `time_entry_tag_cross_ref` (`time_entry_id`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_time_entry_tag_cross_ref_tag_id` ON `time_entry_tag_cross_ref` (`tag_id`)")
            
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_time_statistics_reference` ON `time_statistics` (`reference_type`, `reference_id`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_time_statistics_date` ON `time_statistics` (`date`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_time_statistics_category` ON `time_statistics` (`category`)")
            
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_time_goals_category` ON `time_goals` (`category`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_time_goals_reference` ON `time_goals` (`reference_type`, `reference_id`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_time_goals_is_completed` ON `time_goals` (`is_completed`)")
        }
    }
    
    /**
     * 从版本3迁移到版本4
     * 为笔记表添加图片字段
     */
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 为notes表添加imagesJson字段，默认为空数组
            database.execSQL("ALTER TABLE notes ADD COLUMN imagesJson TEXT NOT NULL DEFAULT '[]'")
        }
    }
    
    /**
     * 从版本4迁移到版本5
     * 处理CheckInTaskEntity架构变更
     */
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 这是一个空迁移，因为我们实际上只是修复了代码中的重复字段，而不需要对表结构进行变更
            // 但仍需要增加版本号以更新 Room 期望的架构哈希值
        }
    }
} 