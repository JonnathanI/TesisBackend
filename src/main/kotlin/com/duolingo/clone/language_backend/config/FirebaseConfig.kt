package com.duolingo.clone.language_backend.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.springframework.context.annotation.Configuration
import java.io.ByteArrayInputStream

@Configuration
class FirebaseConfig {
/*
    init {
        try {
            val credentialsJson = System.getenv("FIREBASE_CREDENTIALS")

            if (!credentialsJson.isNullOrBlank()) {

                val serviceAccountStream = ByteArrayInputStream(credentialsJson.toByteArray())

                val options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                    .build()

                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options)
                    println("🔥 Firebase Admin inicializado correctamente (ENV)")
                }

            } else {
                println("⚠️ FIREBASE_CREDENTIALS no está configurado. Firebase Admin NO se inicializa.")
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
            println("❌ Error inicializando Firebase Admin (ENV)")
        }
    }*/
}