package com.virtua.cycles.controller

import com.virtua.cycles.dto.RecommendResponse
import com.virtua.cycles.service.RecsService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.slf4j.LoggerFactory

@RestController
@RequestMapping("/api")
class RecsController(private val recsService: RecsService) {
    private val logger = LoggerFactory.getLogger(RecsController::class.java)
    @GetMapping("/recommend/{itemId}")
    fun recommend(
        @PathVariable itemId: Int,
        @RequestParam(defaultValue = "5") topN: Int
    ): RecommendResponse {
        return try {
            recsService.getRecommendations(itemId, topN)

        } catch (ex: Exception) {
            logger.error("Error calling recommender service", ex)
            // Devuelve 502 Bad Gateway con mensaje
            throw ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Error al obtener recomendaciones: ${ex.message}"
            )
        }
    }
}



