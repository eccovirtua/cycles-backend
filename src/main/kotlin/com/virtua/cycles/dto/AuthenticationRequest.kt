package com.virtua.cycles.dto

data class AuthenticationRequest(
    val usernameOrEmail: String,
    val password: String
)

