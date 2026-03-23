package com.diana.ComTec.controller

import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class CustomErrorController : ErrorController {

    @RequestMapping("/error")
    fun handleError(request: HttpServletRequest, model: Model): String {
        val status = request.getAttribute("jakarta.servlet.error.status_code") as? Int ?: 500
        val message = when (status) {
            400 -> "Solicitud incorrecta"
            401 -> "No autorizado"
            403 -> "Acceso denegado"
            404 -> "Página no encontrada"
            500 -> "Error interno del servidor"
            else -> "Error inesperado"
        }
        model.addAttribute("status", status)
        model.addAttribute("message", message)
        return "error"
    }
}