package com.duolingo.clone.language_backend.service

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class CloudinaryService(
    @Value("\${cloudinary.cloud-name}") private val cloudName: String,
    @Value("\${cloudinary.api-key}") private val apiKey: String,
    @Value("\${cloudinary.api-secret}") private val apiSecret: String
) {

    // Inicializamos Cloudinary usando las propiedades inyectadas
    private val cloudinary = Cloudinary(ObjectUtils.asMap(
        "cloud_name", cloudName,
        "api_key", apiKey,
        "api_secret", apiSecret,
        "secure", true
    ))

    fun uploadFile(file: MultipartFile, folder: String): String {
        // Validación de seguridad: si el archivo es nulo, está vacío o es el placeholder de React
        if (file.isEmpty || file.originalFilename == "placeholder.txt") {
            return ""
        }

        return try {
            val options = ObjectUtils.asMap(
                "folder", "duolingo_clone/$folder",
                "resource_type", "auto" // Detecta automáticamente si es JPG, PNG, MP3, etc.
            )

            val uploadResult = cloudinary.uploader().upload(file.bytes, options)

            // Retornamos la URL segura (https)
            uploadResult["secure_url"] as String
        } catch (e: Exception) {
            // Imprime el error en la consola de IntelliJ para que puedas debuguear si falla
            println("ERROR CLOUDINARY: ${e.message}")
            throw RuntimeException("Error al subir archivo a Cloudinary: ${e.message}")
        }
    }
}