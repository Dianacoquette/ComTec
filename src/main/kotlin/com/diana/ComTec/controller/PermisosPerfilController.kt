package com.diana.ComTec.controller

import com.diana.ComTec.dto.ApiResponse
import com.diana.ComTec.dto.PermisosPerfilResponse
import com.diana.ComTec.service.JwtService
import com.diana.ComTec.service.PermisosPerfilService
import com.diana.ComTec.repository.PermisosPerfilRepository
import com.diana.ComTec.config.obtenerPermisosModulo
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

    // ── Vista lista ───────────────────────────────────
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

    // ── API: Listar módulos con permisos (incluye sin permiso) ──
    @GetMapping("/api")
    @ResponseBody
    fun listar(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "0") perfilFiltro: Int
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        // Si no hay perfil seleccionado, mostrar lista vacía
        // para no cargar todos los perfiles x módulos a la vez
        if (perfilFiltro == 0) {
            val data = mapOf(
                "content"       to emptyList<Any>(),
                "totalPages"    to 0,
                "totalElements" to 0,
                "currentPage"   to 0,
                "sinFiltro"     to true
            )
            return ResponseEntity.ok(ApiResponse(true, "OK", data))
        }

        val resultado = permisosPerfilService.listarModulosConPermisos(perfilFiltro, page)
        return ResponseEntity.ok(ApiResponse(true, "OK", resultado))
    }

    // ── API: Toggle con creación automática si no existe ──
    @PatchMapping("/api/toggle")
    @ResponseBody
    fun togglePermisoConCreacion(
        @RequestBody body: Map<String, Any>
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {

        // Convertir seguro desde Any (puede llegar como Int o Double desde JSON)
        fun toInt(v: Any?): Int = when (v) {
            is Int    -> v
            is Double -> v.toInt()
            is Long   -> v.toInt()
            else      -> 0
        }

        val idPermiso = toInt(body["idPermiso"])
        val idPerfil  = toInt(body["idPerfil"])
        val idModulo  = toInt(body["idModulo"])
        val campo     = body["campo"] as? String
            ?: return ResponseEntity.badRequest()
                .body(ApiResponse(false, "Campo requerido"))
        val valor = body["valor"] as? Boolean
            ?: return ResponseEntity.badRequest()
                .body(ApiResponse(false, "Valor requerido"))

        return try {
            val permiso = if (idPermiso > 0) {
                permisosPerfilService.togglePermiso(idPermiso, campo, valor)
            } else {
                permisosPerfilService.crearDesdeToggle(idPerfil, idModulo, campo, valor)
            }

            if (permiso == null) {
                ResponseEntity.notFound().build()
            } else {
                val data = mapOf(
                    "idPermiso"   to permiso.id,
                    "bitAgregar"  to permiso.bitAgregar,
                    "bitEditar"   to permiso.bitEditar,
                    "bitConsulta" to permiso.bitConsulta,
                    "bitEliminar" to permiso.bitEliminar,
                    "bitDetalle"  to permiso.bitDetalle
                )
                ResponseEntity.ok(ApiResponse(true, "Permiso actualizado", data))
            }
        } catch (e: Exception) {
            ResponseEntity.badRequest()
                .body(ApiResponse(false, e.message ?: "Error al actualizar"))
        }
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

    // ── API: Actualizar un permiso específico (toggle) ─
    @PatchMapping("/api/{id}/toggle")
    @ResponseBody
    fun togglePermiso(
        @PathVariable id: Int,
        @RequestBody body: Map<String, Any>
    ): ResponseEntity<ApiResponse<PermisosPerfilResponse>> {
        val campo = body["campo"] as? String
            ?: return ResponseEntity.badRequest()
                .body(ApiResponse(false, "Campo requerido"))
        val valor = body["valor"] as? Boolean
            ?: return ResponseEntity.badRequest()
                .body(ApiResponse(false, "Valor requerido"))

        val actualizado = permisosPerfilService.togglePermiso(id, campo, valor)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(
            ApiResponse(true, "Permiso actualizado", permisosPerfilService.toResponse(actualizado))
        )
    }

    // ── API: Eliminar ─────────────────────────────────
    @DeleteMapping("/api/{id}")
    @ResponseBody
    fun eliminar(@PathVariable id: Int): ResponseEntity<ApiResponse<Nothing>> {
        if (!permisosPerfilService.eliminar(id))
            return ResponseEntity.notFound().build()
        return ResponseEntity.ok(ApiResponse(true, "Permiso eliminado correctamente"))
    }

    // ── API: Catálogos ────────────────────────────────
    @GetMapping("/api/modulos")
    @ResponseBody
    fun listarModulos() =
        ResponseEntity.ok(ApiResponse(true, "OK", permisosPerfilService.listarModulos()))

    @GetMapping("/api/perfiles")
    @ResponseBody
    fun listarPerfiles() =
        ResponseEntity.ok(ApiResponse(true, "OK", permisosPerfilService.listarPerfiles()))
}