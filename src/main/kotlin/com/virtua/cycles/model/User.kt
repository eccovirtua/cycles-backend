//clase user. esta clase maneja la estructura principal de un usuario de la app.
package com.virtua.cycles.model

import jakarta.validation.constraints.NotBlank
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "users")
data class User(
    @Id val id: String? = null,

    @field:NotBlank(message = "User name cannot be blank") //validaciones de campo no rellenado
    val name: String,

    @field:NotBlank(message = "el correo no puede estar vacío")
    val email: String,

    val age: Int? = null,

    @field:NotBlank(message = "la contraseña es obligatoria")
    val password: String,

    val cratedAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)