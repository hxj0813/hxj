package com.example.test2.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.test2.data.local.entity.TaskTagEntity
import kotlinx.coroutines.flow.Flow

/**
 * 任务标签DAO接口
 * 提供对任务标签表的访问方法
 */
@Dao
interface TaskTagDao {
    /**
     * 获取所有标签
     */
    @Query("SELECT * FROM task_tags ORDER BY isDefault DESC, `order` ASC, name ASC")
    fun getAllTags(): Flow<List<TaskTagEntity>>
    
    /**
     * 获取默认标签
     */
    @Query("SELECT * FROM task_tags WHERE isDefault = 1 ORDER BY `order` ASC, name ASC")
    fun getDefaultTags(): Flow<List<TaskTagEntity>>
    
    /**
     * 获取自定义标签
     */
    @Query("SELECT * FROM task_tags WHERE isDefault = 0 ORDER BY `order` ASC, name ASC")
    fun getUserTags(): Flow<List<TaskTagEntity>>
    
    /**
     * 通过ID获取标签
     */
    @Query("SELECT * FROM task_tags WHERE id = :tagId")
    suspend fun getTagById(tagId: String): TaskTagEntity?
    
    /**
     * 通过分类获取标签
     */
    @Query("SELECT * FROM task_tags WHERE category = :category ORDER BY isDefault DESC, `order` ASC, name ASC")
    fun getTagsByCategory(category: Int): Flow<List<TaskTagEntity>>
    
    /**
     * 通过名称搜索标签
     */
    @Query("SELECT * FROM task_tags WHERE name LIKE '%' || :query || '%' ORDER BY isDefault DESC, `order` ASC")
    fun searchTags(query: String): Flow<List<TaskTagEntity>>
    
    /**
     * 插入单个标签
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TaskTagEntity)
    
    /**
     * 批量插入标签
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(tags: List<TaskTagEntity>)
    
    /**
     * 更新标签
     */
    @Update
    suspend fun updateTag(tag: TaskTagEntity)
    
    /**
     * 删除标签
     */
    @Delete
    suspend fun deleteTag(tag: TaskTagEntity)
    
    /**
     * 通过ID删除标签
     */
    @Query("DELETE FROM task_tags WHERE id = :tagId")
    suspend fun deleteTagById(tagId: String)
    
    /**
     * 更新标签顺序
     */
    @Query("UPDATE task_tags SET `order` = :order WHERE id = :tagId")
    suspend fun updateTagOrder(tagId: String, order: Int)
    
    /**
     * 重置所有标签顺序（通常在重新排序时使用）
     */
    @Query("UPDATE task_tags SET `order` = 0")
    suspend fun resetTagOrders()
    
    /**
     * 获取标签总数
     */
    @Query("SELECT COUNT(*) FROM task_tags")
    suspend fun getTagCount(): Int
    
    /**
     * 检查标签名称是否已存在（排除指定ID的标签）
     */
    @Query("SELECT COUNT(*) FROM task_tags WHERE name = :name AND id != :excludeId")
    suspend fun isTagNameExists(name: String, excludeId: String): Int
} 