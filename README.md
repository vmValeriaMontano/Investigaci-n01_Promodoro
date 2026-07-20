# Investigaci-n01_Promodoro

## Diseño de Interfaz e Inflación Dinámica (Frontend)

Para el desarrollo de la interfaz de usuario de la aplicación Pomodoro, se adoptó un enfoque modular y dinámico basado en las mejores prácticas de Android:

### Justificación de Componentes Reutilizables
Los diseños individuales de las tareas (`activity_item_tarea.xml`) y del historial (`activity_item_historial.xml`) se separaron en archivos XML independientes debido a los siguientes beneficios técnicos:
* **Principio de Responsabilidad Única:** Evita sobrecargar el archivo `activity_main.xml` con estructuras repetitivas, manteniendo el código limpio y mantenible.
* **Reutilización de Código:** Actúan como moldes genéricos que se clonan en memoria de forma consecutiva según la cantidad de elementos requeridos.
* **Modularidad Estética:** Facilita la modificación o el rediseño visual de una sola fila (por ejemplo, cambiar el color del Checkbox o espaciados) sin alterar el comportamiento o estructura del contenedor principal.

### Justificación de la Inflación Dinámica de Vistas
En lugar de utilizar componentes estáticos, se implementó la **inflación dinámica en tiempo de ejecución** mediante `LayoutInflater` y **View Binding** por las siguientes razones analíticas:
* **Eficiencia de Memoria:** La aplicación no necesita reservar espacio ni renderizar elementos invisibles de forma anticipada. Las vistas se crean e insertan físicamente en los contenedores (`contenedorTareas` y `contenedorHistorial`) únicamente cuando el usuario agrega datos reales.
* **Sincronización Segura de Datos:** Al utilizar View Binding (`ActivityItemTareaBinding` y `ActivityItemHistorialBinding`), se elimina por completo el uso de `findViewById()`, lo que garantiza accesos seguros en tiempo de compilación a los elementos internos (como los contadores, textos y selectores de estado activo) previniendo errores de tipo *NullPointerException*.
* **Persistencia Reactiva ante Rotaciones:** La interfaz responde de forma limpia y reactiva a la arquitectura del proyecto; al combinarse con `LiveData` en el `TareaViewModel`, los textos dinámicos (como la tarea activa seleccionada) se repintan automáticamente tras la destrucción y recreación de la Activity durante un giro de pantalla sin perder el enfoque del usuario.

---
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

**Qué guarda:** TareaViewModel retiene la lista de tareas y el estado
del temporizador (tiempoRestanteMillis, temporizadorActivo,
momentoDePausa), para que ninguno se pierda ni se duplique al rotar
la pantalla.

**Por qué el CountDownTimer no se guarda ahí:** no sobrevive cuando la
Activity se destruye. En su lugar se guarda solo lo necesario para
volver a crearlo: cuánto tiempo quedaba y si estaba corriendo.

**Al pausar (onStop):** si el temporizador está activo, se cancela y
se guarda el momento exacto en que se pausó. Se usa onStop y no
onPause porque este último se dispara con interrupciones muy breves,
como una notificación.

**Al reanudar (onStart):** se calcula cuánto tiempo pasó realmente
desde la pausa y se le resta al tiempo restante antes de volver a
crear el CountDownTimer. Así el conteo sigue siendo exacto aunque el
usuario haya minimizado la app varios minutos.

Esta simetría entre onStop y onStart es lo que evita que el
temporizador se duplique o se desincronice al rotar la pantalla o
minimizar la app.

## Video de explicación

Enlace al video donde se explica la resolución de los 12 pasos:

[Ver video]https://youtu.be/DSA43X4fqow?si=mM-RmP3CQu9gh3g8
