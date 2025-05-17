package com.example.test2.data.firebase.repository

import com.example.test2.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase认证存储库接口
 * 定义与认证相关的操作
 */
interface FirebaseAuthRepository {
    /**
     * 观察认证状态变化
     * @return 返回Firebase用户对象的Flow
     */
    fun observeAuthState(): Flow<FirebaseUser?>
    
    /**
     * 用户注册
     * @param email 邮箱
     * @param password 密码
     * @param displayName 显示名称
     * @return 成功则返回User对象，失败则返回异常
     */
    suspend fun register(email: String, password: String, displayName: String = ""): Result<User>
    
    /**
     * 用户登录
     * @param email 邮箱
     * @param password 密码
     * @return 成功则返回User对象，失败则返回异常
     */
    suspend fun login(email: String, password: String): Result<User>
    
    /**
     * 发送密码重置邮件
     * @param email 邮箱地址
     * @return 成功则返回Unit，失败则返回异常
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    
    /**
     * 发送邮箱验证
     * @return 成功则返回Unit，失败则返回异常
     */
    suspend fun sendEmailVerification(): Result<Unit>
    
    /**
     * 获取当前用户
     * @return 当前登录的用户，如果未登录则返回null
     */
    fun getCurrentUser(): FirebaseUser?
    
    /**
     * 用户注销
     */
    fun logout()
}

/**
 * Firebase认证存储库实现
 * 实现认证相关的具体操作
 */
@Singleton
class FirebaseAuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : FirebaseAuthRepository {
    
    override fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        
        auth.addAuthStateListener(authStateListener)
        
        // 初始发送当前用户状态
        trySend(auth.currentUser)
        
        awaitClose {
            auth.removeAuthStateListener(authStateListener)
        }
    }
    
    override suspend fun register(email: String, password: String, displayName: String): Result<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("创建用户后无法获取用户信息"))
            
            // 设置用户显示名称
            if (displayName.isNotBlank()) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
                
                firebaseUser.updateProfile(profileUpdates).await()
            }
            
            // 创建用户文档
            val user = User.fromFirebaseUser(firebaseUser)
            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(user.toMap())
                .await()
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("登录后无法获取用户信息"))
            
            // 更新最后登录时间
            val userRef = firestore.collection("users").document(firebaseUser.uid)
            userRef.update("lastLoginAt", com.google.firebase.Timestamp.now()).await()
            
            // 获取完整的用户数据
            val snapshot = userRef.get().await()
            if (snapshot.exists()) {
                val userData = snapshot.data
                if (userData != null) {
                    Result.success(User.fromMap(userData))
                } else {
                    Result.success(User.fromFirebaseUser(firebaseUser))
                }
            } else {
                // 如果用户文档不存在，则创建一个
                val user = User.fromFirebaseUser(firebaseUser)
                userRef.set(user.toMap()).await()
                Result.success(user)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun sendEmailVerification(): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("用户未登录，无法发送验证邮件"))
            
            currentUser.sendEmailVerification().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
    
    override fun logout() {
        auth.signOut()
    }
} 