# Investigaci-n01_Promodoro
## Ciclo de vida de la Activity

La pantalla principal de la app (MainActivity) implementa los seis
callbacks del ciclo de vida de Android, cada uno registrado en el Logcat
para poder rastrear en qué momento se dispara cada uno mientras se usa
el temporizador Pomodoro y se gestionan las tareas.

- *onCreate()*: se ejecuta una única vez, al abrir la app por primera vez.
  Aquí se infla la pantalla principal y se preparan los elementos con los
  que el usuario va a interactuar (lista de tareas, temporizador).

- *onStart()*: se dispara justo antes de que la pantalla sea visible para
  el usuario.

- *onResume()*: la pantalla queda en primer plano y el usuario puede
  tocar botones, marcar tareas o controlar el temporizador.

- *onPause()*: se dispara apenas el usuario deja de ver la app en primer
  plano. No se usa para guardar el progreso aquí, porque este callback es
  demasiado breve para garantizar que una operación termine antes de que
  el sistema continúe.

- *onStop()*: se dispara cuando la app deja de ser visible por completo.
  Es el momento adecuado para conservar el progreso de la sesión o de las
  tareas antes de que el usuario abandone la app.

- *onDestroy()*: se dispara antes de que la pantalla sea eliminada de
  memoria, ya sea porque el usuario cerró la app o por un cambio de
  configuración como rotar la pantalla.

## Estado del ViewModel
