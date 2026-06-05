# adFeed

Android 广告流（Feed）展示应用，使用 Jetpack Compose 开发。

## 项目功能

当前已实现：

* Feed 流页面展示
* 大图广告卡片
* 小图广告卡片
* 视频广告卡片
* 点赞功能
* 收藏功能
* 标签展示
* 广告详情页
* Mock 数据展示

---

## 项目结构

### data

#### data/model

`AdIterm.kt`

广告数据模型，定义广告对象的数据结构。

#### data/repository

`Mockdata.kt`

模拟广告数据源，用于开发和测试。

---

### ui

#### ui/feed

Feed 页面相关组件。

* `FeedScreen.kt`

    * 信息流主页面

* `LargeImageCard.kt`

    * 大图广告卡片

* `SmallImageCard.kt`

    * 小图广告卡片

* `VideoCard.kt`

    * 视频广告卡片

#### ui/detail

* `DetailScreen.kt`

    * 广告详情页面

#### ui/components

通用组件。

* `LikeButton.kt`

    * 点赞按钮

* `TagChip.kt`

    * 标签组件

#### ui/theme

Compose 主题配置(未修改)。

* `Color.kt`

    * 颜色定义

* `Theme.kt`

    * Material Theme 配置

* `Type.kt`

    * 字体配置

---

### viewmodel

#### FeedViewModel.kt

负责：

* 广告列表状态管理
* 点赞逻辑
* 收藏逻辑

---

### MainActivity.kt

应用入口。

负责：

* 初始化 Compose
* 页面导航入口

---

## 技术栈

* Kotlin
* Jetpack Compose
* Material3
* ViewModel
* Navigation Compose
* Coil
* Media3 ExoPlayer

---

## 运行环境

* Android Studio
* JDK 11
* Min SDK 26
* Target SDK 36

---

## 当前版本

v1.0

已完成基础广告流展示功能。

---
## 下一步更新需要解决的问题：

### 点赞收藏在后刷新/切换页面后状态复原
  
* 原因：现在 refresh() 和 switchChannel() 直接重新加载MockData，覆盖了本地状态。需要B在Repository层做本地状态持久化，把点赞/收藏状态存在内存Map里，加载新数据时merge回去。

### 增加Tag点击筛选，卡片上的Tag也能点击触发筛选

* 需要修改多个布局