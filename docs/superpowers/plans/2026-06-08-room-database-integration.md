# Room 本地数据库状态持久化与缓存集成计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 集成本地 Room 数据库，实现用户点赞/收藏状态的跨进程持久化，并为 AI 生成的广告介绍建立高速本地缓存，淘汰现有的内存 HashMap 方案。

**Architecture:** 采用唯一事实源 (Single Source of Truth) 的设计理念。使用 Room 的 Flow 接口让 ViewModel 实现对数据库变动的响应式订阅，通过局部状态偏移量 (Offset) 精确更新 Mock 点赞数，并使用协程在 IO 线程进行异步读写以防阻塞 UI。

**Tech Stack:** Room Database (Entity, DAO, RoomDatabase), Kotlin Coroutines (StateFlow, Flow, Dispatchers.IO), Jetpack Compose.

---

### Task 1: 引入 Room 依赖与 kapt 插件

**Files:**
- Modify: [app/build.gradle.kts](file:///Users/Zhuanz/adFeed/app/build.gradle.kts)

- [ ] **Step 1: 添加 kapt 插件与 Room 依赖库**
  
  在 [app/build.gradle.kts](file:///Users/Zhuanz/adFeed/app/build.gradle.kts) 的 `plugins` 块及 `dependencies` 块中添加如下代码：
  
  ```kotlin
  // 在 plugins 块中追加：
  plugins {
      alias(libs.plugins.android.application)
      alias(libs.plugins.kotlin.compose)
      id("kotlin-kapt") // 引入 kapt 用于处理 Room 注解
  }
  
  // 在 dependencies 块中追加：
  dependencies {
      // ... 现有依赖不变 ...
      
      // Room 数据库依赖
      val roomVersion = "2.6.1"
      implementation("androidx.room:room-runtime:$roomVersion")
      implementation("androidx.room:room-ktx:$roomVersion")
      kapt("androidx.room:room-compiler:$roomVersion")
  }
  ```

- [ ] **Step 2: 验证编译状态**
  
  运行 Gradle 编译命令以确保依赖配置正确：
  Run: `./gradlew compileDebugKotlin`
  Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 提交修改**
  
  ```bash
  git add app/build.gradle.kts
  git commit -m "build: add Room database dependencies and kapt plugin"
  ```

---

### Task 2: 定义 Room 实体数据表 (Entities)

**Files:**
- Create: `app/src/main/java/com/example/adfeed/data/local/entity/InteractionEntity.kt`
- Create: `app/src/main/java/com/example/adfeed/data/local/entity/AICacheEntity.kt`

- [ ] **Step 1: 创建 InteractionEntity 实体类**
  
  新建 [InteractionEntity.kt](file:///Users/Zhuanz/adFeed/app/src/main/java/com/example/adfeed/data/local/entity/InteractionEntity.kt) 文件，定义如下数据表结构以记录点赞收藏偏移：
  
  ```kotlin
  package com.example.adfeed.data.local.entity
  
  import androidx.room.Entity
  import androidx.room.PrimaryKey
  
  /**
   * 用户点赞/收藏交互持久化实体
   */
  @Entity(tableName = "interactions")
  data class InteractionEntity(
      @PrimaryKey val adId: String,            // 广告ID
      val isLiked: Boolean,                    // 是否已点赞
      val isCollected: Boolean,                // 是否已收藏
      val likeCountOffset: Int,                // 用户操作引起的点赞偏移量 (例如 +1, -1)
      val collectCountOffset: Int,             // 用户操作引起的收藏偏移量
      val shareCount: Int = 0                  // 分享统计数
  )
  ```

- [ ] **Step 2: 创建 AICacheEntity 实体类**
  
  新建 [AICacheEntity.kt](file:///Users/Zhuanz/adFeed/app/src/main/java/com/example/adfeed/data/local/entity/AICacheEntity.kt) 文件，定义 AI 大模型结果本地缓存：
  
  ```kotlin
  package com.example.adfeed.data.local.entity
  
  import androidx.room.Entity
  import androidx.room.PrimaryKey
  
  /**
   * AI 生成内容本地高速磁盘缓存实体
   */
  @Entity(tableName = "ai_cache")
  data class AICacheEntity(
      @PrimaryKey val adId: String,            // 广告ID
      val summary: String,                     // 缓存的摘要信息
      val introText: String,                   // 缓存的广告介绍大文本
      val timestamp: Long                      // 缓存写入时间戳
  )
  ```

- [ ] **Step 3: 提交修改**
  
  ```bash
  git add app/src/main/java/com/example/adfeed/data/local/entity/
  git commit -m "feat: define Room database entities for interaction and AI cache"
  ```

---

### Task 3: 创建数据库访问接口 (DAOs)

**Files:**
- Create: `app/src/main/java/com/example/adfeed/data/local/dao/InteractionDao.kt`
- Create: `app/src/main/java/com/example/adfeed/data/local/dao/AICacheDao.kt`

- [ ] **Step 1: 创建 InteractionDao 访问接口**
  
  新建 [InteractionDao.kt](file:///Users/Zhuanz/adFeed/app/src/main/java/com/example/adfeed/data/local/dao/InteractionDao.kt) 文件，定义数据操作契约（使用 Flow 进行响应式观察）：
  
  ```kotlin
  package com.example.adfeed.data.local.dao
  
  import androidx.room.Dao
  import androidx.room.Insert
  import androidx.room.OnConflictStrategy
  import androidx.room.Query
  import com.example.adfeed.data.local.entity.InteractionEntity
  import kotlinx.coroutines.flow.Flow
  
  @Dao
  interface InteractionDao {
      @Query("SELECT * FROM interactions WHERE adId = :adId")
      suspend fun getInteraction(adId: String): InteractionEntity?
  
      @Query("SELECT * FROM interactions")
      fun getAllInteractionsFlow(): Flow<List<InteractionEntity>>
  
      @Query("SELECT * FROM interactions WHERE adId = :adId")
      fun getInteractionFlow(adId: String): Flow<InteractionEntity?>
  
      @Insert(onConflict = OnConflictStrategy.REPLACE)
      suspend fun insertOrUpdate(interaction: InteractionEntity)
  }
  ```

- [ ] **Step 2: 创建 AICacheDao 访问接口**
  
  新建 [AICacheDao.kt](file:///Users/Zhuanz/adFeed/app/src/main/java/com/example/adfeed/data/local/dao/AICacheDao.kt) 文件，定义大模型缓存读取：
  
  ```kotlin
  package com.example.adfeed.data.local.dao
  
  import androidx.room.Dao
  import androidx.room.Insert
  import androidx.room.OnConflictStrategy
  import androidx.room.Query
  import com.example.adfeed.data.local.entity.AICacheEntity
  
  @Dao
  interface AICacheDao {
      @Query("SELECT * FROM ai_cache WHERE adId = :adId")
      suspend fun getAICache(adId: String): AICacheEntity?
  
      @Insert(onConflict = OnConflictStrategy.REPLACE)
      suspend fun insertCache(cache: AICacheEntity)
  
      @Query("DELETE FROM ai_cache WHERE adId = :adId")
      suspend fun deleteCache(adId: String)
  }
  ```

- [ ] **Step 3: 提交修改**
  
  ```bash
  git add app/src/main/java/com/example/adfeed/data/local/dao/
  git commit -m "feat: create DAO interfaces for Room database"
  ```

---

### Task 4: 声明 AppDatabase 与初始化 Application

**Files:**
- Create: `app/src/main/java/com/example/adfeed/data/local/db/AppDatabase.kt`
- Create: `app/src/main/java/com/example/adfeed/AdApplication.kt`
- Modify: [app/src/main/AndroidManifest.xml](file:///Users/Zhuanz/adFeed/app/src/main/AndroidManifest.xml)

- [ ] **Step 1: 创建 AppDatabase 抽象数据库类**
  
  新建 [AppDatabase.kt](file:///Users/Zhuanz/adFeed/app/src/main/java/com/example/adfeed/data/local/db/AppDatabase.kt) 文件：
  
  ```kotlin
  package com.example.adfeed.data.local.db
  
  import androidx.room.Database
  import androidx.room.RoomDatabase
  import com.example.adfeed.data.local.dao.AICacheDao
  import com.example.adfeed.data.local.dao.InteractionDao
  import com.example.adfeed.data.local.entity.AICacheEntity
  import com.example.adfeed.data.local.entity.InteractionEntity
  
  @Database(
      entities = [InteractionEntity::class, AICacheEntity::class],
      version = 1,
      exportSchema = false
  )
  abstract class AppDatabase : RoomDatabase() {
      abstract fun interactionDao(): InteractionDao
      abstract fun aiCacheDao(): AICacheDao
  }
  ```

- [ ] **Step 2: 创建 AdApplication 全局上下文入口**
  
  新建 [AdApplication.kt](file:///Users/Zhuanz/adFeed/app/src/main/java/com/example/adfeed/AdApplication.kt) 类，用于在 App 启动时全局单例化构建 Room 数据库：
  
  ```kotlin
  package com.example.adfeed
  
  import android.app.Application
  import androidx.room.Room
  import com.example.adfeed.data.local.db.AppDatabase
  
  class AdApplication : Application() {
      companion object {
          lateinit var database: AppDatabase
              private set
      }
  
      override fun onCreate() {
          super.onCreate()
          database = Room.databaseBuilder(
              applicationContext,
              AppDatabase::class.java,
              "adfeed.db"
          )
          .fallbackToDestructiveMigration() // 允许升级时清除旧数据重新创建
          .build()
      }
  }
  ```

- [ ] **Step 3: 在清单文件中声明 AdApplication**
  
  修改 [AndroidManifest.xml:5](file:///Users/Zhuanz/adFeed/app/src/main/AndroidManifest.xml#L5)，添加 `android:name` 属性：
  
  ```xml
      <application
          android:name=".AdApplication"
          android:allowBackup="true"
          android:dataExtractionRules="@xml/data_extraction_rules"
  ```

- [ ] **Step 4: 验证编译状态**
  
  执行编译以生成 Room 实现辅助类：
  Run: `./gradlew compileDebugKotlin`
  Expected: BUILD SUCCESSFUL

- [ ] **Step 5: 提交修改**
  
  ```bash
  git add app/src/main/java/com/example/adfeed/data/local/db/
  git add app/src/main/java/com/example/adfeed/AdApplication.kt
  git add app/src/main/AndroidManifest.xml
  git commit -m "feat: declare AppDatabase and register global AdApplication"
  ```

---

### Task 5: 重构 FeedViewModel 接入 Room 响应式持久化

**Files:**
- Modify: [app/src/main/java/com/example/adfeed/viewmodel/FeedViewModel.kt](file:///Users/Zhuanz/adFeed/app/src/main/java/com/example/adfeed/viewmodel/FeedViewModel.kt)

- [ ] **Step 1: 移除 HashMap 状态缓存并集成 Room 的 Flow 订阅**
  
  修改 [FeedViewModel.kt](file:///Users/Zhuanz/adFeed/app/src/main/java/com/example/adfeed/viewmodel/FeedViewModel.kt)，用数据库的 Flow 代替内存 localStates：
  
  ```kotlin
  // 替换 lines 26-33 的 localStates
  // 增加 observeDatabaseChanges() 订阅：
  ```
  
  完整修改后的 [FeedViewModel.kt](file:///Users/Zhuanz/adFeed/app/src/main/java/com/example/adfeed/viewmodel/FeedViewModel.kt) 相关部分如下：
  
  ```kotlin
  package com.example.adfeed.viewmodel
  
  import androidx.lifecycle.ViewModel
  import androidx.lifecycle.viewModelScope
  import com.example.adfeed.AdApplication
  import com.example.adfeed.data.local.entity.InteractionEntity
  import com.example.adfeed.data.model.AdItem
  import com.example.adfeed.data.repository.MockData
  import kotlinx.coroutines.Dispatchers
  import kotlinx.coroutines.delay
  import kotlinx.coroutines.flow.MutableStateFlow
  import kotlinx.coroutines.flow.StateFlow
  import kotlinx.coroutines.flow.asStateFlow
  import kotlinx.coroutines.flow.update
  import kotlinx.coroutines.launch
  import kotlinx.coroutines.withContext
  
  data class FeedUiState(
      val ads: List<AdItem> = emptyList(),
      val isLoading: Boolean = false,
      val hasMore: Boolean = true,
      val error: String? = null
  )
  
  class FeedViewModel : ViewModel() {
  
      private val _uiState = MutableStateFlow(FeedUiState())
      val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()
  
      private var currentChannel = "精选"
      private var currentPage = 0
      private val pageSize = 6
      private var shuffledPool: List<AdItem> = emptyList()
  
      init {
          shuffledPool = buildPool(currentChannel)
          loadAds()
          observeDatabaseChanges() // 开启响应式状态流合并订阅
      }
  
      private fun buildPool(channel: String): List<AdItem> {
          return MockData.getByChannel(channel).shuffled()
      }
  
      /**
       * 响应式监听 Room 数据库变动，自动同步到列表中
       */
      private fun observeDatabaseChanges() {
          viewModelScope.launch {
              AdApplication.database.interactionDao().getAllInteractionsFlow().collect { interactions ->
                  val interactionMap = interactions.associateBy { it.adId }
                  _uiState.update { state ->
                      state.copy(ads = state.ads.map { ad ->
                          val entity = interactionMap[ad.id]
                          if (entity != null) {
                              ad.copy(
                                  isLiked = entity.isLiked,
                                  likeCount = ad.likeCount + entity.likeCountOffset,
                                  isCollected = entity.isCollected
                              )
                          } else {
                              ad
                          }
                      })
                  }
              }
          }
      }
  
      /**
       * 加载广告时同步加载 Room 的交互状态
       */
      fun loadAds() {
          viewModelScope.launch {
              _uiState.update { it.copy(isLoading = true, error = null) }
              delay(600)
              val start = currentPage * pageSize
              val end = minOf(start + pageSize, shuffledPool.size)
              if (start >= shuffledPool.size) {
                  _uiState.update { it.copy(isLoading = false, hasMore = false) }
                  return@launch
              }
              
              // 在 IO 协程中异步获取广告的本地持久化状态
              val newAds = withContext(Dispatchers.IO) {
                  shuffledPool.subList(start, end).map { ad ->
                      val entity = AdApplication.database.interactionDao().getInteraction(ad.id)
                      if (entity != null) {
                          ad.copy(
                              isLiked = entity.isLiked,
                              likeCount = ad.likeCount + entity.likeCountOffset,
                              isCollected = entity.isCollected
                          )
                      } else ad
                  }
              }
              
              _uiState.update { state ->
                  state.copy(
                      ads = if (currentPage == 0) newAds else state.ads + newAds,
                      isLoading = false,
                      hasMore = end < shuffledPool.size
                  )
              }
              currentPage++
          }
      }
  
      fun refresh() {
          shuffledPool = buildPool(currentChannel)
          currentPage = 0
          loadAds()
      }
  
      fun loadMore() {
          if (_uiState.value.isLoading || !_uiState.value.hasMore) return
          loadAds()
      }
  
      fun switchChannel(channel: String) {
          if (currentChannel == channel) return
          currentChannel = channel
          shuffledPool = buildPool(channel)
          currentPage = 0
          loadAds()
      }
  
      /**
       * 点赞交互改写为写入 Room
       */
      fun toggleLike(adId: String) {
          viewModelScope.launch(Dispatchers.IO) {
              val dao = AdApplication.database.interactionDao()
              val entity = dao.getInteraction(adId) ?: InteractionEntity(adId, false, false, 0, 0, 0)
              val newLiked = !entity.isLiked
              val newOffset = entity.likeCountOffset + (if (newLiked) 1 else -1)
              
              dao.insertOrUpdate(entity.copy(isLiked = newLiked, likeCountOffset = newOffset))
          }
      }
  
      /**
       * 收藏交互改写为写入 Room
       */
      fun toggleCollect(adId: String) {
          viewModelScope.launch(Dispatchers.IO) {
              val dao = AdApplication.database.interactionDao()
              val entity = dao.getInteraction(adId) ?: InteractionEntity(adId, false, false, 0, 0, 0)
              val newCollected = !entity.isCollected
              
              dao.insertOrUpdate(entity.copy(isCollected = newCollected))
          }
      }
  
      fun recordClick(adId: String) {
          _uiState.update { state ->
              state.copy(ads = state.ads.map { ad ->
                  if (ad.id == adId) ad.copy(clickCount = ad.clickCount + 1) else ad
              })
          }
      }
  
      fun recordExposure(adId: String) {
          _uiState.update { state ->
              state.copy(ads = state.ads.map { ad ->
                  if (ad.id == adId) ad.copy(exposureCount = ad.exposureCount + 1) else ad
              })
          }
      }
  }
  ```

- [ ] **Step 2: 验证编译状态**
  
  Run: `./gradlew compileDebugKotlin`
  Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 提交修改**
  
  ```bash
  git add app/src/main/java/com/example/adfeed/viewmodel/FeedViewModel.kt
  git commit -m "refactor: integrate Room database Flow subscriptions in FeedViewModel"
  ```

---

### Task 6: 重构 DetailViewModel 接入 Room 二级缓存与优雅降级

**Files:**
- Modify: [app/src/main/java/com/example/adfeed/viewmodel/DetailViewModel.kt](file:///Users/Zhuanz/adFeed/app/src/main/java/com/example/adfeed/viewmodel/DetailViewModel.kt)

- [ ] **Step 1: 在 DetailViewModel 中应用 AICacheDao 与降级文案打字机流程**
  
  修改 [DetailViewModel.kt](file:///Users/Zhuanz/adFeed/app/src/main/java/com/example/adfeed/viewmodel/DetailViewModel.kt)，将其中的内存 Map 缓存替换为 Room 的缓存实体，并引入大模型接口故障时的 `summary` 降级兜底打字机逻辑：
  
  ```kotlin
  package com.example.adfeed.viewmodel
  
  import androidx.lifecycle.ViewModel
  import androidx.lifecycle.viewModelScope
  import com.example.adfeed.AdApplication
  import com.example.adfeed.data.local.entity.AICacheEntity
  import com.example.adfeed.data.model.AdItem
  import com.example.adfeed.data.remote.QwenApi
  import kotlinx.coroutines.Dispatchers
  import kotlinx.coroutines.Job
  import kotlinx.coroutines.delay
  import kotlinx.coroutines.flow.MutableStateFlow
  import kotlinx.coroutines.flow.StateFlow
  import kotlinx.coroutines.flow.asStateFlow
  import kotlinx.coroutines.launch
  import kotlinx.coroutines.withContext
  
  data class DetailUiState(
      val displayedText: String = "",   // 当前逐字显示的文字
      val isTyping: Boolean = false,    // 是否正在打字动画
      val isLoading: Boolean = false,   // 是否正在加载
      val error: String? = null
  )
  
  class DetailViewModel : ViewModel() {
  
      private val _uiState = MutableStateFlow(DetailUiState())
      val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()
  
      private var typingJob: Job? = null
  
      fun loadIntro(ad: AdItem) {
          val aiInfo = ad.aiInfo ?: return
  
          viewModelScope.launch {
              _uiState.value = DetailUiState(isLoading = true)
              
              // 1. 尝试从 Room 本地缓存加载 AI 介绍
              val cached = withContext(Dispatchers.IO) {
                  AdApplication.database.aiCacheDao().getAICache(ad.id)
              }
              
              if (cached != null) {
                  // 缓存命中：直接启动打字机动画播放已保存的介绍
                  startTypingAnimation(cached.introText)
                  return@launch
              }
  
              // 2. 缓存未命中：发起 Qwen API 调用进行生成
              QwenApi.generateAdIntro(aiInfo, ad.title).fold(
                  onSuccess = { text ->
                      // 生成成功：将结果写入本地 Room 缓存
                      withContext(Dispatchers.IO) {
                          AdApplication.database.aiCacheDao().insertCache(
                              AICacheEntity(
                                  adId = ad.id,
                                  summary = aiInfo.summary,
                                  introText = text,
                                  timestamp = System.currentTimeMillis()
                              )
                          )
                      }
                      startTypingAnimation(text)
                  },
                  onFailure = {
                      // 生成失败 (如网络连接超时/出错)：优雅降级，使用 aiInfo.summary 作为打字机内容
                      val fallbackText = aiInfo.summary
                      startTypingAnimation(fallbackText)
                  }
              )
          }
      }
  
      private fun startTypingAnimation(fullText: String) {
          typingJob?.cancel()
          typingJob = viewModelScope.launch {
              _uiState.value = DetailUiState(isTyping = true, displayedText = "")
              val sb = StringBuilder()
              for (char in fullText) {
                  sb.append(char)
                  _uiState.value = DetailUiState(
                      isTyping = true,
                      displayedText = sb.toString()
                  )
                  delay(30L)
              }
              _uiState.value = DetailUiState(
                  isTyping = false,
                  displayedText = fullText
              )
          }
      }
  
      override fun onCleared() {
          super.onCleared()
          typingJob?.cancel()
      }
  }
  ```

- [ ] **Step 2: 验证全局编译与执行**
  
  Run: `./gradlew assembleDebug`
  Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 提交修改**
  
  ```bash
  git add app/src/main/java/com/example/adfeed/viewmodel/DetailViewModel.kt
  git commit -m "refactor: integrate Room database caching and summary downgrade strategy in DetailViewModel"
  ```
