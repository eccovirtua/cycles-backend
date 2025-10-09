package com.virtua.cycles.controller

import com.virtua.cycles.model.User
import com.virtua.cycles.repository.UserRepository
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.util.UUID

@RestController
@RequestMapping("/users")
class UserController(private val userRepository: UserRepository) {


    // Ruta donde se guardarán las imágenes. Asegúrate de que esta carpeta exista.
    private val uploadDir = Paths.get("data", "profile_photos")

    /**
     * Helper para obtener el ID del usuario autenticado (asumiendo que estás usando JWT/UserDetails).
     */
    private fun getAuthenticatedUserId(): String {
        val authentication: Authentication = SecurityContextHolder.getContext().authentication
        // Asumiendo que el 'principal' es el objeto User o tiene el ID como String
        return (authentication.principal as User).id ?: throw IllegalStateException("User ID not found in security context")
    }

    // 🎯 NUEVO ENDPOINT: Sube la foto de perfil y actualiza la URL en MongoDB.
    @PostMapping("/profile/photo")
    fun uploadProfilePhoto(
        @RequestParam("photo") file: MultipartFile // El nombre del campo DEBE ser 'photo'
    ): ResponseEntity<User> {
        // Asegura que solo el usuario autenticado pueda subir su foto
        val userId = getAuthenticatedUserId()
        val userOptional = userRepository.findById(userId)

        if (!userOptional.isPresent) {
            return ResponseEntity.notFound().build()
        }
        val user = userOptional.get()

        // 1. Crear el directorio si no existe
        if (Files.notExists(uploadDir)) {
            Files.createDirectories(uploadDir)
        }

        // 2. Generar nombre único y guardar archivo
        val extension = file.originalFilename?.substringAfterLast('.', "") ?: "jpg"
        val uniqueFileName = "${userId}_${UUID.randomUUID()}.$extension"
        val targetPath = uploadDir.resolve(uniqueFileName)

        try {
            file.transferTo(targetPath.toFile())
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }

        // 3. Crear la URL pública para la app móvil
        // Corresponde al mapeo en WebConfig
        val newPublicUrl = "/public/images/$uniqueFileName"

        // 4. Actualizar el modelo y guardar en MongoDB
        user.profileImageUrl = newPublicUrl
        user.updatedAt = LocalDateTime.now() // Opcional: actualizar timestamp
        val updatedUser = userRepository.save(user)

        // 5. Retorna el objeto completo de usuario
        return ResponseEntity.ok(updatedUser)
    }
    // Crear usuario
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    fun createUser(@Valid @RequestBody user: User): ResponseEntity<User> {
        val savedUser = userRepository.save(user)
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser)
    }

    // Listar todos los usuarios
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    fun getAllUsers(): ResponseEntity<List<User>> {
        val users = userRepository.findAll()
        return ResponseEntity.ok(users)
    }

    // Obtener un usuario por ID
    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: String): ResponseEntity<User> {
        val user = userRepository.findById(id)
        return if (user.isPresent) {
            ResponseEntity.ok(user.get())
        } else {
            ResponseEntity.notFound().build()
        }
    }
}

