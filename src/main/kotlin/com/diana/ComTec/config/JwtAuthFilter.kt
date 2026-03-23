package com.diana.ComTec.config

import com.diana.ComTec.service.JwtService
import com.diana.ComTec.service.UsuarioService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(
    private val jwtService: JwtService,
    private val usuarioService: UsuarioService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = extractToken(request)

        if (token != null && jwtService.isTokenValid(token)) {
            val username = jwtService.extractUsername(token)
            val userDetails = usuarioService.loadUserByUsername(username)

            val authToken = UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.authorities
            )
            authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
            SecurityContextHolder.getContext().authentication = authToken
        }

        filterChain.doFilter(request, response)
    }

    // Busca el token primero en Cookie, luego en header Authorization
    private fun extractToken(request: HttpServletRequest): String? {
        val cookie = request.cookies?.find { it.name == "jwt_token" }
        if (cookie != null) return cookie.value

        val header = request.getHeader("Authorization")
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7)
        }
        return null
    }
}