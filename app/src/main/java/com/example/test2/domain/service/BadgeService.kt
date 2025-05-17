package com.example.test2.domain.service

import com.example.test2.data.local.entity.BadgeCategory
import com.example.test2.data.local.entity.BadgeEntity
import com.example.test2.data.local.entity.BadgeRarity
import com.example.test2.data.local.entity.HabitCategory
import com.example.test2.data.local.entity.HabitEntity
import com.example.test2.data.repository.BadgeRepository
import com.example.test2.data.repository.HabitRepository
import kotlinx.coroutines.flow.first
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 徽章服务
 * 负责检查和授予徽章
 */
@Singleton
class BadgeService @Inject constructor(
    private val badgeRepository: BadgeRepository,
    private val habitRepository: HabitRepository
) {
    /**
     * 初始化默认徽章
     */
    suspend fun initializeDefaultBadges() {
        // 检查是否已初始化
        val existingBadges = badgeRepository.getAllBadges().first()
        if (existingBadges.isNotEmpty()) {
            return
        }
        
        // 连续打卡类徽章
        createStreakBadges()
        
        // 累计完成类徽章
        createCompletionBadges()
        
        // 多样性类徽章
        createVarietyBadges()
        
        // 特殊成就类徽章
        createAchievementBadges()
    }
    
    /**
     * 检查连续打卡徽章
     */
    suspend fun checkAndAwardStreakBadges(habit: HabitEntity) {
        val currentStreak = habit.currentStreak
        
        // 连续打卡里程碑
        val streakMilestones = listOf(3, 7, 14, 21, 30, 60, 100, 365)
        
        // 检查是否达到任何里程碑
        for (milestone in streakMilestones) {
            if (currentStreak == milestone) {
                // 寻找对应的徽章
                val badgeId = "streak_${milestone}_days"
                val badge = badgeRepository.getBadgeById(badgeId)
                
                if (badge != null && !badgeRepository.isUserBadgeUnlocked(badge.id)) {
                    // 解锁徽章
                    badgeRepository.unlockBadge(
                    badgeId = badge.id,
                    habitId = habit.id,
                    valueWhenUnlocked = currentStreak,
                        note = "连续完成 ${habit.title} ${currentStreak} 天"
                    )
                }
            }
        }
    }
    
    /**
     * 检查累计完成徽章
     */
    suspend fun checkAndAwardCompletionBadges(habit: HabitEntity) {
        val totalCompletions = habit.totalCompletions
        
        // 累计完成里程碑
        val completionMilestones = listOf(10, 50, 100, 500, 1000)
        
        // 检查是否达到任何里程碑
        for (milestone in completionMilestones) {
            if (totalCompletions == milestone) {
                // 寻找对应的徽章
                val badgeId = "completion_${milestone}_times"
                val badge = badgeRepository.getBadgeById(badgeId)
                
                if (badge != null && !badgeRepository.isUserBadgeUnlocked(badge.id)) {
                    // 解锁徽章
                    badgeRepository.unlockBadge(
                    badgeId = badge.id,
                    habitId = habit.id,
                    valueWhenUnlocked = totalCompletions,
                        note = "累计完成 ${habit.title} ${totalCompletions} 次"
                    )
                }
            }
        }
        
        // 检查全局累计完成徽章
        checkGlobalCompletionBadges()
    }
    
    /**
     * 检查全局累计完成徽章（所有习惯的总完成次数）
     */
    private suspend fun checkGlobalCompletionBadges() {
        // 获取所有习惯
        val habits = habitRepository.getAllHabits().first()
        
        // 计算所有习惯的总完成次数
        val totalGlobalCompletions = habits.sumOf { it.totalCompletions }
        
        // 全局累计完成里程碑
        val globalMilestones = listOf(50, 100, 500, 1000, 5000)
        
        // 检查是否达到任何里程碑
        for (milestone in globalMilestones) {
            if (totalGlobalCompletions >= milestone) {
                // 寻找对应的徽章
                val badgeId = "global_completion_${milestone}"
                val badge = badgeRepository.getBadgeById(badgeId)
                
                if (badge != null && !badgeRepository.isUserBadgeUnlocked(badge.id)) {
                    // 解锁徽章
                    badgeRepository.unlockBadge(
                    badgeId = badge.id,
                        valueWhenUnlocked = totalGlobalCompletions,
                        note = "累计完成所有习惯 ${totalGlobalCompletions} 次"
                    )
                }
            }
        }
    }
    
    /**
     * 检查多样性徽章
     */
    suspend fun checkAndAwardVarietyBadges() {
        // 获取所有活跃习惯
        val habits = habitRepository.getAllHabits().first().filter { !it.isArchived }
        
        // 统计不同类别的习惯数量
        val categoryCounts = HabitCategory.values().associateWith { category ->
            habits.count { it.getCategoryEnum() == category }
        }
        
        // 检查类别数量徽章
        val uniqueCategories = categoryCounts.count { it.value > 0 }
        
        // 类别数量里程碑
        val categoryMilestones = listOf(3, 5, 8)
        
        // 检查是否达到任何里程碑
        for (milestone in categoryMilestones) {
            if (uniqueCategories >= milestone) {
                // 寻找对应的徽章
                val badgeId = "variety_${milestone}_categories"
                val badge = badgeRepository.getBadgeById(badgeId)
                
                if (badge != null && !badgeRepository.isUserBadgeUnlocked(badge.id)) {
                    // 解锁徽章
                    badgeRepository.unlockBadge(
                        badgeId = badge.id,
                        valueWhenUnlocked = uniqueCategories,
                        note = "创建了 ${uniqueCategories} 种不同类别的习惯"
                    )
                }
            }
        }
        
        // 检查平衡发展徽章（每个类别都有活跃习惯）
        if (categoryCounts.all { it.value > 0 }) {
            val badgeId = "balanced_developer"
            val badge = badgeRepository.getBadgeById(badgeId)
            
            if (badge != null && !badgeRepository.isUserBadgeUnlocked(badge.id)) {
                badgeRepository.unlockBadge(
                        badgeId = badge.id,
                    note = "每个习惯类别都培养了习惯"
                )
            }
        }
    }
    
    /**
     * 检查特殊成就徽章
     */
    suspend fun checkAndAwardSpecialBadges() {
        checkEarlyBirdBadge()
        checkNightOwlBadge()
        checkPerfectWeekBadge()
        checkConsistencyBadge()
    }
    
    /**
     * 检查早起达人徽章
     */
    private suspend fun checkEarlyBirdBadge() {
        // 获取所有习惯的完成日志
        // 逻辑：连续30天在早上8点前完成至少一个习惯
        // 实际实现需要习惯日志数据
    }
    
    /**
     * 检查夜猫子徽章
     */
    private suspend fun checkNightOwlBadge() {
        // 获取所有习惯的完成日志
        // 逻辑：连续14天在晚上10点后完成至少一个习惯
        // 实际实现需要习惯日志数据
    }
    
    /**
     * 检查完美周徽章
     */
    private suspend fun checkPerfectWeekBadge() {
        // 获取所有习惯
        // 逻辑：一周内所有活跃习惯都按计划完成
        // 实际实现需要习惯日志数据
    }
    
    /**
     * 检查坚持不懈徽章
     */
    private suspend fun checkConsistencyBadge() {
        // 获取所有习惯
        // 逻辑：3个月内没有中断过任何一个习惯
        // 实际实现需要习惯日志数据
    }
    
    /**
     * 创建连续打卡类徽章
     */
    private suspend fun createStreakBadges() {
        val streakBadges = listOf(
            Triple("streak_3_days", "初露锋芒", 3),
            Triple("streak_7_days", "一周坚持", 7),
            Triple("streak_14_days", "两周不断", 14),
            Triple("streak_21_days", "习惯养成", 21),
            Triple("streak_30_days", "月度达人", 30),
            Triple("streak_60_days", "持之以恒", 60),
            Triple("streak_100_days", "百日不辍", 100),
            Triple("streak_365_days", "周年庆典", 365)
        )
        
        streakBadges.forEachIndexed { index, (id, name, days) ->
            val rarity = when {
                days >= 365 -> BadgeRarity.LEGENDARY
                days >= 100 -> BadgeRarity.EPIC
                days >= 30 -> BadgeRarity.RARE
                days >= 14 -> BadgeRarity.UNCOMMON
                else -> BadgeRarity.COMMON
            }
            
            val badge = BadgeEntity(
                id = id,
                name = name,
                description = "连续完成同一习惯 $days 天",
                iconName = "streak_badge_$days",
                category = BadgeCategory.STREAK.ordinal,
                rarity = rarity.ordinal,
                condition = "连续完成同一习惯 $days 天",
                thresholdValue = days,
                isDefault = true
            )
            
            badgeRepository.insertBadge(badge)
        }
    }
    
    /**
     * 创建累计完成类徽章
     */
    private suspend fun createCompletionBadges() {
        val completionBadges = listOf(
            Triple("completion_10_times", "起步阶段", 10),
            Triple("completion_50_times", "稳步成长", 50),
            Triple("completion_100_times", "百次打卡", 100),
            Triple("completion_500_times", "专注执行", 500),
            Triple("completion_1000_times", "千次传奇", 1000)
        )
        
        completionBadges.forEachIndexed { index, (id, name, times) ->
            val rarity = when {
                times >= 1000 -> BadgeRarity.LEGENDARY
                times >= 500 -> BadgeRarity.EPIC
                times >= 100 -> BadgeRarity.RARE
                times >= 50 -> BadgeRarity.UNCOMMON
                else -> BadgeRarity.COMMON
            }
            
            val badge = BadgeEntity(
                id = id,
                name = name,
                description = "累计完成同一习惯 $times 次",
                iconName = "completion_badge_$times",
                category = BadgeCategory.COMPLETION.ordinal,
                rarity = rarity.ordinal,
                condition = "累计完成同一习惯 $times 次",
                thresholdValue = times,
                isDefault = true
            )
            
            badgeRepository.insertBadge(badge)
        }
        
        // 全局累计完成徽章
        val globalBadges = listOf(
            Triple("global_completion_50", "习惯养成者", 50),
            Triple("global_completion_100", "生活掌控者", 100),
            Triple("global_completion_500", "习惯大师", 500),
            Triple("global_completion_1000", "自律王者", 1000),
            Triple("global_completion_5000", "传奇人物", 5000)
        )
        
        globalBadges.forEachIndexed { index, (id, name, times) ->
            val rarity = when {
                times >= 5000 -> BadgeRarity.LEGENDARY
                times >= 1000 -> BadgeRarity.EPIC
                times >= 500 -> BadgeRarity.RARE
                times >= 100 -> BadgeRarity.UNCOMMON
                else -> BadgeRarity.COMMON
            }
            
            val badge = BadgeEntity(
                id = id,
                name = name,
                description = "累计完成所有习惯 $times 次",
                iconName = "global_badge_$times",
                category = BadgeCategory.COMPLETION.ordinal,
                rarity = rarity.ordinal,
                condition = "累计完成所有习惯 $times 次",
                thresholdValue = times,
                isDefault = true
            )
            
            badgeRepository.insertBadge(badge)
        }
    }
    
    /**
     * 创建多样性类徽章
     */
    private suspend fun createVarietyBadges() {
        val varietyBadges = listOf(
            Triple("variety_3_categories", "多元尝试", 3),
            Triple("variety_5_categories", "全面发展", 5),
            Triple("variety_8_categories", "生活达人", 8)
        )
        
        varietyBadges.forEachIndexed { index, (id, name, count) ->
            val rarity = when {
                count >= 8 -> BadgeRarity.EPIC
                count >= 5 -> BadgeRarity.RARE
                else -> BadgeRarity.UNCOMMON
            }
            
            val badge = BadgeEntity(
                id = id,
                name = name,
                description = "创建 $count 种不同类别的习惯",
                iconName = "variety_badge_$count",
                category = BadgeCategory.VARIETY.ordinal,
                rarity = rarity.ordinal,
                condition = "创建 $count 种不同类别的习惯",
                thresholdValue = count,
                isDefault = true
            )
            
            badgeRepository.insertBadge(badge)
        }
        
        // 平衡发展徽章
        val balancedBadge = BadgeEntity(
            id = "balanced_developer",
            name = "全面发展",
            description = "在每个类别都培养了习惯",
            iconName = "balanced_badge",
            category = BadgeCategory.VARIETY.ordinal,
            rarity = BadgeRarity.EPIC.ordinal,
            condition = "在每个习惯类别都培养了习惯",
            isDefault = true
        )
        
        badgeRepository.insertBadge(balancedBadge)
    }
    
    /**
     * 创建特殊成就类徽章
     */
    private suspend fun createAchievementBadges() {
        // 早起达人
        val earlyBirdBadge = BadgeEntity(
            id = "early_bird",
            name = "早起达人",
            description = "连续30天在早上8点前完成习惯",
            iconName = "early_bird_badge",
            category = BadgeCategory.ACHIEVEMENT.ordinal,
            rarity = BadgeRarity.RARE.ordinal,
            condition = "连续30天在早上8点前完成任何习惯",
            thresholdValue = 30,
            isDefault = true
        )
        
        badgeRepository.insertBadge(earlyBirdBadge)
        
        // 夜猫子
        val nightOwlBadge = BadgeEntity(
            id = "night_owl",
            name = "夜猫子",
            description = "连续14天在晚上10点后完成习惯",
            iconName = "night_owl_badge",
            category = BadgeCategory.ACHIEVEMENT.ordinal,
            rarity = BadgeRarity.UNCOMMON.ordinal,
            condition = "连续14天在晚上10点后完成任何习惯",
            thresholdValue = 14,
            isDefault = true
        )
        
        badgeRepository.insertBadge(nightOwlBadge)
        
        // 完美周
        val perfectWeekBadge = BadgeEntity(
            id = "perfect_week",
            name = "完美周",
            description = "一周内完成所有计划的习惯",
            iconName = "perfect_week_badge",
            category = BadgeCategory.ACHIEVEMENT.ordinal,
            rarity = BadgeRarity.RARE.ordinal,
            condition = "一周内完成所有计划的习惯，没有遗漏",
            isDefault = true
        )
        
        badgeRepository.insertBadge(perfectWeekBadge)
        
        // 急起直追
        val rapidProgressBadge = BadgeEntity(
            id = "rapid_progress",
            name = "急起直追",
            description = "7天内恢复3个已中断的习惯",
            iconName = "rapid_progress_badge",
            category = BadgeCategory.ACHIEVEMENT.ordinal,
            rarity = BadgeRarity.UNCOMMON.ordinal,
            condition = "在7天内重新开始并持续3个已中断的习惯",
            isDefault = true
        )
        
        badgeRepository.insertBadge(rapidProgressBadge)
    }
} 