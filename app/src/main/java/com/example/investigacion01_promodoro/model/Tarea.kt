package com.example.investigacion01_promodoro.model

data class Tarea(
    val id: Long,
    val texto: String,
    val completada: Boolean = false
)