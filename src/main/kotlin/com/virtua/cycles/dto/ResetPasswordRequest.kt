package com.virtua.cycles.dto

data class ResetPasswordRequest(
    val email: String,
    val code: String,
    val newPassword: String
)