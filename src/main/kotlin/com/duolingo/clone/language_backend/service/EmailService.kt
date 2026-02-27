package com.duolingo.clone.language_backend.service

import com.sendgrid.Method
import com.sendgrid.Request
import com.sendgrid.SendGrid
import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.Content
import com.sendgrid.helpers.mail.objects.Email
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class EmailService(
    @Value("\${sendgrid.api-key}") private val sendgridApiKey: String
) {

    fun sendEmail(to: String, subject: String, body: String) {
        try {
            val fromEmail = Email("jonnathanjose67@gmail.com")  // tu correo verificado en SendGrid
            val toEmail = Email(to)
            val content = Content("text/plain", body)

            val mail = Mail(fromEmail, subject, toEmail, content)

            val sendGrid = SendGrid(sendgridApiKey)

            val request = Request()
            request.method = Method.POST        // ✅ ahora sí es válido
            request.endpoint = "mail/send"
            request.body = mail.build()

            val response = sendGrid.api(request)

            println("📧 SendGrid Status: ${response.statusCode}")
            println("📨 Body: ${response.body}")
            println("📬 Headers: ${response.headers}")

        } catch (e: Exception) {
            println("❌ ERROR EN SENDGRID: ${e.message}")
            e.printStackTrace()
        }
    }
}
