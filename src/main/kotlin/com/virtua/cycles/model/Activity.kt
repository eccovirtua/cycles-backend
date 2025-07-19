package com.virtua.cycles.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

class Activity {
    @Document(collection = "activities")
    data class Activity(
        @Id val id: String? = null,
        val userId: String,
        val contentType: String,
        val contentTitle: String,
        val timestamp: Long
    )
}