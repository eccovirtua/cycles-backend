package com.virtua.cycles.controller

import com.virtua.cycles.dto.RegisterRequest
import com.virtua.cycles.model.User
import com.virtua.cycles.repository.UserRepository
import com.virtua.cycles.security.JwtService
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import com.virtua.cycles.dto.AuthenticationRequest
import com.virtua.cycles.dto.AuthenticationResponse
import org.apache.coyote.BadRequestException
//import org.apache.coyote.Response
import org.springframework.http.HttpStatus
import com.virtua.cycles.dto.ForgotPasswordRequest
import com.virtua.cycles.dto.GenericResponse
import com.virtua.cycles.dto.ResetPasswordRequest
import com.virtua.cycles.dto.VerifyCodeRequest
import com.virtua.cycles.service.PasswordResetService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails



//Servicio de autenticación y registro. Se valida que no exista un usuario con mail ya registrado y autenticación (login)

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val passwordResetService: PasswordResetService
) {

    @PostMapping("/register")
    fun register(@RequestBody req: RegisterRequest): ResponseEntity<Any> {
        if (userRepository.findByEmail(req.email) != null) {
            return ResponseEntity.status(409).body("Email ya registrado")
        }
        val age = req.age ?: throw BadRequestException("La edad es obligatoria")
        if (age < 10) throw BadRequestException("Debes tener al menos 10 años")

        val user = User(
            name     = req.name,
            email    = req.email,
            age      = age,
            _password = passwordEncoder.encode(req.password),  // <— CORREGIDO
            role     = User.Role.USER
        )
        userRepository.save(user)
        val token = jwtService.generateToken(user)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(AuthenticationResponse(token))
    }

    @PostMapping("/login")
    fun login(@RequestBody authRequest: AuthenticationRequest): ResponseEntity<AuthenticationResponse> {
        return try {
            val authenticationToken = UsernamePasswordAuthenticationToken(
                authRequest.usernameOrEmail,
                authRequest.password
            )
            val authentication = authenticationManager.authenticate(authenticationToken)
            val userDetails    = authentication.principal as UserDetails
            val jwt            = jwtService.generateToken(userDetails)
            ResponseEntity.ok(AuthenticationResponse(jwt))
        } catch (_: Exception) {
            ResponseEntity.status(401).build()
        }
    }

    @PostMapping("/forgot-password")
    fun forgotPassword(@RequestBody request: ForgotPasswordRequest): ResponseEntity<GenericResponse> =
        passwordResetService.generateAndSendCode(request.email)

    @PostMapping("/verify-code")
    fun verifyCode(@RequestBody request: VerifyCodeRequest): ResponseEntity<GenericResponse> {
        val isValid = passwordResetService.verifyCode(request.email, request.code)
        return if (isValid)
            ResponseEntity.ok(GenericResponse("Código válido"))
        else
            ResponseEntity.badRequest().body(GenericResponse("Código inválido o expirado"))
    }

    @PutMapping("/reset-password")
    fun resetPassword(@RequestBody request: ResetPasswordRequest): ResponseEntity<GenericResponse> =
        passwordResetService.resetPassword(request)

    @PostMapping("/check-username")
    fun checkUsername(@RequestBody body: Map<String, String>): ResponseEntity<GenericResponse> {
        val name = body["name"]
            ?: return ResponseEntity.badRequest().body(GenericResponse("name es requerido"))
        val exists = userRepository.findByName(name) != null
        val msg = if (exists) "taken" else "available"
        return ResponseEntity.ok(GenericResponse(msg))
    }

    @PatchMapping("/update-username")
    fun updateUsername(
        @RequestBody body: Map<String, String>,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<GenericResponse> {
        val name = body["name"]
            ?: return ResponseEntity.badRequest().body(GenericResponse("name es requerido"))

        val user = userRepository.findByEmail(userDetails.username)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(GenericResponse("Usuario no encontrado"))

        user.name = name
        userRepository.save(user)
        return ResponseEntity.ok(GenericResponse("Nombre de usuario actualizado"))
    }
}



//llave de cierre a  los endpoints
