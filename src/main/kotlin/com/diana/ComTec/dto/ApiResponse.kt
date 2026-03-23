package com.diana.ComTec.dto

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)