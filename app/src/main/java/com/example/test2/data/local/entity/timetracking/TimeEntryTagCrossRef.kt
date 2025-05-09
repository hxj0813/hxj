package com.example.test2.data.local.entity.timetracking

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * 时间条目和标签的多对多关联表
 */
@Entity(
    tableName = "time_entry_tag_cross_ref",
    primaryKeys = ["time_entry_id", "tag_id"],
    foreignKeys = [
        ForeignKey(
            entity = TimeEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["time_entry_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TimeTagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tag_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["time_entry_id"]),
        Index(value = ["tag_id"])
    ]
)
data class TimeEntryTagCrossRef(
    @ColumnInfo(name = "time_entry_id")
    val timeEntryId: Long,
    
    @ColumnInfo(name = "tag_id")
    val tagId: Long,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
) 