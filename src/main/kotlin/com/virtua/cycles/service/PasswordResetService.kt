package com.virtua.cycles.service
import org.springframework.stereotype.Service
import com.virtua.cycles.dto.GenericResponse
import com.virtua.cycles.dto.ResetPasswordRequest
import com.virtua.cycles.model.PasswordResetToken
import com.virtua.cycles.model.User
import com.virtua.cycles.repository.PasswordResetTokenRepository
import com.virtua.cycles.repository.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime



@Service
class PasswordResetService(
    private val userRepository: UserRepository,
    private val emailService: EmailService,
    private val passwordEncoder: PasswordEncoder,
    private val codigoRepository: PasswordResetTokenRepository

) {
    fun generateAndSendCode(email: String): ResponseEntity<GenericResponse> {
        val user = userRepository.findByEmail(email)
            ?: return ResponseEntity.ok(GenericResponse("Si el correo está registrado, se enviará un código."))

        val code = (100000..999999).random().toString()
        val expiresAt = LocalDateTime.now().plusMinutes(10)

        // Eliminar códigos anteriores si existen
        codigoRepository.deleteByEmail(email)

        // Guardar el nuevo token
        val token = PasswordResetToken(
            email = email,
            code = code,
            expiresAt = expiresAt
        )
        codigoRepository.save(token)

        // Enviar el código por correo
        val sent = emailService.sendEmail(
            to = email,
            subject = "Código de recuperación de contraseña",
            text = "Tu código de recuperación es: $code"
        )

        return if (sent) {
            ResponseEntity.ok(GenericResponse("Código enviado al correo"))
        } else {
            ResponseEntity.status(500).body(GenericResponse("No se pudo enviar el correo"))
        }
    }

    fun resetPassword(request: ResetPasswordRequest): ResponseEntity<GenericResponse> {
        val resetToken = codigoRepository.findByEmail(request.email)
            ?: return ResponseEntity.badRequest().body(GenericResponse("Código inválido"))

        if (resetToken.code != request.code || resetToken.expiresAt.isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(GenericResponse("Código inválido o expirado"))
        }

        val user: User = userRepository.findByEmail(request.email)
            ?: return ResponseEntity.badRequest().body(GenericResponse("Usuario no encontrado"))

        user.setPassword(passwordEncoder.encode(request.newPassword))
        userRepository.save(user)

        // Eliminar el token usado
        codigoRepository.deleteByEmail(request.email)

        return ResponseEntity.ok(GenericResponse("Contraseña cambiada"))
    }

    fun verifyCode(email: String, code: String): Boolean {
        val token = codigoRepository.findByEmail(email)
            ?: return false

        return token.code == code && token.expiresAt.isAfter(LocalDateTime.now())
    }
}