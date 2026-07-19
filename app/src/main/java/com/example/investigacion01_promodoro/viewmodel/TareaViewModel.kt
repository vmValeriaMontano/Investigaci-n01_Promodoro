package com.example.investigacion01_promodoro.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.investigacion01_promodoro.data.TareaManager
import com.example.investigacion01_promodoro.model.Tarea

class TareaViewModel : ViewModel() {

    private val tareaManager = TareaManager()

    // Creamos un LiveData para el texto de la tarea activa que la Activity pueda observar
    private val _tareaActivaTexto = MutableLiveData<String>("Sin tarea activa seleccionada")
    val tareaActivaTexto: LiveData<String> get() = _tareaActivaTexto

    // --- Tareas ---
    fun obtenerTareas(): List<Tarea> = tareaManager.obtenerTareas()
    fun obtenerTareaActivaId(): Long? = tareaManager.obtenerTareaActivaId()

    fun agregarTarea(texto: String): Boolean =
        tareaManager.agregarTarea(texto)

    fun seleccionarTareaActiva(id: Long): Boolean {
        val exito = tareaManager.seleccionarTareaActiva(id)
        if (exito) {
            actualizarTextoTareaActiva()
        }
        return exito
    }

    fun alternarCompletada(id: Long): Boolean =
        tareaManager.alternarCompletada(id)

    fun eliminarTarea(id: Long): Boolean {
        val exito = tareaManager.eliminarTarea(id)
        // Si eliminamos la tarea que estaba activa, reiniciamos el texto
        if (tareaManager.obtenerTareaActivaId() == null) {
            _tareaActivaTexto.value = "Sin tarea activa seleccionada"
        }
        return exito
    }

    fun cargarTareas(tareasGuardadas: List<Tarea>) {
        tareaManager.cargarTareas(tareasGuardadas)
        actualizarTextoTareaActiva()
    }

    // Función interna para buscar el nombre de la tarea activa y actualizar el LiveData
    private fun actualizarTextoTareaActiva() {
        val activaId = tareaManager.obtenerTareaActivaId()
        val tareaActiva = tareaManager.obtenerTareas().find { it.id == activaId }
        _tareaActivaTexto.value = tareaActiva?.texto ?: "Sin tarea activa seleccionada"
    }

    // --- Estado del temporizador ---
    var tiempoRestanteMillis: Long = 25 * 60 * 1000L
    var temporizadorActivo: Boolean = false
    var momentoDePausa: Long = 0L
}