package com.example.test2.data.model

import androidx.compose.ui.text.AnnotatedString
import java.util.UUID

/**
 * 内联内容 - 用于富文本编辑器中的文本和图片内容
 */
sealed class InlineContent {
    /**
     * 获取内容ID
     */
    abstract val contentId: String

    /**
     * 内联文本内容
     */
    data class Text(
        val id: String = UUID.randomUUID().toString(),
        val content: String
    ) : InlineContent() {
        override val contentId: String
            get() = id
    }

    /**
     * 内联图片内容
     */
    data class Image(
        val id: String = UUID.randomUUID().toString(), 
        val noteImage: NoteImage,
        val altText: String = ""
    ) : InlineContent() {
        override val contentId: String
            get() = id
    }
    
    companion object {
        /**
         * 将内容列表转换为纯文本
         */
        fun contentListToString(contentList: List<InlineContent>): String {
            return contentList.joinToString("") { content ->
                when (content) {
                    is Text -> content.content
                    is Image -> "[图片]" // 图片在纯文本中的占位符
                }
            }
        }
        
        /**
         * 将内容列表转换为JSON格式，用于存储
         */
        fun contentListToJson(contentList: List<InlineContent>): String {
            // 记录转换前内容类型统计
            val textCount = contentList.count { it is Text }
            val imageCount = contentList.count { it is Image }
            android.util.Log.d("InlineContent", "转换为JSON: $textCount 文本, $imageCount 图片")
            
            if (contentList.isEmpty()) {
                android.util.Log.d("InlineContent", "内容列表为空，返回空JSON")
                return "[]"
            }
            
            // 检查图片内容
            if (imageCount > 0) {
                contentList.filterIsInstance<Image>().forEachIndexed { index, image ->
                    android.util.Log.d("InlineContent", "准备序列化图片[$index]: ID=${image.id}, 图片ID=${image.noteImage.id}, URI=${image.noteImage.uri}")
                }
            }
            
            val contentJson = contentList.mapIndexed { index, content ->
                when (content) {
                    is Text -> {
                        val escapedContent = content.content.replace("\"", "\\\"")
                        val json = """{"type":"text","id":"${content.id}","content":"$escapedContent"}"""
                        android.util.Log.d("InlineContent", "转换文本[$index]: ID=${content.id}, ${content.content.take(20)}...")
                        json
                    }
                    is Image -> {
                        val escapedAltText = content.altText.replace("\"", "\\\"")
                        // 确保URI是正确的格式
                        val uri = content.noteImage.uri
                        val formattedUri = if (uri.startsWith("/") && !uri.startsWith("file://")) {
                            "file://$uri"
                        } else {
                            uri
                        }
                        val imageJson = """{"type":"image","id":"${content.id}","imageId":"${content.noteImage.id}","uri":"$formattedUri","altText":"$escapedAltText"}"""
                        android.util.Log.d("InlineContent", "转换图片[$index]: ID=${content.id}, 图片ID=${content.noteImage.id}, URI=$formattedUri")
                        imageJson
                    }
                }
            }
            
            val result = "[${contentJson.joinToString(",")}]"
            android.util.Log.d("InlineContent", "生成JSON长度: ${result.length}")
            // 检查结果中是否包含图片相关标记
            if (imageCount > 0 && !result.contains("\"type\":\"image\"")) {
                android.util.Log.e("InlineContent", "警告: 图片内容序列化失败，JSON中不包含图片类型")
            }
            return result
        }
        
        /**
         * 从JSON解析内容列表
         */
        fun parseFromJson(json: String, availableImages: List<NoteImage>): List<InlineContent> {
            if (json.isEmpty() || json == "[]") return emptyList()
            
            val resultList = mutableListOf<InlineContent>()
            try {
                android.util.Log.d("InlineContent", "开始解析JSON: ${json.take(100)}...")
                android.util.Log.d("InlineContent", "可用图片: ${availableImages.size} 张")
                
                // 添加更多详细日志
                if (availableImages.isNotEmpty()) {
                    availableImages.forEachIndexed { index, image -> 
                        android.util.Log.d("InlineContent", "可用图片[$index]: ID=${image.id}, URI=${image.uri}")
                    }
                }
                
                // 记录JSON中是否包含图片相关关键字
                val containsImageType = json.contains("\"type\":\"image\"")
                val containsImageId = json.contains("\"imageId\":")
                android.util.Log.d("InlineContent", "JSON中包含图片类型: $containsImageType, 包含图片ID: $containsImageId")
                
                // 简单的JSON解析，实际项目中建议使用Gson或Moshi等库
                val items = json.trimStart('[').trimEnd(']').split("},")
                    .map { if (it.endsWith("}")) it else "$it}" }
                
                android.util.Log.d("InlineContent", "解析出 ${items.size} 个内容项")
                
                for ((idx, item) in items.withIndex()) {
                    android.util.Log.d("InlineContent", "解析内容项[$idx]: ${item.take(50)}...")
                    
                    val type = item.substringAfter("\"type\":\"").substringBefore("\"")
                    val id = item.substringAfter("\"id\":\"").substringBefore("\"")
                    
                    if (type == "text") {
                        val content = item.substringAfter("\"content\":\"").substringBefore("\"")
                            .replace("\\\"", "\"")
                        resultList.add(Text(id, content))
                        android.util.Log.d("InlineContent", "添加文本内容: ID=$id, 内容长度=${content.length}")
                    } else if (type == "image") {
                        val imageId = item.substringAfter("\"imageId\":\"").substringBefore("\"")
                        val uri = item.substringAfter("\"uri\":\"").substringBefore("\"")
                        val altText = if (item.contains("\"altText\":\"")) {
                            item.substringAfter("\"altText\":\"").substringBefore("\"")
                                .replace("\\\"", "\"")
                        } else ""
                        
                        android.util.Log.d("InlineContent", "解析图片内容: ID=$id, 图片ID=$imageId, URI=$uri")
                        
                        // 查找对应的NoteImage
                        val noteImage = availableImages.find { it.id == imageId }
                        
                        if (noteImage != null) {
                            android.util.Log.d("InlineContent", "找到图片 ID=$imageId: ${noteImage.uri}")
                            resultList.add(Image(id, noteImage, altText))
                        } else {
                            // 如果找不到对应的图片，使用URI创建新的NoteImage
                            android.util.Log.d("InlineContent", "未找到图片 ID=$imageId，创建新图片对象")
                            
                            // 检查是否有URI匹配的图片
                            val imageByUri = availableImages.find { it.uri == uri }
                            if (imageByUri != null) {
                                android.util.Log.d("InlineContent", "通过URI找到图片: ID=${imageByUri.id}, URI=$uri")
                                resultList.add(Image(id, imageByUri, altText))
                            } else {
                                // 创建新的NoteImage
                                val newImage = NoteImage(imageId, uri, "", java.util.Date())
                                android.util.Log.d("InlineContent", "创建新图片对象: ID=$imageId, URI=$uri")
                                resultList.add(Image(id, newImage, altText))
                            }
                        }
                    }
                }
                
                android.util.Log.d("InlineContent", "解析完成，共 ${resultList.size} 个内容项")
                // 记录内容类型分布
                val textCount = resultList.count { it is Text }
                val imageCount = resultList.count { it is Image }
                android.util.Log.d("InlineContent", "内容类型分布: $textCount 文本, $imageCount 图片")
            } catch (e: Exception) {
                android.util.Log.e("InlineContent", "解析JSON失败: ${e.message}", e)
            }
            
            return resultList
        }
        
        /**
         * 将普通文本内容转换为内联内容列表
         */
        fun fromPlainText(text: String): List<InlineContent> {
            return if (text.isNotEmpty()) {
                listOf(Text(content = text))
            } else {
                emptyList()
            }
        }
    }
} 