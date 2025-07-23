package com.virtua.cycles.service

import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import org.springframework.mail.SimpleMailMessage

@Service
class EmailService(private val mailSender: JavaMailSender) {

    fun sendEmail(to: String, subject: String, text: String): Boolean {
        return try {
            val message = SimpleMailMessage()
            message.setTo(to)
            message.subject = subject
            message.text = text
            mailSender.send(message)
            true
        } catch (e: Exception) {
            println("Error al enviar correo: ${e.message}")
            false
        }
    }
}
