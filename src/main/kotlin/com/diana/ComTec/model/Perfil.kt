package com.diana.ComTec.model

import jakarta.persistence.*

@Entity
@Table(name = "Perfil")
data class Perfil(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(name = "strNombrePerfil", nullable = false, length = 100)
    val strNombrePerfil: String = "",

    @Column(name = "bitAdministrador", nullable = false)
    val bitAdministrador: Boolean = false
)