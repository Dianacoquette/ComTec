package com.diana.ComTec.controller

import com.diana.ComTec.config.obtenerPermisosModulo
import com.diana.ComTec.repository.MenuRepository
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
    private val permisosPerfilRepository: PermisosPerfilRepository,
    private val menuRepository: MenuRepository
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

        // Rutas para navbar
        val menus = menuRepository.findAll()
        val modulosConPermiso = permisosActivos.map { it.modulo.id }.toSet()

        val menuSeguridad  = menus.filter { it.idMenu == 1 && it.modulo.id in modulosConPermiso }
        val menuPrincipal1 = menus.filter { it.idMenu == 2 && it.modulo.id in modulosConPermiso }
        val menuPrincipal2 = menus.filter { it.idMenu == 3 && it.modulo.id in modulosConPermiso }

        val rutasModulo = menuSeguridad.associate { m ->
            m.modulo.strNombreModulo to rutaModulo(m.modulo.strNombreModulo)
        }

        // Nombre del módulo estático según la ruta
        val nombreModulo = when (modulo) {
            "1-1" -> "Principal 1.1"
            "1-2" -> "Principal 1.2"
            "2-1" -> "Principal 2.1"
            "2-2" -> "Principal 2.2"
            else  -> return "redirect:/dashboard"
        }

        val (titulo, breadcrumb, parent) = when (modulo) {
            "1-1" -> Triple("Principal 1.1", "Principal 1.1", "Principal 1")
            "1-2" -> Triple("Principal 1.2", "Principal 1.2", "Principal 1")
            "2-1" -> Triple("Principal 2.1", "Principal 2.1", "Principal 2")
            "2-2" -> Triple("Principal 2.2", "Principal 2.2", "Principal 2")
            else  -> return "redirect:/dashboard"
        }

        // Obtener permisos específicos del módulo estático
        val p = obtenerPermisosModulo(permisos, nombreModulo)

        // Si no tiene ningún permiso activo en este módulo, redirige al dashboard
        if (!p.agregar && !p.editar && !p.consulta && !p.eliminar && !p.detalle) {
            return "redirect:/dashboard"
        }

        model.addAttribute("usuario",        usuario)
        model.addAttribute("permisos",       permisosActivos)
        model.addAttribute("menuSeguridad",  menuSeguridad)
        model.addAttribute("menuPrincipal1", menuPrincipal1)
        model.addAttribute("menuPrincipal2", menuPrincipal2)
        model.addAttribute("rutasModulo",    rutasModulo)
        model.addAttribute("moduloTitulo",   titulo)
        model.addAttribute("breadcrumbItem", breadcrumb)
        model.addAttribute("parentMenu",     parent)

        // Permisos del módulo estático para ocultar/mostrar botones
        model.addAttribute("pAgregar",  p.agregar)
        model.addAttribute("pEditar",   p.editar)
        model.addAttribute("pEliminar", p.eliminar)
        model.addAttribute("pDetalle",  p.detalle)
        model.addAttribute("pConsulta", p.consulta)

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