package com.diana.ComTec.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class CaptchaService {

    @Value("\${recaptcha.secret}")
    private lateinit var recaptchaSecret: String

    @Value("\${recaptcha.verify.url}")
    private lateinit var verifyUrl: String

    private val restClient = RestClient.create()

    fun verify(token: String): Boolean {
        if (token.isBlank()) return false
        return try {
            val response = restClient.post()
                .uri("$verifyUrl?secret=$recaptchaSecret&response=$token")
                .retrieve()
                .body(Map::class.java)
            response?.get("success") as? Boolean ?: false
        } catch (e: Exception) {
            false
        }
    }
}