package com.diana.ComTec.service

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class CloudinaryService(private val cloudinary: Cloudinary) {

    @Value("\${cloudinary.folder}")
    private lateinit var folder: String

    // Sube imagen y retorna solo el nombre del archivo (sin URL)
    fun subirImagen(file: MultipartFile, publicId: String): String {
        val opciones = mapOf(
            "folder"        to folder,
            "public_id"     to publicId,
            "resource_type" to "image",
            "overwrite"     to true
        )

        val resultado = cloudinary.uploader().upload(file.bytes, opciones)

        // Retorna solo el public_id sin la carpeta (solo el nombre)
        val fullPublicId = resultado["public_id"] as String
        return fullPublicId.substringAfterLast("/")
    }

    // Elimina imagen por nombre
    fun eliminarImagen(nombreArchivo: String) {
        try {
            val publicId = "$folder/$nombreArchivo"
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap())
        } catch (e: Exception) {
            // No crítico
        }
    }

    // Construye la URL completa desde el nombre
    fun construirUrl(nombreArchivo: String): String {
        val cloudName = cloudinary.config.cloudName
        return "https://res.cloudinary.com/$cloudName/image/upload/$folder/$nombreArchivo"
    }
}