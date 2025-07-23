package com.virtua.cycles.repository

import com.virtua.cycles.model.PasswordResetToken
import org.springframework.data.mongodb.repository.MongoRepository

interface PasswordResetTokenRepository : MongoRepository<PasswordResetToken, String> {
    fun findByEmail(email: String): PasswordResetToken?
    fun deleteByEmail(email: String)
}