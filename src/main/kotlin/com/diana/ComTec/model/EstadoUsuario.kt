package com.diana.ComTec.model

import jakarta.persistence.*

@Entity
@Table(name = "EstadoUsuario")
data class EstadoUsuario(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(name = "strEstado", nullable = false, length = 50)
    val strEstado: String = ""
)