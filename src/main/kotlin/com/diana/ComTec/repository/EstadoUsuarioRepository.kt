package com.diana.ComTec.repository

import com.diana.ComTec.model.EstadoUsuario
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EstadoUsuarioRepository : JpaRepository<EstadoUsuario, Int>