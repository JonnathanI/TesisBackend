package com.duolingo.clone.language_backend.service

import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val mailSender: JavaMailSender
) {
    fun sendEmail(to: String, subject: String, body: String) {
        try {
            val message = SimpleMailMessage()
            message.setTo(to)
            message.subject = subject
            message.text = body
            mailSender.send(message)
            println("✅ Correo enviado exitosamente a $to")
        } catch (e: Exception) {
            println("❌ ERROR REAL DE SMTP: ${e.message}") // 💡 Esto te dirá si es "Auth Failed" o "Timeout"
            e.printStackTrace() // Esto imprimirá el rastro completo en Render

            println("🔗 LINK DE RESPALDO (Copiado de la consola):")
            println(body)
        }
    }
}
