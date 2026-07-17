package com.example.investigacion01_promodoro.data

import com.example.investigacion01_promodoro.model.Tarea

class TareaManager {

    private val _tareas = mutableListOf<Tarea>()
    private var tareaActivaId: Long? = null
    private var siguienteId: Long = 0L

    fun obtenerTareas(): List<Tarea> = _tareas.toList()

    fun obtenerTareaActivaId(): Long? = tareaActivaId

    fun agregarTarea(texto: String): Boolean {
        val textoLimpio = texto.trim()
        if (textoLimpio.isEmpty()) return false

        val nuevaTarea = Tarea(id = siguienteId, texto = textoLimpio)
        siguienteId++
        _tareas.add(nuevaTarea)
        return true
    }

    fun seleccionarTareaActiva(id: Long): Boolean {
        val existe = _tareas.any { it.id == id }
        if (existe) tareaActivaId = id
        return existe
    }

    fun alternarCompletada(id: Long): Boolean {
        val indice = _tareas.indexOfFirst { it.id == id }
        if (indice == -1) return false

        val tarea = _tareas[indice]
        _tareas[indice] = tarea.copy(completada = !tarea.completada)
        return true
    }

    fun eliminarTarea(id: Long): Boolean {
        val eliminada = _tareas.removeIf { it.id == id }
        if (eliminada && tareaActivaId == id) tareaActivaId = null
        return eliminada
    }

    fun cargarTareas(tareasGuardadas: List<Tarea>) {
        _tareas.clear()
        _tareas.addAll(tareasGuardadas)
        siguienteId = (tareasGuardadas.maxOfOrNull { it.id } ?: -1L) + 1
    }
}