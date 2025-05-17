package com.example.test2.data.model

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import java.util.Date

/**
 * 用户数据模型
 * 存储用户的基本信息和设置
 *
 * @property uid 用户唯一ID
 * @property email 用户邮箱
 * @property displayName 显示名称
 * @property photoUrl 头像URL
 * @property emailVerified 邮箱是否已验证
 * @property createdAt 账户创建时间
 * @property lastLoginAt 最后登录时间
 * @property isDataSyncEnabled 是否启用云同步
 */
data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val emailVerified: Boolean = false,
    val createdAt: Date = Date(),
    val lastLoginAt: Date = Date(),
    val isDataSyncEnabled: Boolean = true // 默认启用数据同步
) {
    /**
     * 将用户数据转换为Firestore可存储的Map
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "email" to email,
            "displayName" to displayName,
            "photoUrl" to photoUrl,
            "emailVerified" to emailVerified,
            "createdAt" to Timestamp(createdAt.time / 1000, ((createdAt.time % 1000) * 1000000).toInt()),
            "lastLoginAt" to Timestamp(lastLoginAt.time / 1000, ((lastLoginAt.time % 1000) * 1000000).toInt()),
            "isDataSyncEnabled" to isDataSyncEnabled
        )
    }
    
    companion object {
        /**
         * 从FirebaseUser创建用户数据模型
         */
        fun fromFirebaseUser(firebaseUser: FirebaseUser): User {
            return User(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName ?: "",
                photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                emailVerified = firebaseUser.isEmailVerified,
                createdAt = Date(), // Firebase不直接提供创建时间，使用当前时间
                lastLoginAt = Date()
            )
        }
        
        /**
         * 从Firestore文档数据创建用户数据模型
         */
        fun fromMap(data: Map<String, Any>): User {
            return User(
                uid = data["uid"] as? String ?: "",
                email = data["email"] as? String ?: "",
                displayName = data["displayName"] as? String ?: "",
                photoUrl = data["photoUrl"] as? String ?: "",
                emailVerified = data["emailVerified"] as? Boolean ?: false,
                createdAt = (data["createdAt"] as? Timestamp)?.toDate() ?: Date(),
                lastLoginAt = (data["lastLoginAt"] as? Timestamp)?.toDate() ?: Date(),
                isDataSyncEnabled = data["isDataSyncEnabled"] as? Boolean ?: true
            )
        }
    }
} 