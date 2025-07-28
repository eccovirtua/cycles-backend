package com.virtua.cycles.dto

data class RecommendRequest(
    val item_id: Int,
    val top_n: Int = 5
)
