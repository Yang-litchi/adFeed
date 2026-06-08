# 数据层与 AI 接口集成设计文档：AI广告推荐信息流

本项目致力于重构并搭建一个具备高性能列表滚动、本地 Room 数据库持久化、网络层封装与故障 Mock 切换，以及大模型 AI 智能增强能力的单列广告信息流 Android 应用。

根据架构决策，项目采用 **单模块包分层架构 (Single-Module Package-Based Architecture)**，所有开发直接在 `:app` 模块内通过清晰的 package 分包进行。

---

## 目录
1. [项目架构总览](#1-项目架构总览)
2. [分包结构设计 (Package Structure)](#2-分包结构设计-package-structure)
3. [数据模型定义与接口协议](#3-数据模型定义与接口协议)
4. [网络层封装与 Mock 拦截器设计](#4-网络层封装与-mock-拦截器设计)
5. [本地 Room 数据库设计](#5-本地-room-数据库设计)
6. [媒体资源加载与缓存策略](#6-媒体资源加载与缓存策略)
7. [AI 增强接口重构与优雅降级机制](#7-ai-增强接口重构与优雅降级机制)
8. [落地实施与重构步骤](#8-落地实施与重构步骤)

---

## 1. 项目架构总览

项目采用 **MVVM** 架构设计。表现层（Presentation）使用 Jetpack Compose 进行状态驱动的 UI 开发；数据层（Data）通过 Repository 屏蔽数据来源细节；AI 接口直接接入统一封装的网络层。

在当前开发阶段，大模型 API（AI 介绍生成、AI 产品顾问问答）将通过 **OkHttp Mock 拦截器进行本地模拟**（模拟网络延迟、重试及随机网络异常），同时提供 Mock/Real 开关以支持未来平滑切换至真实的 DashScope (通义千问) 接口。

---

## 2. 分包结构设计 (Package Structure)

在当前的 `app/src/main/java/com/example/adfeed/` 目录下，我们通过以下包结构进行架构分层开发：

```
com.example.adfeed/
├── MainActivity.kt               # 应用唯一 Activity 兼 Navigation 路由配置
├── core/                         # 基础核心层
│   ├── network/                  # OkHttp / Retrofit 封装（支持超时、重试、错误码、Mock 拦截器）
│   ├── db/                       # Room 数据库配置 (AppDatabase)
│   └── util/                     # Coil 图片加载与 ExoPlayer 视频缓存工具
├── data/                         # 数据层
│   ├── model/                    # 数据业务模型 (AdItem, AiInfo, Tag)
│   ├── local/                    # Room Database Dao 与 Entity 定义
│   ├── remote/                   # Retrofit API 接口及大模型网关实现
│   └── repository/               # Repository 接口与实现 (AdRepositoryImpl, InteractionRepositoryImpl)
├── ui/                           # 表现层 (Jetpack Compose UI)
│   ├── splash/                   # 启动页 Screen
│   ├── feed/                     # 信息流列表页及卡片 (LargeImage, SmallImage, Video)
│   ├── detail/                   # 详情页及视频内流播放
│   ├── ai/                       # AI 悬浮球及聊天面板 (AiChatOverlay)
│   └── theme/                    # Compose 主题配置
└── viewmodel/                    # 状态管理层 (FeedViewModel, DetailViewModel)
```

---

## 3. 数据模型定义与接口协议

### 3.1 广告主体模型 `AdItem` (修改后)
```kotlin
package com.example.adfeed.data.model

data class AdItem(
    val id: String,
    val type: AdType,
    val title: String,
    val description: String,
    val imageUrl: String,
    val videoUrl: String? = null,
    val channel: String,
    val likeCount: Int = 0,
    val isLiked: Boolean = false,
    val isCollected: Boolean = false,
    val exposureCount: Int = 0,
    val clickCount: Int = 0,
    val summary: String? = null,
    val tags: List<String> = emptyList(),
    val aiInfo: AiInfo? = null               // AI 增强属性
)

enum class AdType {
    LARGE_IMAGE,
    SMALL_IMAGE,
    VIDEO
}
```

### 3.2 广告 AI 分析模型 `AiInfo`
```kotlin
package com.example.adfeed.data.model

data class AiInfo(
    val summary: String,                     // 一句话简短摘要
    val features: List<String>,              // 产品核心特点
    val targetUsers: List<String>,           // 目标用户
    val recommendReasons: List<String>,      // 推荐理由
    val scenarios: List<String>              // 适用场景
)
```

---

## 4. 网络层封装与 Mock 拦截器设计

网络底座通过 OkHttp 与 Retrofit 进行重新封装，提供重试及 Mock 模拟。

### 4.1 统一网络结果包 (`NetworkResult`)
```kotlin
package com.example.adfeed.core.network

sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(val code: Int, val message: String, val throwable: Throwable? = null) : NetworkResult<Nothing>()
    data class Exception(val throwable: Throwable) : NetworkResult<Nothing>()
}
```

### 4.2 自动重试拦截器 (`RetryInterceptor`)
当遭遇网络连接不可达（`IOException`）或服务器故障（HTTP `5xx`）时，自动重试最多 2 次。
```kotlin
package com.example.adfeed.core.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class RetryInterceptor(private val maxRetry: Int = 2) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var exception: IOException? = null
        var tryCount = 0

        while (tryCount <= maxRetry) {
            try {
                response = chain.proceed(request)
                if (response.isSuccessful) return response
                if (response.code in 500..599) {
                    tryCount++
                } else {
                    return response
                }
            } catch (e: IOException) {
                exception = e
                tryCount++
            }
        }
        if (response != null) return response
        throw exception ?: IOException("网络请求多次重试失败")
    }
}
```

### 4.3 故障模拟拦截器 (`MockInterceptor`)
```kotlin
package com.example.adfeed.core.network

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class MockInterceptor(
    var isMockMode: Boolean = true,
    var simulateLatencyMs: Long = 1000L,
    var simulateFailureRate: Float = 0.05f
) : Interceptor {

    private var attemptCount = 0

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!isMockMode) {
            return chain.proceed(request)
        }

        // 模拟延时
        try {
            Thread.sleep(simulateLatencyMs)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val urlPath = request.url.encodedPath

        // 模拟随机异常 (配合 RetryInterceptor)
        attemptCount++
        if (Math.random() < simulateFailureRate && attemptCount % 2 != 0) {
            return Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(500)
                .message("Mock Internal Server Error")
                .body("".toResponseBody("text/plain".toMediaType()))
                .build()
        }

        // 路径过滤返回 Mock 数据
        return when {
            urlPath.contains("chat/completions") -> {
                val mockJson = """
                    {
                        "choices": [{
                            "message": {
                                "role": "assistant",
                                "content": "【AI产品顾问】这是一款极具性价比的潮流跑步鞋，专为学生党设计。气垫回弹科技能给您带来顶级的缓震体验，非常适合日常跑步与出街穿搭。"
                            }
                        }]
                    }
                """.trimIndent()
                createMockResponse(request, mockJson)
            }
            else -> chain.proceed(request)
        }
    }

    private fun createMockResponse(request: okhttp3.Request, json: String): Response {
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(json.toResponseBody("application/json".toMediaType()))
            .build()
    }
}
```

---

## 5. 本地 Room 数据库设计

Room 数据库用于将 HashMap 内存缓存重构为本地磁盘持久化缓存。

### 5.1 广告交互表实体 (`InteractionEntity`)
```kotlin
package com.example.adfeed.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "interactions")
data class InteractionEntity(
    @PrimaryKey val adId: String,            // 广告ID
    val isLiked: Boolean,                    // 是否已点赞
    val isCollected: Boolean,                // 是否已收藏
    val likeCountOffset: Int,                // 用户操作引起的点赞数偏移量
    val collectCountOffset: Int,             // 用户操作引起的收藏数偏移量
    val shareCount: Int = 0                  // 分享次数
)
```

### 5.2 AI 数据本地缓存实体 (`AICacheEntity`)
```kotlin
package com.example.adfeed.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_cache")
data class AICacheEntity(
    @PrimaryKey val adId: String,            // 广告ID
    val summary: String,                     // 大模型生成的摘要文本
    val introText: String,                   // 详情页生成的广告介绍
    val timestamp: Long                      // 写入缓存的时间戳
)
```

---

## 6. 媒体资源加载与缓存策略

### 6.1 Coil 图片双层缓存策略
在全局 Application 初始化时为 Coil 配置最大 20% 运行内存缓存及 100MB 磁盘缓存：
```kotlin
package com.example.adfeed.core.util

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache

object ImageLoaderProvider {
    fun create(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.20)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(100 * 1024 * 1024L)
                    .build()
            }
            .crossfade(true)
            .build()
    }
}
```

### 6.2 ExoPlayer 视频本地高速缓存 (Video Cache)
在 `:player` 中构建全局唯一的 50MB 磁盘 LRU 缓存，通过 `CacheDataSource` 拦截并复用视频数据块：
```kotlin
package com.example.adfeed.core.util

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

@UnstableApi
object VideoCacheManager {
    private var cache: SimpleCache? = null

    @Synchronized
    fun getCache(context: Context): SimpleCache {
        if (cache == null) {
            val cacheDir = File(context.cacheDir, "video_cache")
            val evictor = LeastRecentlyUsedCacheEvictor(50 * 1024 * 1024L)
            val databaseProvider = StandaloneDatabaseProvider(context)
            cache = SimpleCache(cacheDir, evictor, databaseProvider)
        }
        return cache!!
    }
}
```

---

## 7. AI 增强接口重构与优雅降级机制

对 [QwenApi.kt](file:///Users/Zhuanz/adFeed/app/src/main/java/com/example/adfeed/data/remote/QwenApi.kt) 进行重构，将其纳入封装后的 OkHttp/Retrofit 中，并实现统一的超时与网络异常捕捉。当网络异常重试失效时，系统会优雅降级：
* **AI 摘要/介绍生成降级**：若接口超时或请求失败，自动提取原始 description 的前 50 字作为摘要兜底展示，打字机动画能够正常展示兜底文字。
* **AI 产品顾问聊天降级**：若聊天网络断开且重试失败，AI 悬浮球面板显示：“【系统消息】网络连接超时，请检查您的网络连接或稍后重试”，不发生闪退。

---

## 8. 落地实施与重构步骤

1. **库依赖引入**：
   在 `app/build.gradle.kts` 中配置 Room 数据库依赖、Moshi 依赖以及 OkHttp / Retrofit 依赖。
2. **底层网络封装与 Mock 拦截器实现**：
   在 `com.example.adfeed.core.network` 下搭建 `RetryInterceptor` 和 `MockInterceptor`，初始化全局 `Retrofit`。
3. **Room 本地持久化重构**：
   * 在 `com.example.adfeed.data.local` 中创建数据库类、DAO 接口和 Entity。
   * 修改 `FeedViewModel` 和 `DetailViewModel`，移除 localStates 和 cache 对应的 HashMap，替换为 Room 数据库的 Flow 响应式观察与写入。
4. **媒体资源缓存对接**：
   * 配置全局 Application 以使用 `ImageLoaderProvider` 图片缓存。
   * 对 ExoPlayer 配置 `CacheDataSource` 数据源工厂以拦截视频缓冲。
5. **网络层 QwenApi 迁移**：
   * 将 [QwenApi.kt](file:///Users/Zhuanz/adFeed/app/src/main/java/com/example/adfeed/data/remote/QwenApi.kt) 中原先的 `HttpURLConnection` 代码重构为使用 Retrofit 代理接口，并将 API_KEY 与 Mock 状态对接到全局 AppContainer 中。
