package com.diana.ComTec.service

import com.diana.ComTec.dto.UsuarioRequest
import com.diana.ComTec.dto.UsuarioResponse
import com.diana.ComTec.model.Usuario
import com.diana.ComTec.repository.EstadoUsuarioRepository
import com.diana.ComTec.repository.PerfilRepository
import com.diana.ComTec.repository.UsuarioRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID

@Service
class UsuarioService(
    private val usuarioRepository: UsuarioRepository,
    private val perfilRepository: PerfilRepository,
    private val estadoUsuarioRepository: EstadoUsuarioRepository,
    private val passwordEncoder: PasswordEncoder
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val usuario = usuarioRepository.findByStrNombreUsuario(username)
            .orElseThrow { UsernameNotFoundException("Usuario no encontrado: $username") }
        return User.builder()
            .username(usuario.strNombreUsuario)
            .password(usuario.strPwd)
            .authorities(SimpleGrantedAuthority("ROLE_USER"))
            .build()
    }

    fun findByUsername(username: String): Usuario? =
        usuarioRepository.findByStrNombreUsuario(username).orElse(null)

    fun listar(page: Int, size: Int = 5): Page<Usuario> =
        usuarioRepository.findAll(PageRequest.of(page, size))

    fun buscar(nombre: String, page: Int, size: Int = 5): Page<Usuario> =
        usuarioRepository.findByStrNombreUsuarioContainingIgnoreCase(
            nombre, PageRequest.of(page, size)
        )

    fun obtener(id: Int): Usuario? = usuarioRepository.findById(id).orElse(null)

    fun crear(req: UsuarioRequest): Usuario {
        val perfil = perfilRepository.findById(req.idPerfil)
            .orElseThrow { IllegalArgumentException("Perfil no encontrado") }
        val estado = estadoUsuarioRepository.findById(req.idEstadoUsuario)
            .orElseThrow { IllegalArgumentException("Estado no encontrado") }
        val usuario = Usuario(
            strNombreUsuario = req.strNombreUsuario,
            perfil           = perfil,
            strPwd           = passwordEncoder.encode(req.strPwd),
            estadoUsuario    = estado,
            strCorreo        = req.strCorreo,
            strNumeroCelular = req.strNumeroCelular
        )
        return usuarioRepository.save(usuario)
    }

    fun actualizar(id: Int, req: UsuarioRequest): Usuario? {
        val existente = usuarioRepository.findById(id).orElse(null) ?: return null
        val perfil = perfilRepository.findById(req.idPerfil)
            .orElseThrow { IllegalArgumentException("Perfil no encontrado") }
        val estado = estadoUsuarioRepository.findById(req.idEstadoUsuario)
            .orElseThrow { IllegalArgumentException("Estado no encontrado") }
        val pwd = if (req.strPwd.isBlank()) existente.strPwd
        else passwordEncoder.encode(req.strPwd)
        val actualizado = existente.copy(
            strNombreUsuario = req.strNombreUsuario,
            perfil           = perfil,
            strPwd           = pwd,
            estadoUsuario    = estado,
            strCorreo        = req.strCorreo,
            strNumeroCelular = req.strNumeroCelular
        )
        return usuarioRepository.save(actualizado)
    }

    fun eliminar(id: Int): Boolean {
        if (!usuarioRepository.existsById(id)) return false
        usuarioRepository.deleteById(id)
        return true
    }

    fun subirImagen(id: Int, file: MultipartFile): String? {
        val usuario = usuarioRepository.findById(id).orElse(null) ?: return null

        // Crear directorio si no existe
        val uploadDir = Paths.get("uploads/usuarios")
        if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir)

        // Generar nombre único
        val extension = file.originalFilename?.substringAfterLast('.', "jpg") ?: "jpg"
        val nombreArchivo = "${UUID.randomUUID()}.$extension"
        val rutaArchivo = uploadDir.resolve(nombreArchivo)

        // Guardar archivo
        Files.copy(file.inputStream, rutaArchivo)

        // Eliminar imagen anterior si existe
        if (!usuario.strImagen.isNullOrBlank()) {
            val anterior = uploadDir.resolve(usuario.strImagen!!)
            if (Files.exists(anterior)) Files.delete(anterior)
        }

        // Actualizar en BD
        usuarioRepository.save(usuario.copy(strImagen = nombreArchivo))
        return nombreArchivo
    }

    fun toResponse(u: Usuario) = UsuarioResponse(
        id               = u.id,
        strNombreUsuario = u.strNombreUsuario,
        idPerfil         = u.perfil.id,
        strNombrePerfil  = u.perfil.strNombrePerfil,
        idEstadoUsuario  = u.estadoUsuario.id,
        strEstado        = u.estadoUsuario.strEstado,
        strCorreo        = u.strCorreo,
        strNumeroCelular = u.strNumeroCelular,
        strImagen        = u.strImagen
    )

    fun listarPerfiles(): List<Map<String, Any>> =
        perfilRepository.findAll().map { mapOf("id" to it.id, "nombre" to it.strNombrePerfil) }

    fun listarEstados(): List<Map<String, Any>> =
        estadoUsuarioRepository.findAll().map { mapOf("id" to it.id, "nombre" to it.strEstado) }
}