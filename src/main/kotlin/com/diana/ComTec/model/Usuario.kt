package com.diana.ComTec.model

import jakarta.persistence.*

@Entity
@Table(name = "Usuario")
data class Usuario(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(name = "strNombreUsuario", nullable = false, unique = true, length = 100)
    val strNombreUsuario: String = "",

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idPerfil", nullable = false)
    val perfil: Perfil = Perfil(),

    @Column(name = "strPwd", nullable = false, length = 255)
    val strPwd: String = "",

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idEstadoUsuario", nullable = false)
    val estadoUsuario: EstadoUsuario = EstadoUsuario(),

    @Column(name = "strCorreo", length = 150)
    val strCorreo: String? = null,

    @Column(name = "strNumeroCelular", length = 20)
    val strNumeroCelular: String? = null,

    @Column(name = "strImagen", length = 255)
    val strImagen: String? = null
)