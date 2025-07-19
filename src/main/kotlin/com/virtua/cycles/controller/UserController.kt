package com.virtua.cycles.controller

import com.virtua.cycles.model.User
import com.virtua.cycles.repository.UserRepository
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus

@RestController
@RequestMapping("/users")
class UserController(private val userRepository: UserRepository) {

    // Crear usuario
    @PostMapping
    fun createUser(@Valid @RequestBody user: User): ResponseEntity<User> {
        val savedUser = userRepository.save(user)
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser)
    }

    // Listar todos los usuarios
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

