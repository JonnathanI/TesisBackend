package com.duolingo.clone.language_backend.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource

@Configuration
class FirebaseConfig {

    init {
        try {
            // El archivo debe estar en src/main/resources/firebase-credentials.json
            val serviceAccount = ClassPathResource("firebase-credentials.json").inputStream

            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build()

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options)
                println("🔥 Firebase Admin inicializado")
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            println("❌ Error inicializando Firebase Admin")
        }
    }
}