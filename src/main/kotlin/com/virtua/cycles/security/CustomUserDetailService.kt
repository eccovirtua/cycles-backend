package com.virtua.cycles.security

import com.virtua.cycles.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(usernameOrEmail: String): UserDetails {
        val user = userRepository.findByEmailOrName(usernameOrEmail, usernameOrEmail)
            ?: throw UsernameNotFoundException("Usuario con email $usernameOrEmail no encontrado")

        return org.springframework.security.core.userdetails.User
            .withUsername(user.email)
            .password(user.password)
//            .authorities("ROLE_${user.role.name}")
            .authorities("ROLE_${user.role}")// puedes cambiar según tus roles
            .build()
    }
}
