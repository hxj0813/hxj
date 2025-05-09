package com.example.test2.data.repository

import com.example.test2.data.local.dao.TimeTrackingDao
import com.example.test2.data.local.entity.timetracking.TimeEntryEntity
import com.example.test2.data.local.entity.timetracking.TimeEntryTagCrossRef
import com.example.test2.data.local.entity.timetracking.TimeGoalEntity
import com.example.test2.data.local.entity.timetracking.TimeStatEntity
import com.example.test2.data.local.entity.timetracking.TimeTagEntity
import com.example.test2.data.model.TimeCategory
import com.example.test2.data.model.TimeEntry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 时间追踪仓库
 * 负责时间追踪相关数据的存取和业务逻辑
 */
@Singleton
class TimeTrackingRepository @Inject constructor(
    private val timeTrackingDao: TimeTrackingDao
) {
    private val gson = Gson()
    
    // ==================== 时间条目操作 ====================
    
    /**
     * 保存时间条目
     * @param timeEntry 时间条目模型
     * @return 保存的条目ID
     */
    suspend fun saveTimeEntry(timeEntry: TimeEntry): Long {
        val entity = mapTimeEntryToEntity(timeEntry)
        val id = timeTrackingDao.insertTimeEntry(entity)
        
        // 保存后更新统计数据
        updateStatisticsForTimeEntry(entity)
        
        return id
    }
    
    /**
     * 更新时间条目
     * @param timeEntry 时间条目模型
     */
    suspend fun updateTimeEntry(timeEntry: TimeEntry) {
        val entity = mapTimeEntryToEntity(timeEntry.copy(id = timeEntry.id))
        timeTrackingDao.updateTimeEntry(entity)
        
        // 更新后更新统计数据
        updateStatisticsForTimeEntry(entity)
    }
    
    /**
     * 删除时间条目
     * @param timeEntryId 时间条目ID
     */
    suspend fun deleteTimeEntry(timeEntryId: Long) {
        val entity = timeTrackingDao.getTimeEntryById(timeEntryId)
        entity?.let {
            timeTrackingDao.deleteTimeEntry(it)
            // 删除后更新统计数据
            updateStatisticsAfterDeletion(it)
        }
    }
    
    /**
     * 获取所有时间条目
     * @return 时间条目流
     */
    fun getAllTimeEntries(): Flow<List<TimeEntry>> {
        return timeTrackingDao.getAllTimeEntries()
            .map { entities -> entities.map { mapEntityToTimeEntry(it) } }
    }
    
    /**
     * 获取正在进行的时间条目
     * @return 正在进行的时间条目流
     */
    fun getOngoingTimeEntries(): Flow<List<TimeEntry>> {
        return timeTrackingDao.getOngoingTimeEntries()
            .map { entities -> entities.map { mapEntityToTimeEntry(it) } }
    }
    
    /**
     * 获取指定时间段内的时间条目
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 时间条目流
     */
    fun getTimeEntriesBetween(startTime: Date, endTime: Date): Flow<List<TimeEntry>> {
        return timeTrackingDao.getTimeEntriesBetween(startTime.time, endTime.time)
            .map { entities -> entities.map { mapEntityToTimeEntry(it) } }
    }
    
    /**
     * 获取指定分类的时间条目
     * @param category 时间分类
     * @return 时间条目流
     */
    fun getTimeEntriesByCategory(category: TimeCategory): Flow<List<TimeEntry>> {
        return timeTrackingDao.getTimeEntriesByCategory(category.name)
            .map { entities -> entities.map { mapEntityToTimeEntry(it) } }
    }
    
    /**
     * 获取指定任务的时间条目
     * @param taskId 任务ID
     * @return 时间条目流
     */
    fun getTimeEntriesByTask(taskId: Long): Flow<List<TimeEntry>> {
        return timeTrackingDao.getTimeEntriesByTask(taskId)
            .map { entities -> entities.map { mapEntityToTimeEntry(it) } }
    }
    
    /**
     * 获取指定习惯的时间条目
     * @param habitId 习惯ID
     * @return 时间条目流
     */
    fun getTimeEntriesByHabit(habitId: Long): Flow<List<TimeEntry>> {
        return timeTrackingDao.getTimeEntriesByHabit(habitId)
            .map { entities -> entities.map { mapEntityToTimeEntry(it) } }
    }
    
    /**
     * 获取今日时间条目
     * @return 今日时间条目流
     */
    fun getTodayTimeEntries(): Flow<List<TimeEntry>> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.time
        
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val startOfNextDay = calendar.time
        
        return getTimeEntriesBetween(startOfDay, startOfNextDay)
    }
    
    /**
     * 停止正在进行的时间条目
     * @param endTime 结束时间
     * @return 更新的条目数
     */
    suspend fun stopOngoingTimeEntries(endTime: Date = Date()): Int {
        var updatedCount = 0
        timeTrackingDao.getOngoingTimeEntries().map { entities ->
            entities.forEach { entity ->
                val duration = (endTime.time - entity.startTime) / 1000
                val updatedEntity = entity.copy(
                    endTime = endTime.time,
                    durationSeconds = duration,
                    updatedAt = System.currentTimeMillis()
                )
                timeTrackingDao.updateTimeEntry(updatedEntity)
                updateStatisticsForTimeEntry(updatedEntity)
                updatedCount++
            }
        }
        return updatedCount
    }
    
    /**
     * 获取指定时间段内特定分类的总时长
     * @param category 时间分类
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 总时长（秒）
     */
    suspend fun getTotalTimeByCategory(
        category: TimeCategory,
        startTime: Date,
        endTime: Date
    ): Long {
        return timeTrackingDao.getTotalTimeByCategory(
            category.name,
            startTime.time,
            endTime.time
        ) ?: 0
    }
    
    /**
     * 获取指定任务的已完成番茄钟数量
     * @param taskId 任务ID
     * @return 已完成番茄钟数量
     */
    suspend fun getCompletedPomodoroCount(taskId: Long): Int {
        return timeTrackingDao.getCompletedPomodoroCount(taskId)
    }
    
    /**
     * 获取今日打卡次数
     * @return 今日打卡次数
     */
    suspend fun getTodayCheckInCount(): Int {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val startOfNextDay = calendar.timeInMillis
        
        return timeTrackingDao.getDailyCheckInCount(startOfDay, startOfNextDay)
    }
    
    // ==================== 标签操作 ====================
    
    /**
     * 保存标签
     * @param name 标签名称
     * @param colorHex 标签颜色（十六进制）
     * @param iconName 图标名称（可选）
     * @return 保存的标签ID
     */
    suspend fun saveTag(name: String, colorHex: String, iconName: String? = null): Long {
        val entity = TimeTagEntity(
            name = name,
            colorHex = colorHex,
            iconName = iconName,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return timeTrackingDao.insertTag(entity)
    }
    
    /**
     * 获取所有标签
     * @return 标签流
     */
    fun getAllTags(): Flow<List<TimeTagEntity>> {
        return timeTrackingDao.getAllTags()
    }
    
    /**
     * 为时间条目添加标签
     * @param timeEntryId 时间条目ID
     * @param tagId 标签ID
     */
    suspend fun addTagToTimeEntry(timeEntryId: Long, tagId: Long) {
        val crossRef = TimeEntryTagCrossRef(
            timeEntryId = timeEntryId,
            tagId = tagId,
            createdAt = System.currentTimeMillis()
        )
        timeTrackingDao.insertTimeEntryTagCrossRef(crossRef)
    }
    
    /**
     * 获取时间条目的标签
     * @param timeEntryId 时间条目ID
     * @return 标签流
     */
    fun getTagsForTimeEntry(timeEntryId: Long): Flow<List<TimeTagEntity>> {
        return timeTrackingDao.getTagsForTimeEntry(timeEntryId)
    }
    
    // ==================== 统计数据操作 ====================
    
    /**
     * 获取指定日期范围内的时间分类统计
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分类统计数据
     */
    suspend fun getCategoryBreakdown(startDate: Date, endDate: Date): Map<String, Long> {
        val categoryStats = timeTrackingDao.getCategoryBreakdown(startDate.time, endDate.time)
        return categoryStats.associate { it.category to it.total }
    }
    
    /**
     * 获取指定日期范围内的总已完成番茄钟数
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 总已完成番茄钟数
     */
    suspend fun getTotalCompletedPomodoros(startDate: Date, endDate: Date): Int {
        return timeTrackingDao.getTotalCompletedPomodoros(startDate.time, endDate.time)
    }
    
    /**
     * 获取指定日期范围内的总追踪时间
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 总追踪时间（秒）
     */
    suspend fun getTotalTrackedTime(startDate: Date, endDate: Date): Long {
        return timeTrackingDao.getTotalTrackedTime(startDate.time, endDate.time) ?: 0
    }
    
    // ==================== 时间目标操作 ====================
    
    /**
     * 创建时间目标
     * @param title 目标标题
     * @param targetSeconds 目标时间（秒）
     * @param category 时间分类（可选）
     * @param startDate 开始日期
     * @param endDate 结束日期（可选）
     * @param referenceType 引用类型（可选）
     * @param referenceId 引用ID（可选）
     * @return 目标ID
     */
    suspend fun createTimeGoal(
        title: String,
        targetSeconds: Long,
        category: TimeCategory? = null,
        startDate: Date,
        endDate: Date? = null,
        referenceType: String? = null,
        referenceId: Long? = null
    ): Long {
        val entity = TimeGoalEntity(
            title = title,
            targetSeconds = targetSeconds,
            category = category?.name,
            startDate = startDate.time,
            endDate = endDate?.time,
            referenceType = referenceType,
            referenceId = referenceId,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return timeTrackingDao.insertTimeGoal(entity)
    }
    
    /**
     * 获取活跃的时间目标
     * @return 活跃的时间目标流
     */
    fun getActiveTimeGoals(): Flow<List<TimeGoalEntity>> {
        return timeTrackingDao.getActiveTimeGoals()
    }
    
    /**
     * 更新时间目标进度
     * @param goalId 目标ID
     * @param additionalSeconds 新增时间（秒）
     * @return 是否已完成目标
     */
    suspend fun updateTimeGoalProgress(goalId: Long, additionalSeconds: Long): Boolean {
        val goal = timeTrackingDao.getTimeGoalById(goalId) ?: return false
        val newCurrentSeconds = goal.currentSeconds + additionalSeconds
        val isCompleted = newCurrentSeconds >= goal.targetSeconds
        
        val updatedGoal = goal.copy(
            currentSeconds = newCurrentSeconds,
            isCompleted = isCompleted,
            completionDate = if (isCompleted) System.currentTimeMillis() else null,
            updatedAt = System.currentTimeMillis()
        )
        
        timeTrackingDao.updateTimeGoal(updatedGoal)
        return isCompleted
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 将TimeEntry模型转换为Entity
     */
    private fun mapTimeEntryToEntity(timeEntry: TimeEntry): TimeEntryEntity {
        return TimeEntryEntity(
            id = if (timeEntry.id > 0) timeEntry.id else 0,
            title = timeEntry.title,
            description = timeEntry.description,
            category = timeEntry.category.name,
            startTime = timeEntry.startTime.time,
            endTime = timeEntry.endTime?.time,
            durationSeconds = timeEntry.duration,
            taskId = timeEntry.taskId,
            tags = gson.toJson(timeEntry.tags),
            isPomodoro = timeEntry.category == TimeCategory.FOCUS,
            createdAt = timeEntry.createdAt.time,
            updatedAt = System.currentTimeMillis()
        )
    }
    
    /**
     * 将Entity转换为TimeEntry模型
     */
    private fun mapEntityToTimeEntry(entity: TimeEntryEntity): TimeEntry {
        val category = try {
            TimeCategory.valueOf(entity.category)
        } catch (e: Exception) {
            TimeCategory.OTHER
        }
        
        val tags: List<String> = try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(entity.tags, type)
        } catch (e: Exception) {
            emptyList()
        }
        
        return TimeEntry(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            category = category,
            startTime = Date(entity.startTime),
            endTime = entity.endTime?.let { Date(it) },
            duration = entity.durationSeconds,
            taskId = entity.taskId,
            tags = tags,
            createdAt = Date(entity.createdAt)
        )
    }
    
    /**
     * 更新时间条目相关的统计数据
     */
    private suspend fun updateStatisticsForTimeEntry(entity: TimeEntryEntity) {
        // 获取日期
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = entity.startTime
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val dateMillis = calendar.timeInMillis
        
        // 日期格式化（用于ID）
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.ENGLISH)
        val dateString = dateFormat.format(Date(dateMillis))
        
        // 更新分类统计
        updateCategoryStatistic(entity.category, dateMillis, dateString, entity)
        
        // 更新任务统计（如果有关联任务）
        entity.taskId?.let { taskId ->
            updateReferenceStatistic("task", taskId, dateMillis, dateString, entity)
        }
        
        // 更新习惯统计（如果有关联习惯）
        entity.habitId?.let { habitId ->
            updateReferenceStatistic("habit", habitId, dateMillis, dateString, entity)
        }
    }
    
    /**
     * 更新分类统计
     */
    private suspend fun updateCategoryStatistic(
        category: String,
        dateMillis: Long,
        dateString: String,
        entity: TimeEntryEntity
    ) {
        val statId = "category_${category}_$dateString"
        val existingStat = timeTrackingDao.getTimeStatistics("category", 0)
            .map { stats -> stats.find { it.id == statId } }
        
        val stat = existingStat.first() ?: TimeStatEntity(
            id = statId,
            referenceType = "category",
            category = category,
            date = dateMillis
        )
        
        val pomodoroCount = if (entity.isPomodoro) 1 else 0
        val checkInCount = if (entity.isCheckIn) 1 else 0
        
        val updatedStat = stat.copy(
            totalSeconds = stat.totalSeconds + entity.durationSeconds,
            pomodoroCount = stat.pomodoroCount + pomodoroCount,
            checkInCount = stat.checkInCount + checkInCount,
            sessionCount = stat.sessionCount + 1,
            updatedAt = System.currentTimeMillis()
        )
        
        timeTrackingDao.insertTimeStatistic(updatedStat)
    }
    
    /**
     * 更新引用对象统计
     */
    private suspend fun updateReferenceStatistic(
        refType: String,
        refId: Long,
        dateMillis: Long,
        dateString: String,
        entity: TimeEntryEntity
    ) {
        val statId = "${refType}_${refId}_$dateString"
        val existingStat = timeTrackingDao.getTimeStatistics(refType, refId)
            .map { stats -> stats.find { it.id == statId } }
        
        val stat = existingStat.first() ?: TimeStatEntity(
            id = statId,
            referenceType = refType,
            referenceId = refId,
            category = entity.category,
            date = dateMillis
        )
        
        val pomodoroCount = if (entity.isPomodoro) 1 else 0
        val checkInCount = if (entity.isCheckIn) 1 else 0
        
        val updatedStat = stat.copy(
            totalSeconds = stat.totalSeconds + entity.durationSeconds,
            pomodoroCount = stat.pomodoroCount + pomodoroCount,
            checkInCount = stat.checkInCount + checkInCount,
            sessionCount = stat.sessionCount + 1,
            updatedAt = System.currentTimeMillis()
        )
        
        timeTrackingDao.insertTimeStatistic(updatedStat)
    }
    
    /**
     * 删除时间条目后更新统计
     */
    private suspend fun updateStatisticsAfterDeletion(entity: TimeEntryEntity) {
        // 目前简单实现，实际应用中可以根据需求调整
        // 例如，可以减去对应的时间、番茄钟数等
    }
} 