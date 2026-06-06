# adFeed

Android 广告信息流（Feed）应用，基于 Jetpack Compose 开发，模拟现代内容推荐平台的信息流浏览体验。

## 项目功能

### 信息流浏览

* 精选频道
* 电商频道
* 本地频道
* 下拉刷新
* 分页加载
* 大图广告卡片
* 小图广告卡片
* 视频广告卡片

### Tag智能筛选

* 顶部筛选面板
* 频道内高频Tag推荐
* 多Tag组合筛选
* 卡片Tag点击快速筛选
* 自定义Tag输入
* 一键清空筛选条件

### 用户交互

* 点赞功能
* 收藏功能
* 点赞数实时更新
* 收藏状态切换
* 状态本地持久化

### AI入口

* 顶部AI助手入口
* AI聊天页面框架
* 左滑快速进入AI页面

### 启动体验

* 动态启动页动画
* Logo弹跳动画
* 标题渐显动画
* 页面淡出跳转

---

## 项目架构

采用 MVVM 架构设计。

```text
UI Layer
│
├── FeedScreen
├── DetailScreen
├── AiChatScreen
│
ViewModel Layer
│
└── FeedViewModel
│
Data Layer
│
├── MockData
└── AdItem
```

---

## 项目结构

### data

#### model

* AdItem.kt

广告数据模型。

#### repository

* MockData.kt

模拟广告数据源。

---

### ui

#### feed

* FeedScreen.kt

  * 信息流主页
  * Tab切换
  * 筛选面板
  * 分页与刷新

* LargeImageCard.kt

  * 大图广告卡片

* SmallImageCard.kt

  * 小图广告卡片

* VideoCard.kt

  * 视频广告卡片

#### detail

* DetailScreen.kt

  * 广告详情页

#### ai

* AiChatScreen.kt

  * AI聊天页面框架

#### components

* LikeButton.kt
* TagChip.kt

公共组件库。

#### theme

* Color.kt
* Theme.kt
* Type.kt

Compose主题配置。

---

### viewmodel

#### FeedViewModel.kt

负责：

* 数据加载
* 频道切换
* Tag筛选
* 点赞逻辑
* 收藏逻辑
* 分页逻辑
* 本地状态维护

---

## 技术栈

* Kotlin
* Jetpack Compose
* Material3
* ViewModel
* Navigation Compose
* Coil
* Media3 ExoPlayer
* Coroutines

---

## 运行环境

* Android Studio
* JDK 11
* Min SDK 26
* Target SDK 36

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
* 自定义Tag
* 筛选面板动画
* AI聊天页面框架
* 启动页动画
* 左右滑动切换频道

优化：

* 下拉刷新逻辑
* 数据随机化展示
* 点赞状态持久化
* 收藏状态持久化
* TopBar布局重构

修复：

* 首次启动列表为空的问题

---

## 后续规划

* AI摘要生成
* AI对话功能
* 网络数据接口接入
* 用户登录系统
* 云端点赞收藏同步
* 推荐算法优化
