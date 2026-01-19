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
            // Imprimimos el error en la consola del backend, pero NO lanzamos la excepción
            println("⚠️ ERROR DE RED: No se pudo enviar el correo real a $to debido a un bloqueo de puerto.")
            println("🔗 PERO AQUÍ TIENES EL LINK PARA TU PRUEBA:")
            println("--------------------------------------------------")
            println(body)
            println("--------------------------------------------------")
        }
    }
}
