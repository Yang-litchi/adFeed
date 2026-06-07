package com.example.adfeed.data.model

data class AiInfo(

    // AI一句话摘要
    val summary: String,

    // 产品核心特点
    val features: List<String>,

    // 目标用户
    val targetUsers: List<String>,

    // 推荐理由
    val recommendReasons: List<String>,

    // 使用场景
    val scenarios: List<String>
)