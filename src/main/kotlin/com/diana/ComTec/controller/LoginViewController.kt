package com.diana.ComTec.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class LoginViewController {

    @Value("\${recaptcha.site.key}")
    private lateinit var recaptchaSiteKey: String

    @GetMapping("/login")
    fun loginPage(model: Model): String {
        model.addAttribute("recaptchaSiteKey", recaptchaSiteKey)
        return "login"
    }

    @GetMapping("/")
    fun root(): String = "redirect:/login"
}