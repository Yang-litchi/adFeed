# 项目整体任务与后续待办清单 (Project Roadmap & Todo List)

根据项目的参考开发任务以及当前基于 Room 和内置 Kotlin 的重构进度，我们将项目的完整生命周期任务进行对齐与梳理。以下为已完成任务与待办/进行中任务的详细清单。

---

## 一、 项目整体任务进度看板

| 参考任务分类 | 子任务/实现详情 | 状态 | 当前分支进度/文件 |
| :--- | :--- | :---: | :--- |
| **1. 项目基础架构搭建** | 建立 MVVM 结构，分包开发组件，引入 Room 本地持久化与响应式 Flow 数据源，确立 Single Source of Truth (SSOT) 数据流动机制。 | **已完成** | [AdApplication.kt](file:///Users/Zhuanz/adFeed/app/src/main/java/com/example/adfeed/AdApplication.kt)<br>[AppDatabase.kt](file:///Users/Zhuanz/adFeed/app/src/main/java/com/example/adfeed/data/local/db/AppDatabase.kt) |
| **2. 数据模型与协议文档** | 定义核心广告模型 `AdItem`、AI 数据模型 `AiInfo`、Room 本地实体类以及大模型网络协议设计。 | **已完成** | [dataLayer-aiIntegration-design.md](file:///Users/Zhuanz/adFeed/docs/dataLayer-aiIntegration-design.md)<br>[data-driven-architecture.md](file:///Users/Zhuanz/adFeed/docs/room/data-driven-architecture.md) |
| **3. 广告数据 Mock** | 设计本地 `MockData`，定义广告 JSON 数据源，并在 ViewModel 中支持无缝分页拉取（Paging & Pagination）。 | **已完成** | [MockData.kt](file:///Users/Zhuanz/adFeed/app/src/main/java/com/example/adfeed/data/repository/MockData.kt)<br>[FeedViewModel.kt](file:///Users/Zhuanz/adFeed/app/src/main/java/com/example/adfeed/viewmodel/FeedViewModel.kt) |
| **4. 网络层封装** | 封装 OkHttp & Retrofit 统一客户端，支持超时配置、统一结果类型处理、错误码解析、自动重试与 Mock/Real 双向切换。 | **待办** | 计划目录：`core/network` |
| **5. AI 摘要生成接口对接** | 重构大模型云端 API 网络请求（将 `HttpURLConnection` 迁移至 Retrofit 服务），建立详情页二级磁盘缓存与异常优雅降级。 | **部分完成** | [DetailViewModel.kt](file:///Users/Zhuanz/adFeed/app/src/main/java/com/example/adfeed/viewmodel/DetailViewModel.kt)（已支持缓存读写与降级，底层网络请求待重构） |
| **6. AI 标签分类接口对接** | 实现 AI 智能标签分类面板，对接大模型分类接口，支持悬浮窗实时对话及故障容灾显示。 | **待办** | 计划目录：`ui/ai` 与 `viewmodel` |
| **7. 图片/视频缓存策略** | Coil 运行内存 20% + 磁盘 100MB 缓存配置；ExoPlayer 分块 50MB 磁盘 LRU 视频播放缓存管理器开发。 | **待办** | 计划目录：`core/util` |

---

## 二、 待办/进行中任务详细开发指南

### 1. 网络层封装 (超时/重试/错误码/Mock切换)
* **技术说明**：在 `com.example.adfeed.core.network` 包中实现，解除裸写网络连接的不稳定性。
  - [ ] **统一请求结果包**：实现 `NetworkResult<T>` 密封类，分离 `Success`、`Error`（解析错误码与信息）与 `Exception`（物理网络故障）。
  - [ ] **重试拦截器 (`RetryInterceptor`)**：实现对超时（`SocketTimeoutException`）或服务器崩溃（HTTP `5xx`）等情况的自动 2 次重试。
  - [ ] **Mock 拦截器 (`MockInterceptor`)**：提供 Mock 开关。开启时自动拦截 Qwen API 请求并模拟延迟（如 1 秒）和随机丢包错误，方便本地离线开发。

### 2. AI 摘要生成与分类接口对接 (云端大模型 API)
* **技术说明**：重构并对接真正的云端大模型网关，替换零散的数据请求方式。
  - [ ] **QwenApi 客户端重构**：将现有的 [QwenApi.kt](file:///Users/Zhuanz/adFeed/app/src/main/java/com/example/adfeed/data/remote/QwenApi.kt) 中的 `HttpURLConnection` 阻塞代码重构为使用封装好的 OkHttp/Retrofit 进行异步挂起请求。
  - [ ] **AI 摘要/介绍服务对接**：在 `DetailViewModel` 载入数据时触发，调用 Retrofit 客户端，成功时写入 Room 的 `AICacheEntity` 磁盘高速缓存，异常时自动降级到原始 `summary` 做打字机动画展示。
  - [ ] **AI 标签分类与顾问问答对接**：重构 AI 顾问聊天面板，发送聊天事件时对接通义千问 API 聊天接口，在网络发生故障重试失效时进行友好系统提示而非闪退。

### 3. 图片/视频加载与缓存策略
* **技术说明**：减少内存泄漏与重复的网络带宽开销，保障流畅滑动的流畅度。
  - [ ] **Coil 图片缓存**：配置全局唯一的 `ImageLoader`。将内存缓存限制在可用内存的 20%，并在缓存目录下分配 100MB 的图片专属磁盘空间。
  - [ ] **ExoPlayer 视频本地分块缓存**：建立 `VideoCacheManager` 单例，管理 50MB 磁盘空间的 `SimpleCache` 目录，封装 `CacheDataSource.Factory` 并与 Compose 视频播放组件绑定，实现滑动即开的无缝缓存加载。

### 4. 仓储模式解耦优化 (追加任务)
* **技术说明**：解耦 ViewModel 对 Room 数据库 DAO 和 Network API 的直接引用，符合依赖倒置原则。
  - [ ] **定义 Repository 接口**：建立 `AdRepository` 和 `InteractionRepository`。
  - [ ] **实现数据仓储代理**：在 `AdRepositoryImpl` 中统一封装 Room 数据查询逻辑和 API 大模型网关。
  - [ ] **重构 ViewModel 引用**：将 ViewModel 对数据库的依赖全部重构为依赖抽象的 Repository。
