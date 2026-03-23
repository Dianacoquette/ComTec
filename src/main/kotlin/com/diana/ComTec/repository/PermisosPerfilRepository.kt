package com.diana.ComTec.repository

import com.diana.ComTec.model.PermisosPerfil
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PermisosPerfilRepository : JpaRepository<PermisosPerfil, Int > {
    fun findByPerfilId(perfilId: Int): List<PermisosPerfil>
    fun findByPerfilId(perfilId: Int, pageable: Pageable): Page<PermisosPerfil>

}