package com.diana.ComTec.controller

import com.diana.ComTec.dto.ApiResponse
import com.diana.ComTec.dto.JwtResponse
import com.diana.ComTec.dto.LoginRequest
import com.diana.ComTec.service.CaptchaService
import com.diana.ComTec.service.JwtService
import com.diana.ComTec.service.UsuarioService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val usuarioService: UsuarioService,
    private val jwtService: JwtService,
    private val captchaService: CaptchaService,
    private val passwordEncoder: PasswordEncoder
) {

    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest,
        response: HttpServletResponse
    ): ResponseEntity<ApiResponse<JwtResponse>> {

        // 1. Validar CAPTCHA
        if (!captchaService.verify(request.captchaToken)) {
            return ResponseEntity.badRequest().body(
                ApiResponse(false, "Captcha inválido, intenta de nuevo")
            )
        }

        // 2. Buscar usuario
        val usuario = usuarioService.findByUsername(request.username)
            ?: return ResponseEntity.status(401).body(
                ApiResponse(false, "Usuario o contraseña incorrectos")
            )

        // 3. Validar contraseña
        if (!passwordEncoder.matches(request.password, usuario.strPwd)) {
            return ResponseEntity.status(401).body(
                ApiResponse(false, "Usuario o contraseña incorrectos")
            )
        }

        // 4. Validar estado activo
        if (usuario.estadoUsuario.id != 1) {
            return ResponseEntity.status(403).body(
                ApiResponse(false, "El usuario se encuentra inactivo")
            )
        }

        // 5. Generar JWT
        val token = jwtService.generateToken(
            usuario.strNombreUsuario,
            usuario.perfil.id,
            usuario.perfil.bitAdministrador
        )

        // 6. Guardar token en cookie HttpOnly
        val cookie = Cookie("jwt_token", token).apply {
            isHttpOnly = true
            path = "/"
            maxAge = 86400
        }
        response.addCookie(cookie)

        return ResponseEntity.ok(
            ApiResponse(
                true,
                "Login exitoso",
                JwtResponse(token, usuario.strNombreUsuario, usuario.perfil.strNombrePerfil)
            )
        )
    }

    @PostMapping("/logout")
    fun logout(response: HttpServletResponse): ResponseEntity<ApiResponse<Nothing>> {
        val cookie = Cookie("jwt_token", "").apply {
            isHttpOnly = true
            path = "/"
            maxAge = 0
        }
        response.addCookie(cookie)
        return ResponseEntity.ok(ApiResponse(true, "Sesión cerrada"))
    }
}