package com.diana.ComTec.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import com.diana.ComTec.service.UsuarioService

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthFilter: JwtAuthFilter,
    private val usuarioService: UsuarioService,
    private val passwordEncoder: PasswordEncoder   // ← viene de PasswordConfig
) {

    @Bean
    fun authenticationProvider(): AuthenticationProvider {
        val provider = DaoAuthenticationProvider()
        provider.setUserDetailsService(usuarioService)
        provider.setPasswordEncoder(passwordEncoder)
        return provider
    }

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager =
        config.authenticationManager

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        "/login",
                        "/auth/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/uploads/**",
                        "/favicon.ico",
                        "/error"
                    ).permitAll()
                    .anyRequest().authenticated()
            }
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .exceptionHandling { ex ->
                ex.authenticationEntryPoint { request, response, _ ->
                    val isAjax    = request.getHeader("X-Requested-With") == "XMLHttpRequest"
                    val acceptsJson = request.getHeader("Accept")
                        ?.contains("application/json") == true
                    if (isAjax || acceptsJson) {
                        response.sendError(401, "No autorizado")
                    } else {
                        response.sendRedirect("/login")
                    }
                }
            }

        return http.build()
    }
}