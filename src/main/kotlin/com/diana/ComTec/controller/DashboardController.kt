package com.diana.ComTec.controller

import com.diana.ComTec.repository.MenuRepository
import com.diana.ComTec.repository.PermisosPerfilRepository
import com.diana.ComTec.service.JwtService
import com.diana.ComTec.service.UsuarioService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class DashboardController(
    private val jwtService: JwtService,
    private val usuarioService: UsuarioService,
    private val permisosPerfilRepository: PermisosPerfilRepository,
    private val menuRepository: MenuRepository
) {

    @GetMapping("/dashboard")
    fun dashboard(request: HttpServletRequest, model: Model): String {
        val token = request.cookies?.find { it.name == "jwt_token" }?.value
            ?: return "redirect:/login"

        val username = jwtService.extractUsername(token)
        val perfilId = jwtService.extractPerfilId(token)
        val usuario  = usuarioService.findByUsername(username) ?: return "redirect:/login"

        // Permisos activos del perfil
        val permisos = permisosPerfilRepository.findByPerfilId(perfilId)
        val permisosActivos = permisos.filter {
            it.bitAgregar || it.bitEditar || it.bitConsulta || it.bitEliminar || it.bitDetalle
        }

        // IDs de módulos con al menos un permiso activo
        val modulosConPermiso = permisosActivos.map { it.modulo.id }.toSet()

        val menus = menuRepository.findAll()

        // ── Menú Seguridad: solo módulos con permiso ──
        val menuSeguridad = menus.filter {
            it.idMenu == 1 && it.modulo.id in modulosConPermiso
        }

        // ── Menú Principal 1: solo si tiene módulos con permiso ──
        val menuPrincipal1 = menus.filter {
            it.idMenu == 2 && it.modulo.id in modulosConPermiso
        }

        // ── Menú Principal 2: solo si tiene módulos con permiso ──
        val menuPrincipal2 = menus.filter {
            it.idMenu == 3 && it.modulo.id in modulosConPermiso
        }

        // ── Rutas para menú Seguridad ──
        val rutasModulo = menuSeguridad.associate { menu ->
            menu.modulo.strNombreModulo to rutaModulo(menu.modulo.strNombreModulo)
        }

        // ── Tarjetas del dashboard con info correcta ──
        // Incluye todos los módulos con permiso con su menú y ruta correcta
        val tarjetas = permisosActivos.map { p ->
            val menuEntry = menus.find { it.modulo.id == p.modulo.id }
            val idMenu = menuEntry?.idMenu ?: 1
            mapOf(
                "nombre"    to p.modulo.strNombreModulo,
                "menuNombre" to nombreMenu(idMenu),
                "ruta"      to rutaTarjeta(p.modulo.strNombreModulo, idMenu),
                "icono"     to iconoModulo(p.modulo.strNombreModulo, idMenu),
                "colorClass" to colorModulo(idMenu)
            )
        }

        model.addAttribute("usuario",        usuario)
        model.addAttribute("permisos",       permisosActivos)
        model.addAttribute("menuSeguridad",  menuSeguridad)
        model.addAttribute("menuPrincipal1", menuPrincipal1)
        model.addAttribute("menuPrincipal2", menuPrincipal2)
        model.addAttribute("rutasModulo",    rutasModulo)
        model.addAttribute("tarjetas",       tarjetas)

        return "dashboard"
    }

    // ── Nombre del menú según idMenu ──────────────────
    private fun nombreMenu(idMenu: Int): String = when (idMenu) {
        1    -> "Seguridad"
        2    -> "Principal 1"
        3    -> "Principal 2"
        else -> "General"
    }

    // ── Ruta para botón Nuevo y tarjetas ─────────────
    private fun rutaTarjeta(nombre: String, idMenu: Int): String = when (idMenu) {
        1 -> rutaModulo(nombre)
        2 -> when (nombre) {
            "Principal 1.1" -> "/principal/1-1"
            "Principal 1.2" -> "/principal/1-2"
            else            -> "/dashboard"
        }
        3 -> when (nombre) {
            "Principal 2.1" -> "/principal/2-1"
            "Principal 2.2" -> "/principal/2-2"
            else            -> "/dashboard"
        }
        else -> "/dashboard"
    }

    // ── Ruta segura para módulos de Seguridad ────────
    private fun rutaModulo(nombre: String): String = when (nombre) {
        "Perfil"          -> "/seguridad/perfil"
        "Módulo"          -> "/seguridad/modulo"
        "Permisos-Perfil" -> "/seguridad/permisos-perfil"
        "Usuario"         -> "/seguridad/usuario"
        else              -> "/dashboard"
    }

    // ── Icono según módulo y menú ────────────────────
    private fun iconoModulo(nombre: String, idMenu: Int): String = when {
        idMenu == 1 -> when (nombre) {
            "Perfil"          -> "fas fa-id-card"
            "Módulo"          -> "fas fa-cubes"
            "Permisos-Perfil" -> "fas fa-key"
            "Usuario"         -> "fas fa-users"
            else              -> "fas fa-circle"
        }
        idMenu == 2 -> "fas fa-th-large"
        idMenu == 3 -> "fas fa-th"
        else        -> "fas fa-circle"
    }

    // ── Color según menú ─────────────────────────────
    private fun colorModulo(idMenu: Int): String = when (idMenu) {
        1    -> "bg-primary bg-opacity-10 text-primary"
        2    -> "bg-success bg-opacity-10 text-success"
        3    -> "bg-warning bg-opacity-10 text-warning"
        else -> "bg-secondary bg-opacity-10 text-secondary"
    }
}