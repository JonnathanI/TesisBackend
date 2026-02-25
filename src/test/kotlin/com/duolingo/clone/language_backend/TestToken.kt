import com.google.auth.oauth2.GoogleCredentials
import java.io.FileInputStream

fun main() {
    val credentials = GoogleCredentials
        .fromStream(FileInputStream("src/main/resources/firebase-credentials.json"))
        .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))

    credentials.refreshIfExpired()

    println("🔑 ACCESS TOKEN:")
    println(credentials.accessToken.tokenValue)
}