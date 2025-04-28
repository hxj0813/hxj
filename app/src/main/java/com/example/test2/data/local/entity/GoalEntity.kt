package com.example.test2.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.test2.data.local.converter.DateConverter
import com.example.test2.data.model.Goal
import java.util.Date

/**
 * 目标数据库实体
 *
 * @property id 目标ID
 * @property title 目标标题
 * @property description 目标描述（可空）
 * @property isImportant 是否重要
 * @property progress 完成进度 (0-100)
 * @property deadline 截止日期（可空）
 * @property isCompleted 是否已完成
 * @property createdAt 创建时间
 * @property updatedAt 更新时间
 */

 
@Entity(tableName = "goals")
@TypeConverters(DateConverter::class)
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val isImportant: Boolean = false,
    val progress: Int = 0,  // 0-100表示百分比
    val deadline: Date? = null,
    val isCompleted: Boolean = false,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) {
    /**
     * 转换为领域模型
     */
    fun toGoal(): Goal {
        return Goal(
            id = id,
            title = title,
            description = description ?: "",
            isImportant = isImportant,
            progress = progress / 100f, // 将0-100转换为0-1的浮点数
            deadline = deadline ?: Date(), // 为空时提供当前日期作为默认值
            isCompleted = isCompleted,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    companion object {
        /**
         * 从领域模型创建实体
         */
        fun fromGoal(goal: Goal): GoalEntity {
            return GoalEntity(
                id = goal.id,
                title = goal.title,
                description = goal.description,
                isImportant = goal.isImportant,
                progress = (goal.progress * 100).toInt(), // 将0-1转换为0-100的整数
                deadline = goal.deadline,
                isCompleted = goal.isCompleted,
                createdAt = goal.createdAt,
                updatedAt = goal.updatedAt
            )
        }
    }
}