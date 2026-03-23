package com.diana.ComTec.model

import jakarta.persistence.*

@Entity
@Table(name = "PermisosPerfil")
data class PermisosPerfil(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idModulo", nullable = false)
    val modulo: Modulo = Modulo(),

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idPerfil", nullable = false)
    val perfil: Perfil = Perfil(),

    @Column(name = "bitAgregar", nullable = false)
    val bitAgregar: Boolean = false,

    @Column(name = "bitEditar", nullable = false)
    val bitEditar: Boolean = false,

    @Column(name = "bitConsulta", nullable = false)
    val bitConsulta: Boolean = false,

    @Column(name = "bitEliminar", nullable = false)
    val bitEliminar: Boolean = false,

    @Column(name = "bitDetalle", nullable = false)
    val bitDetalle: Boolean = false
)