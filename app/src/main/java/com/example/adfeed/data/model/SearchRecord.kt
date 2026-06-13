package com.example.adfeed.data.model

data class SearchRecord(
    val query: String,
    val reply: String,
    val ads: List<AdItem>
)