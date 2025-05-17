package com.example.test2.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test2.data.firebase.repository.FirebaseAuthRepository
import com.example.test2.data.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 认证状态
 */
data class AuthState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val isLoginSuccess: Boolean = false,
    val isRegistrationSuccess: Boolean = false,
    val isPasswordResetEmailSent: Boolean = false,
)

/**
 * 认证视图模型
 * 处理登录、注册、密码重置等认证相关的UI逻辑
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: FirebaseAuthRepository
) : ViewModel() {
    
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    init {
        // 检查是否已经登录
        viewModelScope.launch {
            authRepository.observeAuthState().collect { firebaseUser ->
                firebaseUser?.let {
                    _authState.update { state ->
                        state.copy(
                            user = User.fromFirebaseUser(it),
                            isLoginSuccess = true,
                            error = null
                        )
                    }
                } ?: run {
                    _authState.update { state ->
                        state.copy(
                            user = null,
                            isLoginSuccess = false
                        )
                    }
                }
            }
        }
    }
    
    /**
     * 注册新用户
     * @param email 邮箱
     * @param password 密码
     * @param displayName 显示名称
     */
    fun register(email: String, password: String, displayName: String = "") {
        _authState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            val result = authRepository.register(email, password, displayName)
            
            result.fold(
                onSuccess = { user ->
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            user = user,
                            isRegistrationSuccess = true,
                            error = null
                        )
                    }
                    
                    // 自动发送邮箱验证
                    authRepository.sendEmailVerification()
                },
                onFailure = { exception ->
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            isRegistrationSuccess = false,
                            error = getErrorMessage(exception)
                        )
                    }
                }
            )
        }
    }
    
    /**
     * 用户登录
     * @param email 邮箱
     * @param password 密码
     */
    fun login(email: String, password: String) {
        _authState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            try {
                // 尝试最多3次登录
                var retryCount = 0
                var lastError: String? = null
                var succeeded = false
                
                while (retryCount < 3 && !succeeded) {
                    try {
                        val result = authRepository.login(email, password)
                        
                        result.fold(
                            onSuccess = { user ->
                                _authState.update {
                                    it.copy(
                                        isLoading = false,
                                        user = user,
                                        isLoginSuccess = true,
                                        error = null
                                    )
                                }
                                succeeded = true
                            },
                            onFailure = { exception ->
                                android.util.Log.e("AuthViewModel", "登录失败(尝试${retryCount+1}/3): ${exception.message}", exception)
                                // 保存错误信息而不是异常对象
                                lastError = handleNetworkError(exception)
                                
                                // 如果是网络或SSL错误，重试
                                if (exception.message?.contains("SSL") == true || 
                                    exception.message?.contains("Connection") == true ||
                                    exception.message?.contains("network") == true) {
                                    retryCount++
                                    kotlinx.coroutines.delay(1000) // 延迟1秒再重试
                                } else {
                                    // 非网络错误，直接失败
                                    throw exception
                                }
                            }
                        )
                    } catch (e: Exception) {
                        // 保存错误信息而不是异常对象
                        lastError = handleNetworkError(e)
                        
                        // 如果是网络错误，等待一会再重试
                        if (e.message?.contains("SSL") == true || 
                            e.message?.contains("Connection") == true ||
                            e.message?.contains("network") == true) {
                            retryCount++
                            if (retryCount < 3) kotlinx.coroutines.delay(1000)
                        } else {
                            // 非网络错误，不重试
                            break
                        }
                    }
                }
                
                // 如果所有重试都失败了
                if (!succeeded && lastError != null) {
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            isLoginSuccess = false,
                            error = lastError
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "登录过程发生异常: ${e.message}", e)
                _authState.update {
                    it.copy(
                        isLoading = false,
                        isLoginSuccess = false,
                        error = handleNetworkError(e)
                    )
                }
            }
        }
    }
    
    /**
     * 处理网络相关错误，返回更友好的错误信息
     */
    private fun handleNetworkError(exception: Throwable): String {
        return when {
            exception.message?.contains("SSL handshake") == true -> 
                "SSL连接问题，请检查网络并重试。您可能需要切换到更稳定的网络。"
            exception.message?.contains("Connection reset") == true -> 
                "网络连接被重置，请检查您的网络连接并重试。"
            exception.message?.contains("timeout") == true -> 
                "连接超时，请检查网络并重试。"
            exception.message?.contains("Unable to resolve host") == true -> 
                "无法连接到服务器，请检查您的网络连接是否正常。"
            else -> getErrorMessage(exception)
        }
    }
    
    /**
     * 发送密码重置邮件
     * @param email 邮箱地址
     */
    fun sendPasswordResetEmail(email: String) {
        _authState.update { it.copy(isLoading = true, error = null, isPasswordResetEmailSent = false) }
        
        viewModelScope.launch {
            val result = authRepository.sendPasswordResetEmail(email)
            
            result.fold(
                onSuccess = {
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            isPasswordResetEmailSent = true,
                            error = null
                        )
                    }
                },
                onFailure = { exception ->
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            isPasswordResetEmailSent = false,
                            error = getErrorMessage(exception)
                        )
                    }
                }
            )
        }
    }
    
    /**
     * 用户注销
     */
    fun logout() {
        _authState.update { it.copy(isLoading = true) }
        
        authRepository.logout()
        
        _authState.update {
            it.copy(
                isLoading = false,
                user = null,
                isLoginSuccess = false,
                isRegistrationSuccess = false,
                error = null
            )
        }
    }
    
    /**
     * 重置错误状态
     */
    fun resetError() {
        _authState.update { it.copy(error = null) }
    }
    
    /**
     * 重置成功状态
     */
    fun resetSuccessState() {
        _authState.update {
            it.copy(
                isLoginSuccess = false,
                isRegistrationSuccess = false,
                isPasswordResetEmailSent = false
            )
        }
    }
    
    /**
     * 获取Firebase错误消息
     */
    private fun getErrorMessage(exception: Throwable): String {
        return when (exception.message) {
            "The email address is badly formatted." -> "邮箱格式不正确"
            "The password is invalid or the user does not have a password." -> "密码不正确"
            "There is no user record corresponding to this identifier. The user may have been deleted." -> "用户不存在"
            "The email address is already in use by another account." -> "该邮箱已被注册"
            "Password should be at least 6 characters" -> "密码至少需要6个字符"
            "A network error (such as timeout, interrupted connection or unreachable host) has occurred." -> "网络错误，请检查您的网络连接"
            else -> exception.message ?: "未知错误"
        }
    }
} 