package com.example.investigacion01_promodoro.viewmodel

import androidx.lifecycle.ViewModel
import com.example.investigacion01_promodoro.data.TareaManager
import com.example.investigacion01_promodoro.model.Tarea

// Retiene la lista de tareas para que sobreviva a la rotación de pantalla
// sin perderse ni duplicarse. El TareaManager vive aquí, no en la Activity.
class TareaViewModel : ViewModel() {

    private val tareaManager = TareaManager()

    // Consultas
    fun obtenerTareas(): List<Tarea> = tareaManager.obtenerTareas()
    fun obtenerTareaActivaId(): Long? = tareaManager.obtenerTareaActivaId()

    // Acciones
    fun agregarTarea(texto: String): Boolean =
        tareaManager.agregarTarea(texto)

    fun seleccionarTareaActiva(id: Long): Boolean =
        tareaManager.seleccionarTareaActiva(id)

    fun alternarCompletada(id: Long): Boolean =
        tareaManager.alternarCompletada(id)

    fun eliminarTarea(id: Long): Boolean =
        tareaManager.eliminarTarea(id)
}