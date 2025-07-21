//clase user. esta clase maneja la estructura principal de un usuario de la app.
package com.virtua.cycles.model

import jakarta.validation.constraints.NotBlank
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
//seguridad
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails


@Document(collection = "users")
data class User(
    @Id val id: String? = null,

    @field:NotBlank(message = "User name cannot be blank") //validaciones de campo no rellenado
    val name: String,

    @field:NotBlank(message = "el correo no puede estar vacío")
    val email: String,

    val age: Int? = null,

    @field:NotBlank(message = "la contraseña es obligatoria")
    private val password: String,

    val role: Role = Role.USER,



    val cratedAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) : UserDetails {
    enum class Role {
        USER, ADMIN
    }
    override fun getAuthorities(): Collection<GrantedAuthority> =
        listOf(SimpleGrantedAuthority("ROLE_${role.name}"))

    override fun getPassword(): String = password

    override fun getUsername(): String = email

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true
}