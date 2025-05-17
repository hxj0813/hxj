package com.example.test2.presentation.habits

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test2.data.local.entity.FrequencyType
import com.example.test2.data.local.entity.HabitCategory
import com.example.test2.data.local.entity.HabitEntity
import com.example.test2.data.local.entity.HabitPriority
import com.example.test2.data.repository.HabitRepository
import com.example.test2.domain.service.BadgeService
import com.example.test2.presentation.habits.components.HabitFormData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * 习惯状态
 */
data class HabitsState(
    val habits: List<HabitEntity> = emptyList(),
    val isLoading: Boolean = false,
    val showOnlyActive: Boolean = true,
    val currentFilter: HabitCategory? = null,
    val showAddEditForm: Boolean = false,
    val currentEditHabit: HabitFormData? = null,
    val error: String? = null
)

/**
 * 习惯视图模型
 */
@HiltViewModel
class HabitsViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val badgeService: BadgeService,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    // 内部状态
    private val _state = mutableStateOf(HabitsState())
    val state: State<HabitsState> = _state
    
    // 习惯列表流
    private val _showOnlyActive = MutableStateFlow(true)
    private val _currentFilter = MutableStateFlow<HabitCategory?>(null)
    
    // 过滤后的习惯流
    val filteredHabits: StateFlow<List<HabitEntity>> = combine(
        habitRepository.getAllHabits(),
        _showOnlyActive,
        _currentFilter
    ) { habits, onlyActive, category ->
        var result = habits
        
        // 筛选活跃/归档习惯
        if (onlyActive) {
            result = result.filter { !it.isArchived }
        }
        
        // 按类别筛选
        if (category != null) {
            result = result.filter { it.getCategoryEnum() == category }
        }
        
        result
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    init {
        // 加载习惯列表
        loadHabits()
        
        // 初始化默认徽章
        initializeBadges()
        
        // 监听筛选状态变化
        viewModelScope.launch {
            filteredHabits.collect { habits ->
                _state.value = _state.value.copy(habits = habits)
            }
        }
    }
    
    /**
     * 根据ID获取习惯
     */
    suspend fun getHabitById(habitId: String): HabitEntity? {
        return habitRepository.getHabitById(habitId).first()
    }
    
    /**
     * 获取从导航传递的需要编辑的习惯ID
     */
    fun getHabitToEditFromNavigation(): String? {
        val habitId = savedStateHandle.get<String>("habitToEdit")
        if (habitId != null) {
            // 消费一次后移除，避免重复触发
            savedStateHandle.remove<String>("habitToEdit")
        }
        return habitId
    }
    
    /**
     * 获取习惯并在UI线程中处理
     */
    fun loadHabitForEdit(habitId: String) {
        viewModelScope.launch {
            try {
                val habit = habitRepository.getHabitById(habitId).first()
                if (habit != null) {
                    showEditHabitForm(habit)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "加载习惯失败: ${e.message}"
                )
            }
        }
    }
    
    private fun loadHabits() {
        _state.value = _state.value.copy(isLoading = true)
        // 加载逻辑已通过StateFlow处理
        _state.value = _state.value.copy(isLoading = false)
    }
    
    private fun initializeBadges() {
        viewModelScope.launch {
            try {
                badgeService.initializeDefaultBadges()
            } catch (e: Exception) {
                // 忽略初始化错误
            }
        }
    }
    
    /**
     * 显示添加习惯表单
     */
    fun showAddHabitForm() {
        _state.value = _state.value.copy(
            showAddEditForm = true,
            currentEditHabit = HabitFormData()
        )
    }
    
    /**
     * 显示编辑习惯表单
     */
    fun showEditHabitForm(habit: HabitEntity) {
        _state.value = _state.value.copy(
            showAddEditForm = true,
            currentEditHabit = HabitFormData(
                id = habit.id,
                title = habit.title,
                description = habit.description,
                category = habit.getCategoryEnum(),
                icon = habit.icon,
                color = Color(habit.color.toInt()),
                frequencyType = habit.getFrequencyTypeEnum(),
                frequencyCount = habit.frequencyCount,
                frequencyDays = habit.getFrequencyDaysList(),
                timeOfDay = habit.timeOfDay,
                reminder = habit.reminder,
                reminderTime = habit.reminderTime,
                priority = habit.getPriorityEnum(),
                tags = habit.getTagsList()
            )
        )
    }
    
    /**
     * 隐藏表单
     */
    fun hideForm() {
        _state.value = _state.value.copy(
            showAddEditForm = false,
            currentEditHabit = null
        )
    }
    
    /**
     * 保存习惯
     */
    fun saveHabit(formData: HabitFormData) {
        viewModelScope.launch {
            try {
                if (formData.id == null) {
                    // 创建新习惯
                    val id = habitRepository.createHabit(
                        title = formData.title,
                        description = formData.description,
                        category = formData.category,
                        icon = formData.icon,
                        color = formData.color.toArgb().toLong(),
                        frequencyType = formData.frequencyType,
                        frequencyCount = formData.frequencyCount,
                        frequencyDays = formData.frequencyDays,
                        timeOfDay = formData.timeOfDay,
                        reminder = formData.reminder,
                        reminderTime = formData.reminderTime,
                        priority = formData.priority,
                        tags = formData.tags
                    )
                } else {
                    // 编辑现有习惯
                    val habit = habitRepository.getHabitById(formData.id).first()
                    
                    if (habit != null) {
                        val updatedHabit = habit.copy(
                            title = formData.title,
                            description = formData.description,
                            category = formData.category.ordinal,
                            icon = formData.icon,
                            color = formData.color.toArgb().toLong(),
                            frequencyType = formData.frequencyType.ordinal,
                            frequencyCount = formData.frequencyCount,
                            frequencyDaysJson = if (formData.frequencyDays.isNotEmpty()) {
                                com.google.gson.Gson().toJson(formData.frequencyDays)
                            } else null,
                            timeOfDay = formData.timeOfDay,
                            reminder = formData.reminder,
                            reminderTime = formData.reminderTime,
                            priority = formData.priority.ordinal,
                            tagsJson = if (formData.tags.isNotEmpty()) {
                                com.google.gson.Gson().toJson(formData.tags)
                            } else null,
                            updatedAt = Date()
                        )
                        habitRepository.updateHabit(updatedHabit)
                    }
                }
                
                // 隐藏表单
                hideForm()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "保存习惯失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 完成习惯打卡
     */
    fun completeHabit(habitId: String, completed: Boolean = true) {
        viewModelScope.launch {
            try {
                val (newStreak, isNewRecord, totalCompletions) = 
                    habitRepository.completeHabit(habitId, completed)
                
                // 如果完成了习惯，检查是否解锁徽章
                if (completed) {
                    val habit = habitRepository.getHabitById(habitId).first()
                    habit?.let {
                        // 检查连续打卡徽章
                        badgeService.checkAndAwardStreakBadges(it)
                        
                        // 检查累计完成徽章
                        badgeService.checkAndAwardCompletionBadges(it)
                        
                        // 检查多样性徽章
                        badgeService.checkAndAwardVarietyBadges()
                        
                        // 检查特殊成就徽章
                        badgeService.checkAndAwardSpecialBadges()
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "习惯打卡失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 归档习惯
     */
    fun archiveHabit(habitId: String, archive: Boolean = true) {
        viewModelScope.launch {
            try {
                habitRepository.archiveHabit(habitId, archive)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "归档习惯失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 删除习惯
     */
    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
            try {
                habitRepository.deleteHabit(habit)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "删除习惯失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 切换活跃/归档显示
     */
    fun toggleActiveFilter(showOnlyActive: Boolean) {
        _showOnlyActive.value = showOnlyActive
        _state.value = _state.value.copy(showOnlyActive = showOnlyActive)
    }
    
    /**
     * 设置类别过滤器
     */
    fun setCategoryFilter(category: HabitCategory?) {
        _currentFilter.value = category
        _state.value = _state.value.copy(currentFilter = category)
    }
    
    /**
     * 清除错误
     */
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
} 