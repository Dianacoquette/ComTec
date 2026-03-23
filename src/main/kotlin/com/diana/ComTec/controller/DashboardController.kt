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

        // Menú Seguridad: solo módulos con permiso
        val menus = menuRepository.findAll()
        val menuSeguridad = menus
            .filter { it.idMenu == 1 && it.modulo.id in modulosConPermiso }

        // Construir mapa de nombre → ruta para usar en Thymeleaf
        val rutasModulo = menuSeguridad.associate { menu ->
            menu.modulo.strNombreModulo to rutaModulo(menu.modulo.strNombreModulo)
        }

        model.addAttribute("usuario",        usuario)
        model.addAttribute("permisos",       permisosActivos)
        model.addAttribute("menuSeguridad",  menuSeguridad)
        model.addAttribute("rutasModulo",    rutasModulo)

        return "dashboard"
    }

    // Helper: nombre del módulo → ruta segura sin tildes ni espacios
    private fun rutaModulo(nombre: String): String = when (nombre) {
        "Perfil"          -> "/seguridad/perfil"
        "Módulo"          -> "/seguridad/modulo"
        "Permisos-Perfil" -> "/seguridad/permisos-perfil"
        "Usuario"         -> "/seguridad/usuario"
        else              -> "/dashboard"
    }
}