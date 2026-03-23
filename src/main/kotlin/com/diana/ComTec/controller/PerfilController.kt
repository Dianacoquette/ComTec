package com.diana.ComTec.controller

import com.diana.ComTec.config.obtenerPermisosModulo
import com.diana.ComTec.dto.ApiResponse
import com.diana.ComTec.model.Perfil
import com.diana.ComTec.repository.PermisosPerfilRepository
import com.diana.ComTec.service.JwtService
import com.diana.ComTec.service.PerfilService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/seguridad/perfil")
class PerfilController(
    private val perfilService: PerfilService,
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
        val p = obtenerPermisosModulo(permisos, "Perfil")
        model.addAttribute("pAgregar",  p.agregar)
        model.addAttribute("pEditar",   p.editar)
        model.addAttribute("pEliminar", p.eliminar)
        model.addAttribute("pDetalle",  p.detalle)
        model.addAttribute("pConsulta", p.consulta)
        return "seguridad/perfil-lista"
    }

    // ── Vista formulario nuevo ────────────────────────
    @GetMapping("/nuevo")
    fun nuevo(request: HttpServletRequest, model: Model): String {
        val token = request.cookies?.find { it.name == "jwt_token" }?.value
            ?: return "redirect:/login"
        val perfilId = jwtService.extractPerfilId(token)
        val permisos = permisosPerfilRepository.findByPerfilId(perfilId)
        val p = obtenerPermisosModulo(permisos, "Perfil")
        if (!p.agregar) return "redirect:/seguridad/perfil"
        model.addAttribute("modo", "nuevo")
        return "seguridad/perfil-form"
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
        val p = obtenerPermisosModulo(permisos, "Perfil")
        if (!p.editar) return "redirect:/seguridad/perfil"
        model.addAttribute("modo", "editar")
        model.addAttribute("perfilId", id)
        return "seguridad/perfil-form"
    }

    // ── API: Listar ───────────────────────────────────
    @GetMapping("/api")
    @ResponseBody
    fun listar(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "") buscar: String
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        val resultado = if (buscar.isBlank())
            perfilService.listar(page)
        else
            perfilService.buscar(buscar, page)
        val data = mapOf(
            "content"       to resultado.content,
            "totalPages"    to resultado.totalPages,
            "totalElements" to resultado.totalElements,
            "currentPage"   to resultado.number
        )
        return ResponseEntity.ok(ApiResponse(true, "OK", data))
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    fun obtener(@PathVariable id: Int): ResponseEntity<ApiResponse<Perfil>> {
        val perfil = perfilService.obtener(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(ApiResponse(true, "OK", perfil))
    }

    @PostMapping("/api")
    @ResponseBody
    fun crear(@RequestBody perfil: Perfil): ResponseEntity<ApiResponse<Perfil>> {
        if (perfil.strNombrePerfil.isBlank())
            return ResponseEntity.badRequest()
                .body(ApiResponse(false, "El nombre del perfil es requerido"))
        return ResponseEntity.ok(ApiResponse(true, "Perfil creado correctamente",
            perfilService.guardar(perfil)))
    }

    @PutMapping("/api/{id}")
    @ResponseBody
    fun actualizar(
        @PathVariable id: Int,
        @RequestBody perfil: Perfil
    ): ResponseEntity<ApiResponse<Perfil>> {
        if (perfil.strNombrePerfil.isBlank())
            return ResponseEntity.badRequest()
                .body(ApiResponse(false, "El nombre del perfil es requerido"))
        val actualizado = perfilService.actualizar(id, perfil)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(ApiResponse(true, "Perfil actualizado correctamente", actualizado))
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    fun eliminar(@PathVariable id: Int): ResponseEntity<ApiResponse<Nothing>> {
        if (!perfilService.eliminar(id)) return ResponseEntity.notFound().build()
        return ResponseEntity.ok(ApiResponse(true, "Perfil eliminado correctamente"))
    }
}