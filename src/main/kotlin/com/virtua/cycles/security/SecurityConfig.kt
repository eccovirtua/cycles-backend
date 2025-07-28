package com.virtua.cycles.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@EnableMethodSecurity
@Configuration
class SecurityConfig(
    private val jwtAuthFilter: JwtAuthenticationFilter
) {

    /** 1) El encoder de contraseñas */
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    /** 2) El proveedor DAO: tu UserDetailsService + el encoder */
    @Bean
    fun daoAuthenticationProvider(
        passwordEncoder: PasswordEncoder,
        userDetailsService: CustomUserDetailsService
    ): DaoAuthenticationProvider =
        DaoAuthenticationProvider().apply {
            setUserDetailsService(userDetailsService)
            setPasswordEncoder(passwordEncoder)
        }

    /** 3) Expone el AuthenticationManager para poder inyectarlo en tus controladores */
    @Bean
    fun authenticationManager(
        authConfig: AuthenticationConfiguration
    ): AuthenticationManager = authConfig.authenticationManager

    /** 4) Cadena de seguridad: registra el DAO provider y tu filtro JWT */
    @Bean
    fun securityFilterChain(http: HttpSecurity,daoAuthenticationProvider: DaoAuthenticationProvider): SecurityFilterChain {
        http
            .authenticationProvider(daoAuthenticationProvider)
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    // Permitir acceso a las rutas y definir los permisos necesarios
                    .requestMatchers("/", "/index.html", "/vite.svg", "/assets/**").permitAll()
                    .requestMatchers("/api/auth/register").permitAll()
                    .requestMatchers("/api/auth/login").permitAll()
                    .requestMatchers("/api/auth/forgot-password").permitAll()
                    .requestMatchers("/api/auth/check-username").permitAll()
                    .requestMatchers("/api/recommend/**").permitAll()
                    .requestMatchers("/error").permitAll()
                    .requestMatchers("/api/auth/update-username").authenticated()
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/admin/**").hasRole("ADMIN")
                    .requestMatchers("/users/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }






    /** 5) Configuración global de CORS si la necesitas */
    @Bean
    fun corsConfigurer(): WebMvcConfigurer = object : WebMvcConfigurer {
        override fun addCorsMappings(registry: CorsRegistry) {
            registry.addMapping("/**")
                .allowedOrigins("https://tu-frontend.com") // ajusta a tu dominio
                .allowedMethods("*")
                .allowCredentials(true)
        }
    }
}
