package com.diana.ComTec.model

import jakarta.persistence.*

@Entity
@Table(name = "Modulo")
data class Modulo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(name = "strNombreModulo", nullable = false, length = 100)
    val strNombreModulo: String = ""
)