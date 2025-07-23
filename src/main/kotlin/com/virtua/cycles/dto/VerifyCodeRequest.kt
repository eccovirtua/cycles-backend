package com.virtua.cycles.dto

data class VerifyCodeRequest(
    val email: String,
    val code: String
)