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
} 