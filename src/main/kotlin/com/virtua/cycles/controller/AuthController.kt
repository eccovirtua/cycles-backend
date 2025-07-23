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
        // 1) Verificar si ya existe un usuario con ese email
        if (userRepository.findByEmail(req.email) != null) {
            return ResponseEntity.status(409).body("Email ya registrado")
        }
        //2.1 Obtener y validar la edad
        val age = req.age ?: throw BadRequestException("La edad es obligatoria")
        if (age < 10) throw BadRequestException("Debes tener al menos 10 años")

        // 2.2) Hashear la contraseña y crear el usuario (role = USER)
        val user = User(
            name = req.name,
            email = req.email,
            age = age,
            _password = passwordEncoder.encode(req.password),
            role = User.Role.USER
        )
        userRepository.save(user)
        val token = jwtService.generateToken(user) //generar token a partir de usuario registrado/guardado

        return ResponseEntity
        .status(HttpStatus.CREATED)
            .body(AuthenticationResponse(token))
    }

    @PostMapping("/login")
    fun login(@RequestBody authRequest: AuthenticationRequest): ResponseEntity<AuthenticationResponse> {
        // 1) Loguear lo que llega:
        println("🕵️‍♂️ Login attempt con: $authRequest")

        try {
            val authenticationToken =
                UsernamePasswordAuthenticationToken(authRequest.email, authRequest.password)
            val authentication = authenticationManager.authenticate(authenticationToken)

            // 2) Si pasamos, devolvemos el token
            val userDetails = authentication.principal as org.springframework.security.core.userdetails.User
            val jwt = jwtService.generateToken(userDetails)
            println("✅ Authentication OK, token generado")
            return ResponseEntity.ok(AuthenticationResponse(jwt))

        } catch (ex: Exception) {
            // 3) Loguear la excepción para ver la causa
            println("🚨 Error autenticando: ${ex::class.simpleName}: ${ex.message}")
            return ResponseEntity.status(401).build()
        }
    }




    @PostMapping("/forgot-password")
    fun forgotPassword(@RequestBody request: ForgotPasswordRequest): ResponseEntity<GenericResponse> {
        return passwordResetService.generateAndSendCode(request.email)
    }

    @PostMapping("/verify-code")
    fun verifyCode(@RequestBody request: VerifyCodeRequest): ResponseEntity<GenericResponse> {
        val isValid = passwordResetService.verifyCode(request.email, request.code)
        return if (isValid)
            ResponseEntity.ok(GenericResponse("Código válido"))
        else
            ResponseEntity.badRequest().body(GenericResponse("Código inválido o expirado"))
    }
    @PostMapping("/request-password-reset")
    fun requestPasswordReset(@RequestBody email: Map<String, String>): ResponseEntity<GenericResponse> {
        val userEmail = email["email"] ?: return ResponseEntity.badRequest()
            .body(GenericResponse("El campo 'email' es requerido"))
        return passwordResetService.generateAndSendCode(userEmail)
    }
    // Endpoint para establecer nueva contraseña con código
    @PostMapping("/reset-password")
    fun resetPassword(@RequestBody request: ResetPasswordRequest): ResponseEntity<GenericResponse> {
        return passwordResetService.resetPassword(request)
    }






}
