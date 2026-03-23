package com.diana.ComTec.controller

import com.diana.ComTec.config.obtenerPermisosModulo
import com.diana.ComTec.dto.ApiResponse
import com.diana.ComTec.dto.PermisosPerfilRequest
import com.diana.ComTec.dto.PermisosPerfilResponse
import com.diana.ComTec.repository.PermisosPerfilRepository
import com.diana.ComTec.service.JwtService
import com.diana.ComTec.service.PermisosPerfilService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/seguridad/permisos-perfil")
class PermisosPerfilController(
    private val permisosPerfilService: PermisosPerfilService,
    private val jwtService: JwtService,
    private val permisosPerfilRepository: PermisosPerfilRepository
) {

    @GetMapping
    fun lista(request: HttpServletRequest, model: Model): String {
        val token = request.cookies?.find { it.name == "jwt_token" }?.value
            ?: return "redirect:/login"
        val perfilId = jwtService.extractPerfilId(token)
        val permisos = permisosPerfilRepository.findByPerfilId(perfilId)
        val p = obtenerPermisosModulo(permisos, "Permisos-Perfil")
        model.addAttribute("pAgregar",  p.agregar)
        model.addAttribute("pEditar",   p.editar)
        model.addAttribute("pEliminar", p.eliminar)
        model.addAttribute("pDetalle",  p.detalle)
        model.addAttribute("pConsulta", p.consulta)
        return "seguridad/permisos-perfil-lista"
    }

    @GetMapping("/nuevo")
    fun nuevo(request: HttpServletRequest, model: Model): String {
        val token = request.cookies?.find { it.name == "jwt_token" }?.value
            ?: return "redirect:/login"
        val perfilId = jwtService.extractPerfilId(token)
        val permisos = permisosPerfilRepository.findByPerfilId(perfilId)
        val p = obtenerPermisosModulo(permisos, "Permisos-Perfil")
        if (!p.agregar) return "redirect:/seguridad/permisos-perfil"
        model.addAttribute("modo", "nuevo")
        return "seguridad/permisos-perfil-form"
    }

    @GetMapping("/editar/{id}")
    fun editar(
        @PathVariable id: Int,
        request: HttpServletRequest,
        model: Model
    ): String {
        val token = request.cookies?.find { it.name == "jwt_token" }?.value
            ?: return "redirect:/login"
        val perfilId = jwtService.extractPerfilId(token)
        val permisos = permisosPerfilRepository.findByPerfilId(perfilId)
        val p = obtenerPermisosModulo(permisos, "Permisos-Perfil")
        if (!p.editar) return "redirect:/seguridad/permisos-perfil"
        model.addAttribute("modo", "editar")
        model.addAttribute("permisoId", id)
        return "seguridad/permisos-perfil-form"
    }

    // ── API: Listar con paginado (SIN filtrado) ───────
    @GetMapping("/api")
    @ResponseBody
    fun listar(
        @RequestParam(defaultValue = "0") page: Int
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        val resultado = permisosPerfilService.listar(page)
        val data = mapOf(
            "content"       to resultado.content.map { permisosPerfilService.toResponse(it) },
            "totalPages"    to resultado.totalPages,
            "totalElements" to resultado.totalElements,
            "currentPage"   to resultado.number
        )
        return ResponseEntity.ok(ApiResponse(true, "OK", data))
    }

    // ── API: Obtener por ID ───────────────────────────
    @GetMapping("/api/{id}")
    @ResponseBody
    fun obtener(@PathVariable id: Int): ResponseEntity<ApiResponse<PermisosPerfilResponse>> {
        val permiso = permisosPerfilService.obtener(id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(
            ApiResponse(true, "OK", permisosPerfilService.toResponse(permiso))
        )
    }

    // ── API: Crear ────────────────────────────────────
    @PostMapping("/api")
    @ResponseBody
    fun crear(
        @RequestBody req: PermisosPerfilRequest
    ): ResponseEntity<ApiResponse<PermisosPerfilResponse>> {
        if (req.idModulo == 0 || req.idPerfil == 0)
            return ResponseEntity.badRequest()
                .body(ApiResponse(false, "Módulo y Perfil son requeridos"))
        return try {
            val nuevo = permisosPerfilService.crear(req)
            ResponseEntity.ok(
                ApiResponse(true, "Permiso creado correctamente",
                    permisosPerfilService.toResponse(nuevo))
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest()
                .body(ApiResponse(false, e.message ?: "Error al crear"))
        }
    }

    // ── API: Actualizar ───────────────────────────────
    @PutMapping("/api/{id}")
    @ResponseBody
    fun actualizar(
        @PathVariable id: Int,
        @RequestBody req: PermisosPerfilRequest
    ): ResponseEntity<ApiResponse<PermisosPerfilResponse>> {
        if (req.idModulo == 0 || req.idPerfil == 0)
            return ResponseEntity.badRequest()
                .body(ApiResponse(false, "Módulo y Perfil son requeridos"))
        return try {
            val actualizado = permisosPerfilService.actualizar(id, req)
                ?: return ResponseEntity.notFound().build()
            ResponseEntity.ok(
                ApiResponse(true, "Permiso actualizado correctamente",
                    permisosPerfilService.toResponse(actualizado))
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest()
                .body(ApiResponse(false, e.message ?: "Error al actualizar"))
        }
    }

    // ── API: Eliminar ─────────────────────────────────
    @DeleteMapping("/api/{id}")
    @ResponseBody
    fun eliminar(@PathVariable id: Int): ResponseEntity<ApiResponse<Nothing>> {
        if (!permisosPerfilService.eliminar(id))
            return ResponseEntity.notFound().build()
        return ResponseEntity.ok(ApiResponse(true, "Permiso eliminado correctamente"))
    }

    // ── API: Catálogos para selects ───────────────────
    @GetMapping("/api/modulos")
    @ResponseBody
    fun listarModulos() =
        ResponseEntity.ok(ApiResponse(true, "OK", permisosPerfilService.listarModulos()))

    @GetMapping("/api/perfiles")
    @ResponseBody
    fun listarPerfiles() =
        ResponseEntity.ok(ApiResponse(true, "OK", permisosPerfilService.listarPerfiles()))
}