package com.example.investigacion01_promodoro.data

import com.example.investigacion01_promodoro.model.Tarea

// Contiene la lógica de negocio de las tareas: agregar, seleccionar,
// completar y eliminar. No conoce nada de la interfaz de usuario.
class TareaManager {

    private val _tareas = mutableListOf<Tarea>()
    private var tareaActivaId: Long? = null
    private var siguienteId: Long = 0L

    fun obtenerTareas(): List<Tarea> = _tareas.toList()

    fun obtenerTareaActivaId(): Long? = tareaActivaId

    // Agrega una tarea nueva. Devuelve false si el texto está vacío o solo tiene espacios.
    fun agregarTarea(texto: String): Boolean {
        val textoLimpio = texto.trim()
        if (textoLimpio.isEmpty()) return false

        val nuevaTarea = Tarea(id = siguienteId, texto = textoLimpio)
        siguienteId++
        _tareas.add(nuevaTarea)
        return true
    }

    // Marca una tarea como la tarea activa para trabajar con el temporizador.
    fun seleccionarTareaActiva(id: Long): Boolean {
        val existe = _tareas.any { it.id == id }
        if (existe) tareaActivaId = id
        return existe
    }

    // Alterna el estado completada/pendiente de una tarea.
    fun alternarCompletada(id: Long): Boolean {
        val indice = _tareas.indexOfFirst { it.id == id }
        if (indice == -1) return false

        val tarea = _tareas[indice]
        _tareas[indice] = tarea.copy(completada = !tarea.completada)
        return true
    }

    // Elimina una tarea. Si era la tarea activa, limpia la selección.
    fun eliminarTarea(id: Long): Boolean {
        val eliminada = _tareas.removeIf { it.id == id }
        if (eliminada && tareaActivaId == id) tareaActivaId = null
        return eliminada
    }
}