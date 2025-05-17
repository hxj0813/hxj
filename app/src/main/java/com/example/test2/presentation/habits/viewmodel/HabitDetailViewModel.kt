package com.example.test2.presentation.habits.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test2.data.local.entity.BadgeEntity
import com.example.test2.data.local.entity.HabitEntity
import com.example.test2.data.local.entity.UserBadgeEntity
import com.example.test2.data.repository.BadgeRepository
import com.example.test2.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 习惯详情视图模型
 */
@HiltViewModel
class HabitDetailViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val badgeRepository: BadgeRepository
) : ViewModel() {
    
    // 习惯数据
    private val _habit = MutableStateFlow<HabitEntity?>(null)
    val habit: StateFlow<HabitEntity?> = _habit.asStateFlow()
    
    // 用户获得的徽章
    private val _userBadges = MutableStateFlow<List<UserBadgeEntity>>(emptyList())
    val userBadges: StateFlow<List<UserBadgeEntity>> = _userBadges.asStateFlow()
    
    // 所有徽章
    private val _allBadges = MutableStateFlow<List<BadgeEntity>>(emptyList())
    val allBadges: StateFlow<List<BadgeEntity>> = _allBadges.asStateFlow()
    
    // 选中的徽章
    private val _selectedBadge = MutableStateFlow<BadgeEntity?>(null)
    val selectedBadge: StateFlow<BadgeEntity?> = _selectedBadge.asStateFlow()
    
    // 选中的用户徽章
    private val _selectedUserBadge = MutableStateFlow<UserBadgeEntity?>(null)
    val selectedUserBadge: StateFlow<UserBadgeEntity?> = _selectedUserBadge.asStateFlow()
    
    // 错误信息
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * 加载习惯数据
     */
    fun loadHabit(habitId: String) {
        viewModelScope.launch {
            try {
                val habitData = habitRepository.getHabitById(habitId).first()
                _habit.value = habitData
            } catch (e: Exception) {
                _error.value = "加载习惯失败: ${e.message}"
            }
        }
    }
    
    /**
     * 加载习惯相关的徽章
     */
    fun loadHabitBadges(habitId: String) {
        viewModelScope.launch {
            try {
                // 加载用户已获得的徽章
                badgeRepository.getUserBadgesByHabit(habitId).collect { badges ->
                    _userBadges.value = badges
                }
                
                // 加载所有徽章
                badgeRepository.getAllBadges().collect { badges ->
                    _allBadges.value = badges
                }
            } catch (e: Exception) {
                _error.value = "加载徽章失败: ${e.message}"
            }
        }
    }
    
    /**
     * 选择徽章查看详情
     */
    fun selectBadge(badge: BadgeEntity, userBadge: UserBadgeEntity) {
        _selectedBadge.value = badge
        _selectedUserBadge.value = userBadge
        
        // 如果是新获得的徽章，标记为已查看
        if (userBadge.highlighted) {
            viewModelScope.launch {
                try {
                    badgeRepository.markBadgeAsViewed(userBadge.id)
                } catch (e: Exception) {
                    // 忽略错误
                }
            }
        }
    }
    
    /**
     * 清除选中的徽章
     */
    fun clearSelectedBadge() {
        _selectedBadge.value = null
        _selectedUserBadge.value = null
    }
    
    /**
     * 清除错误信息
     */
    fun clearError() {
        _error.value = null
    }
} 