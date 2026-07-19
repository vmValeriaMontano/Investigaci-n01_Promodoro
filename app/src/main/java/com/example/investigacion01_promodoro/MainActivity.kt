package com.example.investigacion01_promodoro

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.investigacion01_promodoro.databinding.ActivityMainBinding
import com.example.investigacion01_promodoro.databinding.ActivityItemTareaBinding
import com.example.investigacion01_promodoro.databinding.ActivityItemHistorialBinding
import com.example.investigacion01_promodoro.model.Tarea
import com.example.investigacion01_promodoro.viewmodel.TareaViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // El ViewModel retiene las tareas Y el estado del temporizador para
    // que sobrevivan a la rotación de pantalla
    private val tareaViewModel: TareaViewModel by viewModels()

    private val listaDeHistorial = mutableListOf<String>()

    // El objeto CountDownTimer en sí NO se puede guardar en el ViewModel
    // (no sobrevive rotación), por eso se recrea cada vez usando el
    // tiempo restante que sí está guardado en el ViewModel.
    private val duracionTotalMillis = 25 * 60 * 1000L // 25 minutos
    private var temporizador: CountDownTimer? = null

    private var tareaActivaTexto: String? = null
    private val CANAL_ID = "pomodoro_channel"
    private val solicitarPermisoNotificaciones =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private val gson = Gson()
    private val PREFS_NAME = "pomodoro_prefs"
    private val KEY_TAREAS = "key_tareas"
    private val KEY_HISTORIAL = "key_historial"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        crearCanalNotificacion()
        pedirPermisoNotificaciones()

        // Solo cargamos desde disco si el ViewModel todavía no tiene nada
        // (primera vez que se abre la app, no venimos de rotar pantalla)
        if (tareaViewModel.obtenerTareas().isEmpty()) {
            cargarDatos()
        }

        // OBSERVADOR DEL VIEWMODEL: Escucha y restaura automáticamente la tarea activa al rotar la pantalla
        tareaViewModel.tareaActivaTexto.observe(this) { texto ->
            if (texto == "Sin tarea activa seleccionada") {
                tareaActivaTexto = null
                binding.tvTareaActiva.text = texto
            } else {
                tareaActivaTexto = texto
                binding.tvTareaActiva.text = "Enfocado en: $texto"
            }
        }

        binding.btnAgregarTarea.setOnClickListener {
            val textoTarea = binding.etNuevaTarea.text.toString().trim()
            if (textoTarea.isEmpty()) {
                Toast.makeText(this, "Por favor escribe una tarea válida", Toast.LENGTH_SHORT).show()
            } else {
                val agregada = tareaViewModel.agregarTarea(textoTarea)
                if (agregada) {
                    binding.etNuevaTarea.text.clear()
                    actualizarInterfazTareas()
                }
            }
        }

        binding.btnStart.setOnClickListener { iniciarTemporizador() }
        binding.btnPause.setOnClickListener { pausarTemporizador() }
        binding.btnResume.setOnClickListener { reanudarTemporizador() }
        binding.btnReset.setOnClickListener { reiniciarTemporizador() }

        actualizarInterfazTareas()
        actualizarTextoTimer() // refleja el tiempo guardado en el ViewModel (importante tras rotar)
        verificarEstadosVacios()
        actualizarInterfazHistorial()
    }


    override fun onStart() {
        super.onStart()
        android.util.Log.d("MainActivity_CicloVida", "onStart: la actividad se vuelve visible para el usuario")

        // INICIALIZACIÓN DE RECURSO: si el temporizador estaba corriendo antes
        // de que la Activity se detuviera (onStop), lo recreamos aquí usando
        // el tiempo real transcurrido mientras estuvo fuera.
        if (tareaViewModel.temporizadorActivo && tareaViewModel.momentoDePausa != 0L) {
            val tiempoTranscurridoReal = System.currentTimeMillis() - tareaViewModel.momentoDePausa
            tareaViewModel.tiempoRestanteMillis -= tiempoTranscurridoReal
            tareaViewModel.momentoDePausa = 0L

            if (tareaViewModel.tiempoRestanteMillis > 0) {
                iniciarTemporizador()
            } else {
                tareaViewModel.tiempoRestanteMillis = 0
                tareaViewModel.temporizadorActivo = false
                actualizarTextoTimer()
                registrarSesionCompletada()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        android.util.Log.d("MainActivity_CicloVida", "onResume: la actividad pasa a primer plano e interactúa con el usuario")
    }

    override fun onPause() {
        super.onPause()
        android.util.Log.d("MainActivity_CicloVida", "onPause: la actividad deja de estar en primer plano")
        // No se libera ningún recurso aquí a propósito: este callback es
        // demasiado breve para garantizar que una operación termine antes
        // de que el sistema continúe (ver justificación en el README).
    }

    override fun onStop() {
        super.onStop()
        android.util.Log.d("MainActivity_CicloVida", "onStop: la actividad ya no es visible para el usuario")

        guardarDatos()

        // LIBERACIÓN DE RECURSO: cancelamos el CountDownTimer para no dejarlo
        // corriendo innecesariamente mientras la app no es visible. Guardamos
        // el momento exacto para poder recalcular el tiempo real en onStart().
        if (tareaViewModel.temporizadorActivo) {
            temporizador?.cancel()
            tareaViewModel.momentoDePausa = System.currentTimeMillis()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        android.util.Log.d("MainActivity_CicloVida", "onDestroy: la actividad va a ser destruida")
    }


    private fun actualizarInterfazTareas() {
        binding.contenedorTareas.removeAllViews()

        val tareas = tareaViewModel.obtenerTareas()

        for (tarea in tareas) {
            val itemBinding = ActivityItemTareaBinding.inflate(
                LayoutInflater.from(this),
                binding.contenedorTareas,
                false
            )

            itemBinding.tvTituloTarea.text = tarea.texto
            itemBinding.cbCompletada.setOnCheckedChangeListener(null)
            itemBinding.cbCompletada.isChecked = tarea.completada
            aplicarEstiloCompletada(itemBinding, tarea.completada)

            itemBinding.root.setOnClickListener {
                tareaViewModel.seleccionarTareaActiva(tarea.id)
            }

            itemBinding.cbCompletada.setOnCheckedChangeListener { _, isChecked ->
                tareaViewModel.alternarCompletada(tarea.id)
                aplicarEstiloCompletada(itemBinding, isChecked)

                if (isChecked) {
                    val sesionCompletada = "Sesión completada en: ${tarea.texto}"
                    if (!listaDeHistorial.contains(sesionCompletada)) {
                        listaDeHistorial.add(sesionCompletada)
                        actualizarInterfazHistorial()
                    }
                }
            }

            itemBinding.btnEliminar.setOnClickListener {
                tareaViewModel.eliminarTarea(tarea.id)
                actualizarInterfazTareas()
            }

            binding.contenedorTareas.addView(itemBinding.root)
        }

        actualizarResumen()
        verificarEstadosVacios()
    }

    private fun aplicarEstiloCompletada(itemBinding: ActivityItemTareaBinding, completada: Boolean) {
        if (completada) {
            itemBinding.tvTituloTarea.paintFlags = itemBinding.tvTituloTarea.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            itemBinding.tvTituloTarea.setTextColor(Color.LTGRAY)
        } else {
            itemBinding.tvTituloTarea.paintFlags = itemBinding.tvTituloTarea.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            itemBinding.tvTituloTarea.setTextColor(Color.BLACK)
        }
    }


    private fun actualizarInterfazHistorial() {
        binding.contenedorHistorial.removeAllViews()

        for (sesion in listaDeHistorial) {
            val historialBinding = ActivityItemHistorialBinding.inflate(
                LayoutInflater.from(this),
                binding.contenedorHistorial,
                false
            )
            historialBinding.tvHistorialItem.text = "• $sesion"
            binding.contenedorHistorial.addView(historialBinding.root)
        }

        actualizarResumen()
        verificarEstadosVacios()
    }

    private fun verificarEstadosVacios() {
        binding.tvTareasVacias.visibility =
            if (tareaViewModel.obtenerTareas().isEmpty()) View.VISIBLE else View.GONE
        binding.tvHistorialVacio.visibility =
            if (listaDeHistorial.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun actualizarResumen() {
        val pendientes = tareaViewModel.obtenerTareas().size
        val completadas = listaDeHistorial.size
        binding.tvResumen.text = "$pendientes pendientes · $completadas sesiones completadas"
    }


    private fun iniciarTemporizador() {
        temporizador = object : CountDownTimer(tareaViewModel.tiempoRestanteMillis, 1000) {
            override fun onTick(millisRestantes: Long) {
                tareaViewModel.tiempoRestanteMillis = millisRestantes
                actualizarTextoTimer()
            }
            override fun onFinish() {
                tareaViewModel.tiempoRestanteMillis = 0
                actualizarTextoTimer()
                tareaViewModel.temporizadorActivo = false
                registrarSesionCompletada()
            }
        }.start()
        tareaViewModel.temporizadorActivo = true
    }

    private fun pausarTemporizador() {
        temporizador?.cancel()
        tareaViewModel.temporizadorActivo = false
    }

    private fun reanudarTemporizador() {
        if (!tareaViewModel.temporizadorActivo && tareaViewModel.tiempoRestanteMillis > 0) {
            iniciarTemporizador()
        }
    }

    private fun reiniciarTemporizador() {
        temporizador?.cancel()
        tareaViewModel.tiempoRestanteMillis = duracionTotalMillis
        tareaViewModel.temporizadorActivo = false
        actualizarTextoTimer()
    }

    private fun actualizarTextoTimer() {
        val minutos = (tareaViewModel.tiempoRestanteMillis / 1000) / 60
        val segundos = (tareaViewModel.tiempoRestanteMillis / 1000) % 60
        binding.tvTimer.text = String.format("%02d:%02d", minutos, segundos)
    }

    // ==================== TAREA ACTIVA + NOTIFICACIONES ====================

    private fun crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL_ID,
                "Sesiones Pomodoro",
                NotificationManager.IMPORTANCE_HIGH
            )
            canal.description = "Avisa cuando termina una sesión de enfoque"
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
        }
    }

    private fun pedirPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val yaTienePermiso = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!yaTienePermiso) {
                solicitarPermisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun registrarSesionCompletada() {
        val tarea = tareaActivaTexto ?: "una tarea sin nombre"
        val hora = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val sesion = "Sesión completada: $tarea — $hora"

        listaDeHistorial.add(sesion)
        actualizarInterfazHistorial()
        mostrarNotificacionFinalizado(tarea)
    }

    private fun mostrarNotificacionFinalizado(tarea: String) {
        val notificacion = NotificationCompat.Builder(this, CANAL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("¡Sesión terminada!")
            .setContentText("Completaste una sesión de: $tarea")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val yaTienePermiso = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

        if (yaTienePermiso) {
            NotificationManagerCompat.from(this).notify(1, notificacion)
        }
    }


    private fun guardarDatos() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val jsonTareas = gson.toJson(tareaViewModel.obtenerTareas())
        val jsonHistorial = gson.toJson(listaDeHistorial)
        prefs.edit()
            .putString(KEY_TAREAS, jsonTareas)
            .putString(KEY_HISTORIAL, jsonHistorial)
            .apply()
    }

    private fun cargarDatos() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val jsonTareas = prefs.getString(KEY_TAREAS, null)
        val jsonHistorial = prefs.getString(KEY_HISTORIAL, null)

        try {
            if (jsonTareas != null) {
                val tipoListaTareas = object : TypeToken<List<Tarea>>() {}.type
                val tareasGuardadas: List<Tarea> = gson.fromJson(jsonTareas, tipoListaTareas)
                tareaViewModel.cargarTareas(tareasGuardadas)
            }

            if (jsonHistorial != null) {
                val tipoListaHistorial = object : TypeToken<MutableList<String>>() {}.type
                val historialGuardado: MutableList<String> = gson.fromJson(jsonHistorial, tipoListaHistorial)
                listaDeHistorial.clear()
                listaDeHistorial.addAll(historialGuardado)
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "No se pudieron cargar datos guardados, se ignoran", e)
        }
    }
}