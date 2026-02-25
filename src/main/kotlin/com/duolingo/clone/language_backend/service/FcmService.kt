package com.duolingo.clone.language_backend.service

import com.google.auth.oauth2.GoogleCredentials
import org.springframework.stereotype.Service
import java.net.HttpURLConnection
import java.net.URL
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
@Service
class FcmService {

    private val mapper = jacksonObjectMapper()
    private val fcmUrl =
        "https://fcm.googleapis.com/v1/projects/europeek-ee4ae/messages:send"

    private fun getAccessToken(): String {
        val credentials = GoogleCredentials
            .fromStream(this::class.java.getResourceAsStream("/firebase-credentials.json"))
            .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
        credentials.refreshIfExpired()
        return credentials.accessToken.tokenValue
    }

    fun sendPush(
        token: String,
        title: String,
        body: String,
        data: Map<String, String> = emptyMap()
    ) {
        // 👇 Mezclamos title/body dentro del data
        val mergedData = data.toMutableMap().apply {
            put("title", title)
            put("body", body)
        }

        // 👇 MENSAJE SOLO DE DATA (sin "notification")
        val message = mapOf(
            "message" to mapOf(
                "token" to token,
                "data" to mergedData
            )
        )

        val json = mapper.writeValueAsString(message)

        val url = URL(fcmUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Authorization", "Bearer ${getAccessToken()}")
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        connection.doOutput = true

        connection.outputStream.use { os ->
            os.write(json.toByteArray())
        }

        val responseCode = connection.responseCode
        val responseBody = try {
            connection.inputStream.bufferedReader().readText()
        } catch (e: Exception) {
            connection.errorStream?.bufferedReader()?.readText() ?: e.message.orEmpty()
        }

        println("🔥 FCM Response ($responseCode): $responseBody")
    }
}