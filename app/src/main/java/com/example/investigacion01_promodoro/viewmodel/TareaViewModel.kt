package com.example.investigacion01_promodoro.viewmodel

import androidx.lifecycle.ViewModel
import com.example.investigacion01_promodoro.data.TareaManager
import com.example.investigacion01_promodoro.model.Tarea

class TareaViewModel : ViewModel() {

    private val tareaManager = TareaManager()

    // --- Tareas ---
    fun obtenerTareas(): List<Tarea> = tareaManager.obtenerTareas()
    fun obtenerTareaActivaId(): Long? = tareaManager.obtenerTareaActivaId()

    fun agregarTarea(texto: String): Boolean =
        tareaManager.agregarTarea(texto)

    fun seleccionarTareaActiva(id: Long): Boolean =
        tareaManager.seleccionarTareaActiva(id)

    fun alternarCompletada(id: Long): Boolean =
        tareaManager.alternarCompletada(id)

    fun eliminarTarea(id: Long): Boolean =
        tareaManager.eliminarTarea(id)

    fun cargarTareas(tareasGuardadas: List<Tarea>) =
        tareaManager.cargarTareas(tareasGuardadas)

    // --- Estado del temporizador ---
    // Vive aquí (no en la Activity) para que sobreviva a la rotación de
    // pantalla sin reiniciarse ni duplicarse.
    var tiempoRestanteMillis: Long = 25 * 60 * 1000L
    var temporizadorActivo: Boolean = false
    var momentoDePausa: Long = 0L
}