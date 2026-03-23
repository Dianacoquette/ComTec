package com.diana.ComTec.controller

import com.diana.ComTec.config.obtenerPermisosModulo
import com.diana.ComTec.dto.ApiResponse
import com.diana.ComTec.model.Modulo
import com.diana.ComTec.repository.PermisosPerfilRepository
import com.diana.ComTec.service.JwtService
import com.diana.ComTec.service.ModuloService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/seguridad/modulo")
class ModuloController(
    private val moduloService: ModuloService,
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
        val p = obtenerPermisosModulo(permisos, "Módulo")
        model.addAttribute("pAgregar",  p.agregar)
        model.addAttribute("pEditar",   p.editar)
        model.addAttribute("pEliminar", p.eliminar)
        model.addAttribute("pDetalle",  p.detalle)
        model.addAttribute("pConsulta", p.consulta)
        return "seguridad/modulo-lista"
    }

    // ── Vista formulario nuevo ────────────────────────
    @GetMapping("/nuevo")
    fun nuevo(request: HttpServletRequest, model: Model): String {
        val token = request.cookies?.find { it.name == "jwt_token" }?.value
            ?: return "redirect:/login"
        val perfilId = jwtService.extractPerfilId(token)
        val permisos = permisosPerfilRepository.findByPerfilId(perfilId)
        val p = obtenerPermisosModulo(permisos, "Módulo")
        if (!p.agregar) return "redirect:/seguridad/modulo"
        model.addAttribute("modo", "nuevo")
        return "seguridad/modulo-form"
    }

    // ── Vista formulario editar ───────────────────────
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
        val p = obtenerPermisosModulo(permisos, "Módulo")
        if (!p.editar) return "redirect:/seguridad/modulo"
        model.addAttribute("modo", "editar")
        model.addAttribute("moduloId", id)
        return "seguridad/modulo-form"
    }
    // ── API: Listar ───────────────────────────────────
    @GetMapping("/api")
    @ResponseBody
    fun listar(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "") buscar: String
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        val resultado = if (buscar.isBlank())
            moduloService.listar(page)
        else
            moduloService.buscar(buscar, page)

        val data = mapOf(
            "content"       to resultado.content,
            "totalPages"    to resultado.totalPages,
            "totalElements" to resultado.totalElements,
            "currentPage"   to resultado.number
        )
        return ResponseEntity.ok(ApiResponse(true, "OK", data))
    }

    // ── API: Obtener por ID ───────────────────────────
    @GetMapping("/api/{id}")
    @ResponseBody
    fun obtener(@PathVariable id: Int): ResponseEntity<ApiResponse<Modulo>> {
        val modulo = moduloService.obtener(id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(ApiResponse(true, "OK", modulo))
    }

    // ── API: Crear ────────────────────────────────────
    @PostMapping("/api")
    @ResponseBody
    fun crear(@RequestBody modulo: Modulo): ResponseEntity<ApiResponse<Modulo>> {
        if (modulo.strNombreModulo.isBlank())
            return ResponseEntity.badRequest()
                .body(ApiResponse(false, "El nombre del módulo es requerido"))
        return ResponseEntity.ok(
            ApiResponse(true, "Módulo creado correctamente", moduloService.guardar(modulo))
        )
    }

    // ── API: Actualizar ───────────────────────────────
    @PutMapping("/api/{id}")
    @ResponseBody
    fun actualizar(
        @PathVariable id: Int,
        @RequestBody modulo: Modulo
    ): ResponseEntity<ApiResponse<Modulo>> {
        if (modulo.strNombreModulo.isBlank())
            return ResponseEntity.badRequest()
                .body(ApiResponse(false, "El nombre del módulo es requerido"))
        val actualizado = moduloService.actualizar(id, modulo)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(ApiResponse(true, "Módulo actualizado correctamente", actualizado))
    }

    // ── API: Eliminar ─────────────────────────────────
    @DeleteMapping("/api/{id}")
    @ResponseBody
    fun eliminar(@PathVariable id: Int): ResponseEntity<ApiResponse<Nothing>> {
        if (!moduloService.eliminar(id))
            return ResponseEntity.notFound().build()
        return ResponseEntity.ok(ApiResponse(true, "Módulo eliminado correctamente"))
    }
}