package com.diana.ComTec.model

import jakarta.persistence.*

@Entity
@Table(name = "Menu")
data class Menu(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(name = "idMenu", nullable = false)
    val idMenu: Int = 0,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idModulo", nullable = false)
    val modulo: Modulo = Modulo()
)