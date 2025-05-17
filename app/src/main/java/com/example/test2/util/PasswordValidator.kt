package com.example.test2.util

/**
 * 密码验证工具类
 * 用于检查密码强度和提供密码格式反馈
 */
object PasswordValidator {

    /**
     * 密码验证结果
     * @property isValid 是否有效
     * @property errorMessage 错误信息
     */
    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null,
        val strength: PasswordStrength = PasswordStrength.WEAK
    )

    /**
     * 密码强度等级
     */
    enum class PasswordStrength {
        WEAK,      // 弱密码 (不符合要求)
        MEDIUM,    // 中等强度 (符合基本要求)
        STRONG     // 强密码 (超过基本要求)
    }

    /**
     * 验证密码是否符合要求
     * 密码必须：
     * 1. 至少8个字符
     * 2. 包含至少一个小写字母
     * 3. 包含至少一个大写字母
     * 4. 包含至少一个数字
     * 
     * @param password 待验证的密码
     * @return 验证结果，包含是否有效和错误信息
     */
    fun validatePassword(password: String): ValidationResult {
        val minLength = 8
        
        // 长度检查
        if (password.length < minLength) {
            return ValidationResult(
                isValid = false,
                errorMessage = "密码长度不能少于${minLength}个字符",
                strength = PasswordStrength.WEAK
            )
        }
        
        // 检查是否包含小写字母
        if (!password.any { it.isLowerCase() }) {
            return ValidationResult(
                isValid = false,
                errorMessage = "密码必须包含至少一个小写字母",
                strength = PasswordStrength.WEAK
            )
        }
        
        // 检查是否包含大写字母
        if (!password.any { it.isUpperCase() }) {
            return ValidationResult(
                isValid = false,
                errorMessage = "密码必须包含至少一个大写字母",
                strength = PasswordStrength.WEAK
            )
        }
        
        // 检查是否包含数字
        if (!password.any { it.isDigit() }) {
            return ValidationResult(
                isValid = false,
                errorMessage = "密码必须包含至少一个数字",
                strength = PasswordStrength.WEAK
            )
        }
        
        // 特殊字符检查，可选
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }
        
        // 确定密码强度
        val strength = when {
            hasSpecialChar && password.length >= 12 -> PasswordStrength.STRONG
            password.length >= 10 -> PasswordStrength.STRONG
            else -> PasswordStrength.MEDIUM
        }
        
        return ValidationResult(
            isValid = true,
            errorMessage = null,
            strength = strength
        )
    }
    
    /**
     * 获取密码强度的描述
     */
    fun getStrengthDescription(strength: PasswordStrength): String {
        return when (strength) {
            PasswordStrength.WEAK -> "弱密码"
            PasswordStrength.MEDIUM -> "中等强度"
            PasswordStrength.STRONG -> "强密码"
        }
    }
    
    /**
     * 生成密码要求描述文本
     */
    fun getPasswordRequirements(): String {
        return "密码必须包含:\n" +
               "• 至少8个字符\n" +
               "• 至少一个小写字母(a-z)\n" +
               "• 至少一个大写字母(A-Z)\n" +
               "• 至少一个数字(0-9)"
    }
} 