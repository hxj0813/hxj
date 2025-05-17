package com.example.test2.presentation.habits.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test2.data.local.entity.BadgeCategory
import com.example.test2.data.local.entity.BadgeEntity
import com.example.test2.data.local.entity.BadgeRarity
import com.example.test2.data.local.entity.UserBadgeEntity
import com.example.test2.data.repository.BadgeRepository
import com.example.test2.domain.service.BadgeService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

/**
 * 徽章视图模型
 */
@HiltViewModel
class BadgeViewModel @Inject constructor(
    private val badgeRepository: BadgeRepository,
    private val badgeService: BadgeService
) : ViewModel() {
    
    // 徽章列表
    private val _badgesState = MutableStateFlow<List<BadgeEntity>>(emptyList())
    val badgesState: StateFlow<List<BadgeEntity>> = _badgesState.asStateFlow()
    
    // 用户徽章
    private val _userBadgesState = MutableStateFlow<List<UserBadgeEntity>>(emptyList())
    val userBadgesState: StateFlow<List<UserBadgeEntity>> = _userBadgesState.asStateFlow()
    
    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // 错误信息
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // 新获得的徽章
    private val _newBadges = MutableStateFlow<List<UserBadgeEntity>>(emptyList())
    val newBadges: StateFlow<List<UserBadgeEntity>> = _newBadges.asStateFlow()
    
    // 已选择的类别
    private val _selectedCategory = MutableStateFlow<BadgeCategory?>(null)
    val selectedCategory: StateFlow<BadgeCategory?> = _selectedCategory.asStateFlow()
    
    /**
     * 初始化默认徽章
     */
    fun initializeBadges() {
        viewModelScope.launch {
            try {
                badgeService.initializeDefaultBadges()
            } catch (e: Exception) {
                _error.value = "初始化徽章失败：${e.message}"
            }
        }
    }
    
    /**
     * 加载所有徽章
     */
    fun loadAllBadges() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                combine(
                    badgeRepository.getAllBadges(),
                    badgeRepository.getUserBadges()
                ) { allBadges, userBadges ->
                    Pair(allBadges, userBadges) // 将两个结果打包成 Pair
                }.collect { (allBadges, userBadges) ->
                    _badgesState.value = allBadges
                    _userBadgesState.value = userBadges
                    _newBadges.value = userBadges.filter { it.highlighted }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "加载徽章失败：${e.message}"
                _isLoading.value = false
            }
        }
    }



    /**
     * 按类别筛选徽章
     */
    fun setCategory(category: BadgeCategory?) {
        _selectedCategory.value = category
        if (category != null) {
            loadBadgesByCategory(category)
        } else {
            loadAllBadges()
        }
    }
    
    /**
     * 加载特定类别的徽章
     */
    private fun loadBadgesByCategory(category: BadgeCategory) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                badgeRepository.getBadgesByCategory(category)
                    .collect { badges ->
                        _badgesState.value = badges
                    }
                    
                badgeRepository.getUserBadgesByCategory(category)
                    .collect { userBadges ->
                        _userBadgesState.value = userBadges
                    }
                    
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "加载徽章失败：${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 标记徽章为已查看
     */
    fun markBadgeAsViewed(userBadgeId: String) {
        viewModelScope.launch {
            try {
                badgeRepository.markBadgeAsViewed(userBadgeId)
                
                // 更新新徽章列表
                _newBadges.value = _newBadges.value.filter { it.id != userBadgeId }
            } catch (e: Exception) {
                _error.value = "标记徽章失败：${e.message}"
            }
        }
    }
    
    /**
     * 清除所有新徽章高亮
     */
    fun clearAllNewBadges() {
        viewModelScope.launch {
            try {
                _newBadges.value.forEach { badge ->
                    badgeRepository.markBadgeAsViewed(badge.id)
                }
                
                _newBadges.value = emptyList()
            } catch (e: Exception) {
                _error.value = "清除徽章高亮失败：${e.message}"
            }
        }
    }
    
    /**
     * 清除错误信息
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * 获取徽章详情
     */
    suspend fun getBadgeDetails(badgeId: String): BadgeEntity? {
        return badgeRepository.getBadgeById(badgeId)
    }
    
    /**
     * 获取用户徽章详情
     */
    fun getUserBadgeForHabit(habitId: String): Flow<List<UserBadgeEntity>> {
        return badgeRepository.getUserBadgesByHabit(habitId)
    }
} 