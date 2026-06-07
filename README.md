# adFeed

Android 广告信息流（Feed）应用，基于 Jetpack Compose 开发，模拟现代内容推荐平台的信息流浏览体验。

## 项目功能

### 信息流浏览

* 精选频道
* 电商频道
* 本地频道
* 左右滑动切换频道
* 下拉刷新
* 分页加载
* 大图广告卡片
* 小图广告卡片
* 视频广告卡片

### Tag智能筛选

* 高频Tag推荐
* 多Tag组合筛选
* 自定义Tag输入
* 卡片Tag快捷筛选
* 一键清空筛选

### AI增强能力

#### AI摘要

广告卡片展示AI生成摘要：

* 商品简介
* 核心卖点提炼

#### AI广告介绍

详情页自动生成广告介绍：

* 基于商品结构化信息生成
* 打字机动画展示
* 本地缓存避免重复请求

#### AI产品顾问

支持多轮问答：

* 商品特点咨询
* 适用人群分析
* 使用场景推荐
* 推荐理由解释

#### AI智能推荐

主页悬浮AI助手：

* 自然语言搜索
* AI推荐广告
* 搜索历史记录
* 推荐结果卡片展示

### 用户交互

* 点赞功能
* 收藏功能
* 状态持久化
* 详情页跳转
* 推荐广告跳转

### 动效体验

* 启动页动画
* AI悬浮球脉冲动画
* Tag筛选展开动画
* AI搜索面板展开动画
* AI介绍打字机动画

---

## 项目架构

采用 MVVM 架构设计。

Data Layer

* AdItem
* AiInfo
* MockData
* QwenApi

ViewModel Layer

* FeedViewModel
* DetailViewModel

UI Layer

* FeedScreen
* DetailScreen
* LargeImageCard
* SmallImageCard
* VideoCard
* AiChatOverlay
* SplashScreen

---

## 数据模型

### AdItem

广告主体数据。

### AiInfo

广告AI分析数据。

包含：

* summary
* features
* targetUsers
* recommendReasons
* scenarios

用于：

* AI摘要展示
* AI介绍生成
* AI顾问问答

---

## 技术栈

* Kotlin
* Jetpack Compose
* Material3
* Navigation Compose
* ViewModel
* StateFlow
* Coroutines
* Coil
* Media3 ExoPlayer
* Qwen API
* HttpURLConnection

---

## 版本记录
### v1.0

* 基础信息流展示
* 点赞功能
* 收藏功能
* 视频广告卡片
* 广告详情页

### v1.1

新增：

* Tag筛选系统
* 多Tag组合筛选
* 自定义Tag输入
* 筛选面板悬浮动画
* AI聊天页面框架
* 启动页动画
* 左右滑动切换频道

优化：

* 下拉刷新逻辑
* Mock数据随机化展示
* TopBar布局重构

修复：

* 首次启动列表为空的问题

### v1.2

新增：

* AiInfo结构化数据模型
* AI广告摘要展示
* AI广告介绍生成
* AI介绍打字机动画
* AI产品顾问问答
* AI搜索推荐助手
* AI搜索历史记录
* AI悬浮球入口

优化：

* TabBar重构
* Pager切换逻辑优化
* 推荐广告跳转逻辑

修复：

* 推荐广告详情页找不到数据的问题
* AI入口与频道切换冲突问题

---

## 后续规划

* AI推荐排序优化
* 云端点赞收藏同步
* 本地数据库缓存
