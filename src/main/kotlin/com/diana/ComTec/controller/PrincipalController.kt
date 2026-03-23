package com.diana.ComTec.controller

import com.diana.ComTec.repository.PermisosPerfilRepository
import com.diana.ComTec.service.JwtService
import com.diana.ComTec.service.UsuarioService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/principal")
class PrincipalController(
    private val jwtService: JwtService,
    private val usuarioService: UsuarioService,
    private val permisosPerfilRepository: PermisosPerfilRepository
) {

    @GetMapping("/{modulo}")
    fun moduloPrincipal(
        @PathVariable modulo: String,
        request: HttpServletRequest,
        model: Model
    ): String {
        val token = request.cookies?.find { it.name == "jwt_token" }?.value
            ?: return "redirect:/login"

        val username = jwtService.extractUsername(token)
        val perfilId = jwtService.extractPerfilId(token)
        val usuario  = usuarioService.findByUsername(username) ?: return "redirect:/login"

        val permisos = permisosPerfilRepository.findByPerfilId(perfilId)
        val permisosActivos = permisos.filter {
            it.bitAgregar || it.bitEditar || it.bitConsulta || it.bitEliminar || it.bitDetalle
        }

        // Rutas del menú seguridad para la navbar
        val rutasModulo = permisosActivos.associate { p ->
            p.modulo.strNombreModulo to rutaModulo(p.modulo.strNombreModulo)
        }

        val (titulo, breadcrumb, parent) = when (modulo) {
            "1-1" -> Triple("Principal 1.1", "Principal 1.1", "Principal 1")
            "1-2" -> Triple("Principal 1.2", "Principal 1.2", "Principal 1")
            "2-1" -> Triple("Principal 2.1", "Principal 2.1", "Principal 2")
            "2-2" -> Triple("Principal 2.2", "Principal 2.2", "Principal 2")
            else  -> return "redirect:/dashboard"
        }

        model.addAttribute("usuario",        usuario)
        model.addAttribute("permisos",       permisosActivos)
        model.addAttribute("rutasModulo",    rutasModulo)
        model.addAttribute("moduloTitulo",   titulo)
        model.addAttribute("breadcrumbItem", breadcrumb)
        model.addAttribute("parentMenu",     parent)

        return "principal/modulo-estatico"
    }

    private fun rutaModulo(nombre: String): String = when (nombre) {
        "Perfil"          -> "/seguridad/perfil"
        "Módulo"          -> "/seguridad/modulo"
        "Permisos-Perfil" -> "/seguridad/permisos-perfil"
        "Usuario"         -> "/seguridad/usuario"
        else              -> "/dashboard"
    }
}