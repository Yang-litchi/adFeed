package com.example.adfeed.data.exposure

/**
 * 会话级曝光状态追踪器（object 单例）
 *
 * ## 职责
 * - 维护当前会话中已完成有效曝光的广告ID集合
 * - 同一广告在单次会话（App启动→关闭）内仅允许记录1次有效曝光
 * - 不负责可见性判定、计时等逻辑（交由UI层 ExposureDetector 处理）
 *
 * ## 设计原则
 * - **独立模块**：与UI层（Compose组件）、数据层（Repository）完全解耦
 * - **单例**：全局唯一，确保跨页面、跨ViewModel的曝光状态一致
 * - **会话级**：App进程存活期间有效，进程被杀后自动重置
 *
 * ## 使用方式
 * ```kotlin
 * if (!ExposureTracker.isExposed(adId)) {
 *     // 满足可见性+计时条件后
 *     ExposureTracker.markExposed(adId)
 *     recordExposure(adId)
 * }
 * ```
 */
object ExposureTracker {

    /** 当前会话已完成有效曝光的广告ID集合 */
    private val exposedAdIds = mutableSetOf<String>()

    /** 只读视图，供外部查询 */
    val exposedIds: Set<String> get() = exposedAdIds.toSet()

    /**
     * 查询某广告在当前会话是否已完成有效曝光
     *
     * @param adId 广告ID
     * @return true 表示已曝光，不应再次记录
     */
    fun isExposed(adId: String): Boolean = adId in exposedAdIds

    /**
     * 将某广告标记为已曝光
     * 调用方应确保满足所有曝光判定条件后再调用此方法
     */
    fun markExposed(adId: String) {
        exposedAdIds.add(adId)
    }

    /** 清空所有曝光记录（仅用于测试或手动重置会话） */
    fun reset() {
        exposedAdIds.clear()
    }

    /** 当前会话已曝光广告数量（调试用） */
    fun size(): Int = exposedAdIds.size
}
