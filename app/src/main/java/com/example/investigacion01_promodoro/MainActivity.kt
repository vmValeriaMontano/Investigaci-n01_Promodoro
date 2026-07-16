package com.example.investigacion01_promodoro


import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.investigacion01_promodoro.databinding.ActivityMainBinding
import com.example.investigacion01_promodoro.databinding.ActivityItemTareaBinding
import com.example.investigacion01_promodoro.databinding.ActivityItemHistorialBinding

class MainActivity : AppCompatActivity() {

    // Inicializamos la variable de View Binding para controlar la UI de manera segura
    private lateinit var binding: ActivityMainBinding

    // Listas de datos temporales (Tu equipo las conectará al ViewModel luego)
    private val listaDeTareas = mutableListOf<String>()

    // Lista de prueba para el historial (Agregamos un par de textos de ejemplo para que veas cómo se dibuja)
    private val listaDeHistorial = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflamos el XML principal y configuramos la vista de la pantalla
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuración del botón AGREGAR
        binding.btnAgregarTarea.setOnClickListener {
            val textoTarea = binding.etNuevaTarea.text.toString().trim()

            // REQUISITO: Validar que no se agreguen textos vacíos
            if (textoTarea.isEmpty()) {
                Toast.makeText(this, "Por favor escribe una tarea válida", Toast.LENGTH_SHORT).show()
            } else {
                listaDeTareas.add(textoTarea)
                binding.etNuevaTarea.text.clear() // Limpiamos el campo de texto
                actualizarInterfazTareas()
            }
        }

        // Ejecución inicial para pintar las pantallas vacías
        verificarEstadosVacios()
        actualizarInterfazHistorial() // Dibujamos el historial al iniciar
    }

    // INFLACIÓN DINÁMICA DE TAREAS (Usa ActivityItemTareaBinding)
    private fun actualizarInterfazTareas() {
        // REGLA DE ORO: Limpiar el contenedor antes de dibujar para no duplicar vistas anteriores
        binding.contenedorTareas.removeAllViews()

        // Recorremos los datos para generar las vistas una por una
        for ((index, tarea) in listaDeTareas.withIndex()) {

            // Inflamos dinámicamente el layout individual usando la clase generada real: ActivityItemTareaBinding
            val itemBinding = ActivityItemTareaBinding.inflate(
                LayoutInflater.from(this),
                binding.contenedorTareas,
                false
            )

            // Modificamos el contenido del molde con el texto real
            itemBinding.tvTituloTarea.text = tarea

            // Gestión interactiva del Checkbox (Tachado y Atenuado al completarse)
            itemBinding.cbCompletada.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // Agregamos la bandera de tachado de texto y lo atenuamos
                    itemBinding.tvTituloTarea.paintFlags = itemBinding.tvTituloTarea.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    itemBinding.tvTituloTarea.setTextColor(Color.LTGRAY)

                    // EJEMPLO: Cuando marcas una tarea como completada, simulamos que se va al historial
                    val sesionCompletada = "Sesión completada en: $tarea"
                    if (!listaDeHistorial.contains(sesionCompletada)) {
                        listaDeHistorial.add(sesionCompletada)
                        actualizarInterfazHistorial() // Redibujamos el historial
                    }
                } else {
                    // Quitamos la bandera de tachado y restauramos el color negro
                    itemBinding.tvTituloTarea.paintFlags = itemBinding.tvTituloTarea.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    itemBinding.tvTituloTarea.setTextColor(Color.BLACK)
                }
            }

            // Gestión interactiva de eliminación
            itemBinding.btnEliminar.setOnClickListener {
                listaDeTareas.removeAt(index)
                actualizarInterfazTareas() // Redibuja la interfaz limpia
            }

            // Agregamos físicamente la nueva vista al contenedor del diseño principal
            binding.contenedorTareas.addView(itemBinding.root)
        }

        actualizarResumen()
        verificarEstadosVacios()
    }

    // HISTORIAL DINÁMIC
    private fun actualizarInterfazHistorial() {
        // Limpiamos el contenedor
        binding.contenedorHistorial.removeAllViews()

        // Recorremos la lista del historial
        for (sesion in listaDeHistorial) {

            val historialBinding = ActivityItemHistorialBinding.inflate(
                LayoutInflater.from(this),
                binding.contenedorHistorial,
                false
            )

            // Texto de la sesión realizada (Verifica que activity_item_historial.xml
            // el id sea tvHistorialItem)
            historialBinding.tvHistorialItem.text = "• $sesion"

            // Metemos la fila del historial en el LinearLayout del activity_main.xml
            binding.contenedorHistorial.addView(historialBinding.root)
        }

        actualizarResumen()
        verificarEstadosVacios()
    }

    // Muestra u oculta los mensajes de "estado vacío" cuando no hay datos
    private fun verificarEstadosVacios() {
        if (listaDeTareas.isEmpty()) {
            binding.tvTareasVacias.visibility = View.VISIBLE
        } else {
            binding.tvTareasVacias.visibility = View.GONE
        }

        if (listaDeHistorial.isEmpty()) {
            binding.tvHistorialVacio.visibility = View.VISIBLE
        } else {
            binding.tvHistorialVacio.visibility = View.GONE
        }
    }

    // Actualiza los contadores en tiempo real
    private fun actualizarResumen() {
        val pendientes = listaDeTareas.size
        val completadas = listaDeHistorial.size
        binding.tvResumen.text = "$pendientes pendientes · $completadas sesiones completadas"
    }
}