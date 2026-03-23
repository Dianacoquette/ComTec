package com.diana.ComTec.repository

import com.diana.ComTec.model.Usuario
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UsuarioRepository : JpaRepository<Usuario, Int> {
    fun findByStrNombreUsuario(strNombreUsuario: String): Optional<Usuario>
    fun findByStrNombreUsuarioContainingIgnoreCase(nombre: String, pageable: Pageable): Page<Usuario>
}