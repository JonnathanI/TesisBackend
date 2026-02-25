package com.duolingo.clone.language_backend.service

import com.google.auth.oauth2.GoogleCredentials
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.net.HttpURLConnection
import java.net.URL
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

@Service
class FcmService {

    private val mapper = jacksonObjectMapper()
    private val fcmUrl =
        "https://fcm.googleapis.com/v1/projects/europeek-ee4ae/messages:send"

    private fun getAccessToken(): String {
        val credentialsJson = System.getenv("FIREBASE_CREDENTIALS")
            ?: throw IllegalStateException("FIREBASE_CREDENTIALS no está configurado")

        val credentials = GoogleCredentials
            .fromStream(ByteArrayInputStream(credentialsJson.toByteArray()))
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
        val message = mapOf(
            "message" to mapOf(
                "token" to token,
                "notification" to mapOf(
                    "title" to title,
                    "body" to body
                ),
                "data" to data
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

        val code = connection.responseCode
        if (code in 200..299) {
            val response = connection.inputStream.bufferedReader().readText()
            println("🔥 FCM Response OK ($code): $response")
        } else {
            val errorBody = connection.errorStream?.bufferedReader()?.readText()
            println("❌ FCM ERROR ($code): $errorBody")
        }
    }
}