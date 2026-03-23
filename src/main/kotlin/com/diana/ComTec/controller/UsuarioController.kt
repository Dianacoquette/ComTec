package com.diana.ComTec.controller

import com.diana.ComTec.config.obtenerPermisosModulo
import com.diana.ComTec.dto.ApiResponse
import com.diana.ComTec.dto.UsuarioRequest
import com.diana.ComTec.dto.UsuarioResponse
import com.diana.ComTec.repository.PermisosPerfilRepository
import com.diana.ComTec.service.JwtService
import com.diana.ComTec.service.UsuarioService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@Controller
@RequestMapping("/seguridad/usuario")
class UsuarioController(
    private val usuarioService: UsuarioService,
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
        val p = obtenerPermisosModulo(permisos, "Usuario")
        model.addAttribute("pAgregar",  p.agregar)
        model.addAttribute("pEditar",   p.editar)
        model.addAttribute("pEliminar", p.eliminar)
        model.addAttribute("pDetalle",  p.detalle)
        model.addAttribute("pConsulta", p.consulta)
        return "seguridad/usuario-lista"
    }

    @GetMapping("/nuevo")
    fun nuevo(request: HttpServletRequest, model: Model): String {
        val token = request.cookies?.find { it.name == "jwt_token" }?.value
            ?: return "redirect:/login"
        val perfilId = jwtService.extractPerfilId(token)
        val permisos = permisosPerfilRepository.findByPerfilId(perfilId)
        val p = obtenerPermisosModulo(permisos, "Usuario")
        if (!p.agregar) return "redirect:/seguridad/usuario"
        model.addAttribute("modo", "nuevo")
        return "seguridad/usuario-form"
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
        val p = obtenerPermisosModulo(permisos, "Usuario")
        if (!p.editar) return "redirect:/seguridad/usuario"
        model.addAttribute("modo", "editar")
        model.addAttribute("usuarioId", id)
        return "seguridad/usuario-form"
    }

    // ── API: Listar ───────────────────────────────────
    @GetMapping("/api")
    @ResponseBody
    fun listar(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "") buscar: String
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        val resultado = if (buscar.isBlank())
            usuarioService.listar(page)
        else
            usuarioService.buscar(buscar, page)

        val data = mapOf(
            "content"       to resultado.content.map { usuarioService.toResponse(it) },
            "totalPages"    to resultado.totalPages,
            "totalElements" to resultado.totalElements,
            "currentPage"   to resultado.number
        )
        return ResponseEntity.ok(ApiResponse(true, "OK", data))
    }

    // ── API: Obtener por ID ───────────────────────────
    @GetMapping("/api/{id}")
    @ResponseBody
    fun obtener(@PathVariable id: Int): ResponseEntity<ApiResponse<UsuarioResponse>> {
        val usuario = usuarioService.obtener(id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(ApiResponse(true, "OK", usuarioService.toResponse(usuario)))
    }

    // ── API: Crear ────────────────────────────────────
    @PostMapping("/api")
    @ResponseBody
    fun crear(@RequestBody req: UsuarioRequest): ResponseEntity<ApiResponse<UsuarioResponse>> {
        if (req.strNombreUsuario.isBlank())
            return ResponseEntity.badRequest()
                .body(ApiResponse(false, "El nombre de usuario es requerido"))
        if (req.strPwd.isBlank())
            return ResponseEntity.badRequest()
                .body(ApiResponse(false, "La contraseña es requerida"))
        return try {
            val nuevo = usuarioService.crear(req)
            ResponseEntity.ok(ApiResponse(true, "Usuario creado correctamente",
                usuarioService.toResponse(nuevo)))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(ApiResponse(false, e.message ?: "Error al crear"))
        }
    }

    // ── API: Actualizar ───────────────────────────────
    @PutMapping("/api/{id}")
    @ResponseBody
    fun actualizar(
        @PathVariable id: Int,
        @RequestBody req: UsuarioRequest
    ): ResponseEntity<ApiResponse<UsuarioResponse>> {
        if (req.strNombreUsuario.isBlank())
            return ResponseEntity.badRequest()
                .body(ApiResponse(false, "El nombre de usuario es requerido"))
        return try {
            val actualizado = usuarioService.actualizar(id, req)
                ?: return ResponseEntity.notFound().build()
            ResponseEntity.ok(ApiResponse(true, "Usuario actualizado correctamente",
                usuarioService.toResponse(actualizado)))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(ApiResponse(false, e.message ?: "Error al actualizar"))
        }
    }

    // ── API: Eliminar ─────────────────────────────────
    @DeleteMapping("/api/{id}")
    @ResponseBody
    fun eliminar(@PathVariable id: Int): ResponseEntity<ApiResponse<Nothing>> {
        if (!usuarioService.eliminar(id))
            return ResponseEntity.notFound().build()
        return ResponseEntity.ok(ApiResponse(true, "Usuario eliminado correctamente"))
    }

    // ── API: Subir imagen ─────────────────────────────
    @PostMapping("/api/{id}/imagen")
    @ResponseBody
    fun subirImagen(
        @PathVariable id: Int,
        @RequestParam("imagen") file: MultipartFile
    ): ResponseEntity<ApiResponse<Map<String, String>>> {
        if (file.isEmpty)
            return ResponseEntity.badRequest()
                .body(ApiResponse(false, "No se seleccionó ningún archivo"))

        val extension = file.originalFilename?.substringAfterLast('.', "")?.lowercase() ?: ""
        if (extension !in listOf("jpg", "jpeg", "png", "gif", "webp"))
            return ResponseEntity.badRequest()
                .body(ApiResponse(false, "Solo se permiten imágenes (jpg, png, gif, webp)"))

        if (file.size > 5 * 1024 * 1024)
            return ResponseEntity.badRequest()
                .body(ApiResponse(false, "La imagen no debe superar los 5MB"))

        val nombreArchivo = usuarioService.subirImagen(id, file)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(ApiResponse(true, "Imagen subida correctamente",
            mapOf("nombreArchivo" to nombreArchivo,
                "url" to "/uploads/usuarios/$nombreArchivo")))
    }

    // ── API: Listar perfiles (para el select) ─────────
    @GetMapping("/api/perfiles")
    @ResponseBody
    fun listarPerfiles(): ResponseEntity<ApiResponse<List<Map<String, Any>>>> {
        val perfiles = usuarioService.listarPerfiles()
        return ResponseEntity.ok(ApiResponse(true, "OK", perfiles))
    }

    // ── API: Listar estados (para el select) ──────────
    @GetMapping("/api/estados")
    @ResponseBody
    fun listarEstados(): ResponseEntity<ApiResponse<List<Map<String, Any>>>> {
        val estados = usuarioService.listarEstados()
        return ResponseEntity.ok(ApiResponse(true, "OK", estados))
    }
}