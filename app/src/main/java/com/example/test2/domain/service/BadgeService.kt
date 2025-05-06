package com.example.test2.domain.service

import com.example.test2.data.local.dao.BadgeDao
import com.example.test2.data.local.dao.HabitDao
import com.example.test2.data.local.dao.UserBadgeDao
import com.example.test2.data.local.entity.BadgeEntity
import com.example.test2.data.local.entity.BadgeType
import com.example.test2.data.local.entity.BadgeRarity
import com.example.test2.data.local.entity.HabitCategory
import com.example.test2.data.local.entity.HabitEntity
import com.example.test2.data.local.entity.UserBadgeEntity
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 成就服务
 * 处理徽章发放、进度更新和条件检查
 */
@Singleton
class BadgeService @Inject constructor(
    private val badgeDao: BadgeDao,
    private val userBadgeDao: UserBadgeDao,
    private val habitDao: HabitDao
) {
    /**
     * 检查并颁发连续打卡徽章
     * @return 新解锁的徽章列表
     */
    suspend fun checkAndAwardStreakBadges(habit: HabitEntity): List<BadgeEntity> {
        val currentStreak = habit.currentStreak
        if (currentStreak <= 0) return emptyList()
        
        // 获取所有连续打卡徽章
        val streakBadges = badgeDao.getBadgesByType(BadgeType.STREAK.ordinal).first()
            .filter { badge -> badge.requiredValue <= currentStreak }
            .filter { badge -> 
                // 类别筛选：如果徽章指定了类别，则必须匹配
                badge.requiredCategoryId?.let { it == habit.category } ?: true 
            }
        
        val newBadges = mutableListOf<BadgeEntity>()
        
        // 检查每个徽章
        for (badge in streakBadges) {
            // 检查用户是否已拥有这个徽章
            val habitHasBadge = userBadgeDao.hasHabitBadge(badge.id, habit.id)
            
            if (!habitHasBadge) {
                // 创建用户徽章记录
                val userBadge = UserBadgeEntity.create(
                    badgeId = badge.id,
                    habitId = habit.id,
                    valueWhenUnlocked = currentStreak,
                    note = "连续完成${currentStreak}天习惯「${habit.title}」"
                )
                userBadgeDao.insertUserBadge(userBadge)
                newBadges.add(badge)
                
                // 更新习惯的徽章信息
                updateHabitBadges(habit)
            }
        }
        
        return newBadges
    }
    
    /**
     * 检查并颁发累计完成徽章
     * @return 新解锁的徽章列表
     */
    suspend fun checkAndAwardCompletionBadges(habit: HabitEntity): List<BadgeEntity> {
        val totalCompletions = habit.totalCompletions
        if (totalCompletions <= 0) return emptyList()
        
        // 获取所有累计完成徽章
        val completionBadges = badgeDao.getBadgesByType(BadgeType.COMPLETION.ordinal).first()
            .filter { badge -> badge.requiredValue <= totalCompletions }
            .filter { badge -> 
                badge.requiredCategoryId?.let { it == habit.category } ?: true 
            }
        
        val newBadges = mutableListOf<BadgeEntity>()
        
        // 检查每个徽章
        for (badge in completionBadges) {
            // 检查用户是否已拥有这个徽章
            val habitHasBadge = userBadgeDao.hasHabitBadge(badge.id, habit.id)
            
            if (!habitHasBadge) {
                // 创建用户徽章记录
                val userBadge = UserBadgeEntity.create(
                    badgeId = badge.id,
                    habitId = habit.id,
                    valueWhenUnlocked = totalCompletions,
                    note = "累计完成${totalCompletions}次习惯「${habit.title}」"
                )
                userBadgeDao.insertUserBadge(userBadge)
                newBadges.add(badge)
                
                // 更新习惯的徽章信息
                updateHabitBadges(habit)
            }
        }
        
        return newBadges
    }
    
    /**
     * 检查并颁发多样性徽章（完成不同类别习惯）
     * @return 新解锁的徽章列表
     */
    suspend fun checkAndAwardVarietyBadges(): List<BadgeEntity> {
        // 获取用户活跃习惯的类别数量
        val activeCategories = habitDao.getActiveHabitCategoriesCount()
        
        // 获取所有多样性徽章
        val varietyBadges = badgeDao.getBadgesByType(BadgeType.VARIETY.ordinal).first()
            .filter { badge -> badge.requiredValue <= activeCategories }
        
        val newBadges = mutableListOf<BadgeEntity>()
        
        // 检查每个徽章
        for (badge in varietyBadges) {
            // 检查用户是否已拥有这个徽章
            val userHasBadge = userBadgeDao.hasUserBadge(badge.id)
            
            if (!userHasBadge) {
                // 创建用户徽章记录
                val userBadge = UserBadgeEntity.create(
                    badgeId = badge.id,
                    habitId = null, // 全局徽章，不关联特定习惯
                    valueWhenUnlocked = activeCategories,
                    note = "培养了${activeCategories}种不同类别的习惯"
                )
                userBadgeDao.insertUserBadge(userBadge)
                newBadges.add(badge)
            }
        }
        
        return newBadges
    }
    
    /**
     * 检查并颁发特殊成就徽章
     * @return 新解锁的徽章列表
     */
    suspend fun checkAndAwardSpecialBadges(): List<BadgeEntity> {
        val newBadges = mutableListOf<BadgeEntity>()
        
        // 检查最长连续记录成就
        val longestStreakHabit = habitDao.getHabitWithLongestStreak()
        if (longestStreakHabit != null && longestStreakHabit.bestStreak >= 30) {
            val specialBadges = badgeDao.getBadgesByType(BadgeType.SPECIAL.ordinal).first()
                .filter { badge -> 
                    val conditions = badge.getConditions()
                    conditions["type"] == "longestStreak" && 
                    (conditions["value"] as? Double)?.toInt() ?: 0 <= longestStreakHabit.bestStreak
                }
            
            for (badge in specialBadges) {
                if (!userBadgeDao.hasUserBadge(badge.id)) {
                    val userBadge = UserBadgeEntity.create(
                        badgeId = badge.id,
                        habitId = longestStreakHabit.id,
                        valueWhenUnlocked = longestStreakHabit.bestStreak,
                        note = "在习惯「${longestStreakHabit.title}」中创造了${longestStreakHabit.bestStreak}天的连续记录！"
                    )
                    userBadgeDao.insertUserBadge(userBadge)
                    newBadges.add(badge)
                }
            }
        }
        
        // 检查完成总次数成就
        val mostCompletedHabit = habitDao.getMostCompletedHabit()
        if (mostCompletedHabit != null && mostCompletedHabit.totalCompletions >= 100) {
            val specialBadges = badgeDao.getBadgesByType(BadgeType.SPECIAL.ordinal).first()
                .filter { badge -> 
                    val conditions = badge.getConditions()
                    conditions["type"] == "mostCompletions" && 
                    (conditions["value"] as? Double)?.toInt() ?: 0 <= mostCompletedHabit.totalCompletions
                }
            
            for (badge in specialBadges) {
                if (!userBadgeDao.hasUserBadge(badge.id)) {
                    val userBadge = UserBadgeEntity.create(
                        badgeId = badge.id,
                        habitId = mostCompletedHabit.id,
                        valueWhenUnlocked = mostCompletedHabit.totalCompletions,
                        note = "在习惯「${mostCompletedHabit.title}」中累计完成了${mostCompletedHabit.totalCompletions}次！"
                    )
                    userBadgeDao.insertUserBadge(userBadge)
                    newBadges.add(badge)
                }
            }
        }
        
        return newBadges
    }
    
    /**
     * 更新习惯的徽章信息
     */
    private suspend fun updateHabitBadges(habit: HabitEntity) {
        // 获取习惯已获得的徽章
        val habitBadges = userBadgeDao.getBadgesForHabit(habit.id).first()
        
        // 更新习惯的徽章计数和JSON数据
        habitDao.updateHabitBadges(
            habitId = habit.id,
            badgeCount = habitBadges.size,
            badgesJson = Gson().toJson(habitBadges.map { it.badgeId }),
            updateDate = Date()
        )
    }
    
    /**
     * 初始化默认徽章
     * 在应用首次启动时调用
     */
    suspend fun initializeDefaultBadges() {
        // 检查是否已经有徽章数据
        val existingBadges = badgeDao.getAllBadges().first()
        if (existingBadges.isNotEmpty()) return
        
        val defaultBadges = mutableListOf<BadgeEntity>()
        
        // 连续天数徽章
        defaultBadges.add(BadgeEntity.create(
            name = "坚持一周",
            description = "连续7天完成同一习惯",
            type = BadgeType.STREAK,
            rarity = BadgeRarity.COMMON,
            icon = "badge_streak_7",
            color = 0xFF4CAF50,
            requiredValue = 7
        ))
        
        defaultBadges.add(BadgeEntity.create(
            name = "坚持三周",
            description = "连续21天完成同一习惯，开始养成习惯！",
            type = BadgeType.STREAK,
            rarity = BadgeRarity.UNCOMMON,
            icon = "badge_streak_21",
            color = 0xFF2196F3,
            requiredValue = 21
        ))
        
        defaultBadges.add(BadgeEntity.create(
            name = "66天挑战",
            description = "连续66天完成同一习惯，习惯已经形成！",
            type = BadgeType.STREAK,
            rarity = BadgeRarity.RARE,
            icon = "badge_streak_66",
            color = 0xFF9C27B0,
            requiredValue = 66
        ))
        
        defaultBadges.add(BadgeEntity.create(
            name = "百日打卡",
            description = "连续100天完成同一习惯，你的毅力令人敬佩！",
            type = BadgeType.STREAK,
            rarity = BadgeRarity.EPIC,
            icon = "badge_streak_100",
            color = 0xFFFF9800,
            requiredValue = 100
        ))
        
        defaultBadges.add(BadgeEntity.create(
            name = "年度坚持",
            description = "连续365天完成同一习惯，你简直是意志力大师！",
            type = BadgeType.STREAK,
            rarity = BadgeRarity.LEGENDARY,
            icon = "badge_streak_365",
            color = 0xFFF44336,
            requiredValue = 365
        ))
        
        // 累计完成徽章
        defaultBadges.add(BadgeEntity.create(
            name = "初学者",
            description = "累计完成10次习惯",
            type = BadgeType.COMPLETION,
            rarity = BadgeRarity.COMMON,
            icon = "badge_completion_10",
            color = 0xFF4CAF50,
            requiredValue = 10
        ))
        
        defaultBadges.add(BadgeEntity.create(
            name = "进行时",
            description = "累计完成50次习惯",
            type = BadgeType.COMPLETION,
            rarity = BadgeRarity.UNCOMMON,
            icon = "badge_completion_50",
            color = 0xFF2196F3,
            requiredValue = 50
        ))
        
        defaultBadges.add(BadgeEntity.create(
            name = "百次达成",
            description = "累计完成100次习惯",
            type = BadgeType.COMPLETION,
            rarity = BadgeRarity.RARE,
            icon = "badge_completion_100",
            color = 0xFF9C27B0,
            requiredValue = 100
        ))
        
        defaultBadges.add(BadgeEntity.create(
            name = "习惯大师",
            description = "累计完成500次习惯",
            type = BadgeType.COMPLETION,
            rarity = BadgeRarity.EPIC,
            icon = "badge_completion_500",
            color = 0xFFFF9800,
            requiredValue = 500
        ))
        
        defaultBadges.add(BadgeEntity.create(
            name = "千次成就",
            description = "累计完成1000次习惯，你已经掌握了自律的真谛！",
            type = BadgeType.COMPLETION,
            rarity = BadgeRarity.LEGENDARY,
            icon = "badge_completion_1000",
            color = 0xFFF44336,
            requiredValue = 1000
        ))
        
        // 多样性徽章
        defaultBadges.add(BadgeEntity.create(
            name = "多元初体验",
            description = "培养3种不同类别的习惯",
            type = BadgeType.VARIETY,
            rarity = BadgeRarity.UNCOMMON,
            icon = "badge_variety_3",
            color = 0xFF2196F3,
            requiredValue = 3
        ))
        
        defaultBadges.add(BadgeEntity.create(
            name = "全面发展",
            description = "培养5种不同类别的习惯",
            type = BadgeType.VARIETY,
            rarity = BadgeRarity.RARE,
            icon = "badge_variety_5",
            color = 0xFF9C27B0,
            requiredValue = 5
        ))
        
        defaultBadges.add(BadgeEntity.create(
            name = "习惯收藏家",
            description = "培养全部8种类别的习惯",
            type = BadgeType.VARIETY,
            rarity = BadgeRarity.LEGENDARY,
            icon = "badge_variety_all",
            color = 0xFFF44336,
            requiredValue = 8
        ))
        
        // 特殊成就徽章
        val specialConditions = mapOf(
            "type" to "longestStreak",
            "value" to 30
        )
        defaultBadges.add(BadgeEntity.create(
            name = "不屈意志",
            description = "创造30天或更长的连续记录",
            type = BadgeType.SPECIAL,
            rarity = BadgeRarity.EPIC,
            icon = "badge_special_streak",
            color = 0xFFFF9800,
            requiredValue = 1,
            conditions = specialConditions
        ))
        
        val completionsConditions = mapOf(
            "type" to "mostCompletions",
            "value" to 100
        )
        defaultBadges.add(BadgeEntity.create(
            name = "持之以恒",
            description = "在一个习惯中累计完成100次以上",
            type = BadgeType.SPECIAL,
            rarity = BadgeRarity.EPIC,
            icon = "badge_special_consistent",
            color = 0xFFFF9800,
            requiredValue = 1,
            conditions = completionsConditions
        ))
        
        // 类别特定徽章（示例：健康类）
        defaultBadges.add(BadgeEntity.create(
            name = "健康达人",
            description = "连续21天完成健康类习惯",
            type = BadgeType.STREAK,
            rarity = BadgeRarity.RARE,
            icon = "badge_health_streak",
            color = 0xFF4CAF50,
            requiredValue = 21,
            requiredCategoryId = HabitCategory.HEALTH.ordinal
        ))
        
        // 批量插入所有默认徽章
        badgeDao.insertBadges(defaultBadges)
    }
} 