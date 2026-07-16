package com.example.investigacion01_promodoro.model

/**
 * Representa una tarea dentro de la aplicación.
 *
 * @param id Identificador único de la tarea. Se usa para encontrarla,
 *           seleccionarla o eliminarla sin depender de su posición en la lista.
 * @param texto El texto que el usuario escribió para describir la tarea.
 * @param completada Indica si la tarea ya fue marcada como completada
 *                    (se usa para mostrarla tachada/atenuada en la UI).
 */
data class Tarea(
    val id: Long,
    val texto: String,
    val completada: Boolean = false
)