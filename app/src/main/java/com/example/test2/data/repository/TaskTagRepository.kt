package com.example.test2.data.repository

import com.example.test2.data.local.dao.TaskTagDao
import com.example.test2.data.local.entity.TagCategory
import com.example.test2.data.local.entity.TaskTagEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 任务标签仓库类
 * 封装对任务标签数据的访问逻辑
 */
@Singleton
class TaskTagRepository @Inject constructor(
    private val taskTagDao: TaskTagDao
) {
    /**
     * 获取所有标签
     */
    fun getAllTags(): Flow<List<TaskTagEntity>> {
        return taskTagDao.getAllTags()
    }
    
    /**
     * 获取默认标签
     */
    fun getDefaultTags(): Flow<List<TaskTagEntity>> {
        return taskTagDao.getDefaultTags()
    }
    
    /**
     * 获取自定义标签
     */
    fun getUserTags(): Flow<List<TaskTagEntity>> {
        return taskTagDao.getUserTags()
    }
    
    /**
     * 通过ID获取标签
     */
    suspend fun getTagById(tagId: String): TaskTagEntity? {
        return taskTagDao.getTagById(tagId)
    }
    
    /**
     * 通过分类获取标签
     */
    fun getTagsByCategory(category: TagCategory): Flow<List<TaskTagEntity>> {
        return taskTagDao.getTagsByCategory(category.ordinal)
    }
    
    /**
     * 通过名称搜索标签
     */
    fun searchTags(query: String): Flow<List<TaskTagEntity>> {
        return taskTagDao.searchTags(query)
    }
    
    /**
     * 创建新标签
     */
    suspend fun createTag(tag: TaskTagEntity) {
        taskTagDao.insertTag(tag)
    }
    
    /**
     * 批量创建标签
     */
    suspend fun createTags(tags: List<TaskTagEntity>) {
        taskTagDao.insertTags(tags)
    }
    
    /**
     * 更新标签
     */
    suspend fun updateTag(tag: TaskTagEntity) {
        taskTagDao.updateTag(tag)
    }
    
    /**
     * 删除标签
     */
    suspend fun deleteTag(tag: TaskTagEntity) {
        taskTagDao.deleteTag(tag)
    }
    
    /**
     * 通过ID删除标签
     */
    suspend fun deleteTagById(tagId: String) {
        taskTagDao.deleteTagById(tagId)
    }
    
    /**
     * 更新标签顺序
     */
    suspend fun updateTagOrder(tagId: String, order: Int) {
        taskTagDao.updateTagOrder(tagId, order)
    }
    
    /**
     * 重置所有标签顺序
     */
    suspend fun resetTagOrders() {
        taskTagDao.resetTagOrders()
    }
    
    /**
     * 获取标签总数
     */
    suspend fun getTagCount(): Int {
        return taskTagDao.getTagCount()
    }
    
    /**
     * 检查标签名称是否已存在
     */
    suspend fun isTagNameExists(name: String, excludeId: String = ""): Boolean {
        return taskTagDao.isTagNameExists(name, excludeId) > 0
    }
    
    /**
     * 初始化默认标签
     * 应用首次运行时调用
     */
    suspend fun initDefaultTags() {
        // 如果已有标签，不进行初始化
        if (getTagCount() > 0) return
        
        val defaultTags = listOf(
            TaskTagEntity.create(
                name = "工作",
                category = TagCategory.WORK,
                color = 0xFF4CAF50, // 绿色
                icon = "work",
                isDefault = true
            ),
            TaskTagEntity.create(
                name = "学习",
                category = TagCategory.STUDY,
                color = 0xFF2196F3, // 蓝色
                icon = "school",
                isDefault = true
            ),
            TaskTagEntity.create(
                name = "运动",
                category = TagCategory.EXERCISE,
                color = 0xFFFF9800, // 橙色
                icon = "fitness_center",
                isDefault = true
            ),
            TaskTagEntity.create(
                name = "阅读",
                category = TagCategory.READING,
                color = 0xFF9C27B0, // 紫色
                icon = "menu_book",
                isDefault = true
            ),
            TaskTagEntity.create(
                name = "创意",
                category = TagCategory.CREATIVE,
                color = 0xFFE91E63, // 粉色
                icon = "palette",
                isDefault = true
            ),
            TaskTagEntity.create(
                name = "个人发展",
                category = TagCategory.PERSONAL,
                color = 0xFF3F51B5, // 靛蓝色
                icon = "psychology",
                isDefault = true
            ),
            TaskTagEntity.create(
                name = "其他",
                category = TagCategory.OTHER,
                color = 0xFF607D8B, // 蓝灰色
                icon = "more_horiz",
                isDefault = true
            )
        )
        
        createTags(defaultTags)
    }
} 