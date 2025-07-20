package com.virtua.cycles.dto

data class RegisterRequest(
    val name: String,
    val email: String,
    val age: Int?,
    val password: String,
)