package co.fexe.campusreport

import android.app.Application
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.fexe.campusreport.data.*
import co.fexe.campusreport.ui.ReporteViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomYViewModelTest {
    private val ctx: Context = ApplicationProvider.getApplicationContext()

    @Before fun limpiar() = runBlocking<Unit> { AppDb.get(ctx).clearAllTables() }

    private fun nuevo(c: String = "Equipos", d: String = "Proyector dañado", u: String = "Bloque A 101") =
        Reporte(categoria = c, descripcion = d, ubicacion = u, fecha = 1L)

    /** P3 a nivel de datos: lo insertado sigue ahí tras cerrar y reabrir la base de archivo. */
    @Test fun persistenciaTrasCerrarYReabrir() = runBlocking<Unit> {
        ctx.deleteDatabase("test_persist.db")
        var db = Room.databaseBuilder(ctx, AppDb::class.java, "test_persist.db").build()
        db.reporteDao().insertar(nuevo())
        db.close()
        db = Room.databaseBuilder(ctx, AppDb::class.java, "test_persist.db").build()
        assertEquals(1, db.reporteDao().contar())
        db.close(); ctx.deleteDatabase("test_persist.db")
    }

    @Test fun idsNoSeRepiten() = runBlocking<Unit> {
        val dao = AppDb.get(ctx).reporteDao()
        val a = dao.insertar(nuevo()); val b = dao.insertar(nuevo())
        assertNotEquals(a, b)
    }

    @Test fun editarConservaFechaYEstado() = runBlocking<Unit> {
        val dao = AppDb.get(ctx).reporteDao()
        val id = dao.insertar(nuevo())
        val vm = ReporteViewModel(ctx as Application, SavedStateHandle())
        val orig = nuevo().copy(id = id)
        val e = vm.editar(orig, "Otro", "  Texto nuevo  ", Ubicacion(Campus.EDIFICIO, 3, 2, 203)) {}
        assertFalse(e.hayErrores)
        Thread.sleep(500)
        val r = dao.todosSync().first()
        assertEquals("Otro", r.categoria); assertEquals("Texto nuevo", r.descripcion)
        assertEquals(1L, r.fecha); assertEquals(ESTADO_INICIAL, r.estado)
        assertEquals("Edificio 3 · Piso 2 · Salón 203", r.ubicacion)
    }

    @Test fun editarConDatosInvalidosNoCambiaNada() = runBlocking<Unit> {
        val dao = AppDb.get(ctx).reporteDao()
        val id = dao.insertar(nuevo())
        val vm = ReporteViewModel(ctx as Application, SavedStateHandle())
        val e = vm.editar(nuevo().copy(id = id), "Equipos", "   ", Ubicacion()) {}
        assertTrue(e.hayErrores)
        Thread.sleep(300)
        assertEquals("Proyector dañado", dao.todosSync().first().descripcion)
    }

    @Test fun eliminarBorraSoloEseReporte() = runBlocking<Unit> {
        val dao = AppDb.get(ctx).reporteDao()
        val a = dao.insertar(nuevo()); dao.insertar(nuevo(c = "Otro"))
        dao.eliminar(a)
        assertEquals(1, dao.contar())
    }

    /** P2 en el ViewModel: campos vacíos no crean ningún registro. */
    @Test fun guardarVacioNoInserta() = runBlocking<Unit> {
        val vm = ReporteViewModel(ctx as Application, SavedStateHandle())
        vm.guardar {}
        Thread.sleep(500)
        assertEquals(0, AppDb.get(ctx).reporteDao().contar())
        assertTrue(vm.errores.value.hayErrores)
    }

    /** P7 en el ViewModel: varias pulsaciones seguidas = un solo registro. */
    @Test fun variasPulsacionesCreanUnSoloReporte() = runBlocking<Unit> {
        val vm = ReporteViewModel(ctx as Application, SavedStateHandle())
        vm.setCategoria("Equipos"); vm.setDescripcion("Proyector"); vm.setUbicacion(Ubicacion(lugar = "Biblioteca"))
        repeat(5) { vm.guardar {} }
        esperar { AppDb.get(ctx).reporteDao().contar() >= 1 }
        Thread.sleep(800)
        assertEquals(1, AppDb.get(ctx).reporteDao().contar())
    }

    @Test fun crearRegistraElEventoDeCreacion() = runBlocking<Unit> {
        val dao = AppDb.get(ctx).reporteDao()
        dao.crear(nuevo())
        val ev = dao.eventos().first()
        assertEquals(1, ev.size)
        assertNull(ev[0].estadoAnterior); assertEquals(Estados.PENDIENTE, ev[0].estadoNuevo)
    }

    @Test fun cambiarEstadoDejaMovimientoEnElHistorial() = runBlocking<Unit> {
        val dao = AppDb.get(ctx).reporteDao()
        val id = dao.crear(nuevo())
        val vm = ReporteViewModel(ctx as Application, SavedStateHandle())
        vm.cambiarEstado(id, Estados.EN_ATENCION, "  Técnico asignado ")
        esperar { dao.contarEventos() >= 2 }
        assertEquals(Estados.EN_ATENCION, dao.porId(id)!!.estado)
        val ultimo = dao.eventos().first().first()
        assertEquals(Estados.PENDIENTE, ultimo.estadoAnterior)
        assertEquals(Estados.EN_ATENCION, ultimo.estadoNuevo)
        assertEquals("Técnico asignado", ultimo.nota)
    }

    @Test fun cambiarAlMismoEstadoNoCreaMovimiento() = runBlocking<Unit> {
        val dao = AppDb.get(ctx).reporteDao()
        val id = dao.crear(nuevo())
        assertFalse(dao.cambiarEstado(id, Estados.PENDIENTE, "", 5L))
        assertEquals(1, dao.contarEventos())
    }

    @Test fun eliminarUnReporteBorraSuHistorial() = runBlocking<Unit> {
        val dao = AppDb.get(ctx).reporteDao()
        val a = dao.crear(nuevo()); dao.crear(nuevo(c = "Otro"))
        dao.cambiarEstado(a, Estados.RESUELTO, "", 5L)
        dao.eliminar(a)
        assertEquals(1, dao.contar()); assertEquals(1, dao.contarEventos())
    }

    @Test fun cargarEjemplosCreaReportesConHistorialCoherente() = runBlocking<Unit> {
        val dao = AppDb.get(ctx).reporteDao()
        dao.crear(nuevo())   // un reporte «real» que no debe tocarse
        val vm = ReporteViewModel(ctx as Application, SavedStateHandle())
        vm.cargarEjemplos()
        esperar { dao.contarEjemplos() == Ejemplos.CANTIDAD }
        assertEquals(Ejemplos.CANTIDAD, dao.contarEjemplos())
        assertEquals(Ejemplos.CANTIDAD + 1, dao.contar())
        val reportes = dao.todos().first(); val eventos = dao.eventos().first()
        reportes.filter { it.ejemplo }.forEach { r ->
            val propios = eventos.filter { it.reporteId == r.id }.sortedWith(compareBy({ it.fecha }, { it.id }))
            assertEquals(r.estado, propios.last().estadoNuevo)
        }
        vm.cargarEjemplos()        // volver a cargar no duplica
        esperar { false }
        assertEquals(Ejemplos.CANTIDAD + 1, dao.contar())
        vm.quitarEjemplos()
        esperar { dao.contarEjemplos() == 0 }
        assertEquals(1, dao.contar()); assertEquals(1, dao.contarEventos())
    }

    private fun esperar(cond: suspend () -> Boolean) = runBlocking {
        val fin = System.currentTimeMillis() + 3000
        while (System.currentTimeMillis() < fin && !cond()) Thread.sleep(50)
    }
}

private suspend fun ReporteDao.todosSync(): List<Reporte> =
    todos().first()
