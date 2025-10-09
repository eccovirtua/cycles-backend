package com.virtua.cycles.controller

import com.virtua.cycles.dto.PhotoData
import com.virtua.cycles.model.User
import com.virtua.cycles.repository.UserRepository
import jakarta.validation.Valid
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.gridfs.GridFsTemplate
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.time.LocalDateTime

@RestController
@RequestMapping("/users")
class UserController(
    private val userRepository: UserRepository,
    private val gridFsTemplate: GridFsTemplate
) {

    private fun getAuthenticatedUserId(): String {
        val authentication: Authentication = SecurityContextHolder.getContext().authentication
        return (authentication.principal as User).id ?: throw IllegalStateException("User ID not found in security context")
    }
    // ----------------------------------------------------
    // 🎯 LÓGICA DE SUBIDA (GRIDFS IMPLEMENTADO)
    // ----------------------------------------------------

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/profile/photo")
    fun uploadProfilePhoto(
        @RequestParam("photo") file: MultipartFile
    ): ResponseEntity<PhotoData> {

        val userId = getAuthenticatedUserId()
        val userOptional = userRepository.findById(userId)

        if (!userOptional.isPresent) {
            return ResponseEntity.notFound().build()
        }
        val user = userOptional.get()

        // 1. Validar el archivo
        if (file.isEmpty || file.contentType.isNullOrEmpty()) {
            return ResponseEntity.badRequest().build()
        }

        try {
            // 2. Almacenar en GridFS
            val fileId = gridFsTemplate.store(
                file.inputStream,                   // El stream del archivo
                file.originalFilename,              // Nombre del archivo
                file.contentType                    // Tipo de contenido (ej: image/jpeg)
            ).toString() // Obtiene el ID del archivo de GridFS

            val downloadUrl = "/users/files/photo/$fileId"

            // 4. Limpiar fotos antiguas (Opcional, se puede mejorar con un servicio)
            user.profileImageUrl?.let { oldUrl ->
                val oldFileId = oldUrl.substringAfterLast("/")
                gridFsTemplate.delete(Query.query(Criteria.where("_id").`is`(oldFileId)))
            }


            // 5. Actualizar el modelo y guardar en MongoDB
            user.profileImageUrl = downloadUrl
            user.updatedAt = LocalDateTime.now()
            userRepository.save(user)

            // 6. Retornar la nueva URL al cliente
            return ResponseEntity.ok(PhotoData(downloadUrl))

        } catch (e: IOException) {
            e.printStackTrace()
            // Error al leer/escribir el stream (común en despliegues)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    // ----------------------------------------------------
    // 🎯 LÓGICA DE LECTURA DE FOTO (GRIDFS)
    // ----------------------------------------------------

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile/photo") // Ruta completa: /users/profile/photo
    fun getProfilePhotoUrl(): ResponseEntity<PhotoData> {
        val userId = getAuthenticatedUserId()
        val userOptional = userRepository.findById(userId)

        if (!userOptional.isPresent) {
            return ResponseEntity.notFound().build()
        }

        val user = userOptional.get()
        // Usamos la URL de descarga guardada en el perfil
        val photoUrl = user.profileImageUrl ?: "https://placehold.co/200x200"

        // Retornamos la URL de descarga al cliente
        return ResponseEntity.ok(PhotoData(photoUrl))
    }

    // ----------------------------------------------------
    // 🎯 LÓGICA DE SERVICIO DE ARCHIVOS (CORREGIDO)
    // ----------------------------------------------------

    // 🎯 Endpoint para SERVIR el archivo desde GridFS
    // Nota: La ruta es /users/files/photo/{fileId} porque el @RequestMapping es /users
    // 🎯 Endpoint para SERVIR el archivo desde GridFS (CORREGIDO)
    // 🎯 Endpoint para SERVIR el archivo desde GridFS (CORRECCIÓN FINAL)
    @GetMapping("/files/photo/{fileId}")
    fun servePhoto(@PathVariable fileId: String): ResponseEntity<ByteArray> {
        val gridFsFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").`is`(fileId)))

        // 🛑 CORRECCIÓN CLAVE: Accedemos al campo 'contentType' dentro de los metadatos de GridFS.
        // Usamos 'as String' porque sabemos que Spring lo guarda como String.
        val contentType = gridFsFile.metadata?.get("_contentType") as? String ?: MediaType.IMAGE_JPEG_VALUE

        try {
            val resource = gridFsTemplate.getResource(gridFsFile)
            val fileBytes = resource.inputStream.readBytes()

            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(fileBytes)

        } catch (e: Exception) {
            e.printStackTrace()
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    // ----------------------------------------------------
    // Métodos Estándar
    // ----------------------------------------------------

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