package co.fexe.campusreport.ui

import android.app.Application
import androidx.lifecycle.*
import co.fexe.campusreport.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/** Un movimiento del historial junto con el reporte al que pertenece. */
data class EventoVista(val e: Evento, val r: Reporte)

class ReporteViewModel(app: Application, private val saved: SavedStateHandle) : AndroidViewModel(app) {
    private val dao = AppDb.get(app).reporteDao()
    private val inicio = SharingStarted.WhileSubscribed(5000)

    val reportes: StateFlow<List<Reporte>> = dao.todos().stateIn(viewModelScope, inicio, emptyList())
    val eventos: StateFlow<List<Evento>> = dao.eventos().stateIn(viewModelScope, inicio, emptyList())

    // Filtros de cada pantalla (se conservan al rotar). Cada una tiene los suyos.
    private fun filtros(clave: String) = saved.getStateFlow(clave, Filtros().codigo())
        .map { Filtros.desde(it) }.stateIn(viewModelScope, SharingStarted.Eagerly, Filtros())
    val fReportes = filtros("f_reportes")
    val fGestion = filtros("f_gestion")
    val fHistorial = filtros("f_historial")
    fun setFReportes(f: Filtros) { saved["f_reportes"] = f.codigo() }
    fun setFGestion(f: Filtros) { saved["f_gestion"] = f.codigo() }
    fun setFHistorial(f: Filtros) { saved["f_historial"] = f.codigo() }

    val visiblesReportes: StateFlow<List<Reporte>> =
        combine(reportes, fReportes) { l, f -> l.filter(f::aplica) }.stateIn(viewModelScope, inicio, emptyList())
    val visiblesGestion: StateFlow<List<Reporte>> =
        combine(reportes, fGestion) { l, f -> l.filter(f::aplica) }.stateIn(viewModelScope, inicio, emptyList())
    /** Historial filtrado: el filtro de estado se aplica al estado NUEVO del movimiento. */
    val historial: StateFlow<List<EventoVista>> = combine(eventos, reportes, fHistorial) { ev, rs, f ->
        val porId = rs.associateBy { it.id }
        ev.mapNotNull { e -> porId[e.reporteId]?.let { EventoVista(e, it) } }
            .filter { (f.estado.isEmpty() || it.e.estadoNuevo == f.estado) && f.aplicaSinEstado(it.r) }
    }.stateIn(viewModelScope, inicio, emptyList())

    // Borrador del formulario: sobrevive a rotación y a que el sistema cierre el proceso.
    val categoria = saved.getStateFlow("categoria", "")
    val descripcion = saved.getStateFlow("descripcion", "")
    val ubicacion = saved.getStateFlow("ubicacion", Ubicacion().codigo()) // ver Ubicacion.codigo()

    private val _errores = MutableStateFlow(ErroresForm())
    val errores: StateFlow<ErroresForm> = _errores

    private val _guardando = MutableStateFlow(false)
    val guardando: StateFlow<Boolean> = _guardando

    private val _mensaje = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val mensaje: SharedFlow<String> = _mensaje

    fun setCategoria(v: String) { saved["categoria"] = v; _errores.update { it.copy(categoria = null) } }
    fun setDescripcion(v: String) { saved["descripcion"] = v; _errores.update { it.copy(descripcion = null) } }
    fun setUbicacion(u: Ubicacion) { saved["ubicacion"] = u.codigo(); _errores.update { it.copy(ubicacion = null) } }

    /** Ignora pulsaciones mientras haya un guardado en curso (una intención = un registro). */
    fun guardar(onGuardado: () -> Unit) {
        if (_guardando.value) return
        val ub = Ubicacion.desdeCodigo(ubicacion.value)
        val e = validar(categoria.value, descripcion.value, ub)
        _errores.value = e
        if (e.hayErrores) { _mensaje.tryEmit("No se guardó: revisa los campos marcados."); return }
        _guardando.value = true
        viewModelScope.launch {
            try {
                dao.crear(Reporte(
                    categoria = categoria.value,
                    descripcion = descripcion.value.trim(),
                    ubicacion = ub.texto(),
                    fecha = System.currentTimeMillis()
                ))
                saved["categoria"] = ""; saved["descripcion"] = ""; saved["ubicacion"] = Ubicacion().codigo()
                _mensaje.tryEmit("Reporte guardado en este dispositivo (pendiente de sincronización).")
                onGuardado()
            } finally { _guardando.value = false }
        }
    }

    /** Edita un reporte existente (conserva fecha y estado). Devuelve los errores de validación. */
    fun editar(r: Reporte, cat: String, desc: String, ubi: Ubicacion, onOk: () -> Unit): ErroresForm {
        val e = validar(cat, desc, ubi)
        if (e.hayErrores) return e
        viewModelScope.launch {
            dao.actualizar(r.copy(categoria = cat, descripcion = desc.trim(), ubicacion = ubi.texto()))
            _mensaje.tryEmit("Cambios guardados en este dispositivo.")
            onOk()
        }
        return e
    }

    fun borrar(id: Long, onOk: () -> Unit) {
        viewModelScope.launch {
            dao.eliminar(id)   // el historial del reporte se borra en cascada
            _mensaje.tryEmit("Reporte eliminado de este dispositivo.")
            onOk()
        }
    }

    /** Cambia el estado y registra el movimiento en el historial. */
    fun cambiarEstado(id: Long, nuevo: String, nota: String, onOk: () -> Unit = {}) {
        viewModelScope.launch {
            val cambio = dao.cambiarEstado(id, nuevo, nota.trim(), System.currentTimeMillis())
            _mensaje.tryEmit(if (cambio) "Estado cambiado a «$nuevo»." else "El reporte ya estaba en «$nuevo».")
            onOk()
        }
    }

    /** Carga (reemplazando los anteriores) los reportes de EJEMPLO con su historial. */
    fun cargarEjemplos() {
        viewModelScope.launch {
            dao.reemplazarEjemplos(Ejemplos.generar(System.currentTimeMillis()))
            _mensaje.tryEmit("${Ejemplos.CANTIDAD} reportes de ejemplo cargados.")
        }
    }

    fun quitarEjemplos() {
        viewModelScope.launch {
            val n = dao.eliminarEjemplos()
            _mensaje.tryEmit("$n reportes de ejemplo eliminados.")
        }
    }
}
