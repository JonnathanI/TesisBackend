package com.duolingo.clone.language_backend

import com.duolingo.clone.language_backend.service.CloudinaryService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest
class CloudinaryConnectionTest {

    @Autowired
    private lateinit var cloudinaryService: CloudinaryService

    @Test
    fun `test connection and upload to cloudinary`() {
        // Creamos un archivo de imagen falso en memoria
        val mockFile = MockMultipartFile(
            "file",
            "test_image.png",
            "image/png",
            "test image content".toByteArray()
        )

        // Intentamos subirlo
        val url = cloudinaryService.uploadFile(mockFile, "test_folder")

        println("✅ Upload exitoso! URL: $url")

        assertNotNull(url)
        assertTrue(url.startsWith("https://res.cloudinary.com"))
    }
}