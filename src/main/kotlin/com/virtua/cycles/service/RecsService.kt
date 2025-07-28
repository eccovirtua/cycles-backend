package com.virtua.cycles.service

import com.virtua.cycles.dto.RecommendRequest
import com.virtua.cycles.dto.RecommendResponse
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

@Service
class RecsService {
    private val rest = RestTemplate()
    private val apiUrl = "http://localhost:8000/recommend"

    fun getRecommendations(itemId: Int, topN: Int = 5): RecommendResponse {
        val req = RecommendRequest(item_id = itemId, top_n = topN)
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val entity = HttpEntity(req, headers)
        return rest.postForObject(apiUrl, entity, RecommendResponse::class.java)
            ?: throw RuntimeException("No response from recommender")
    }
}
