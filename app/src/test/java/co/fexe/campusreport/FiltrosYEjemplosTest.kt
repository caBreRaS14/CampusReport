package co.fexe.campusreport

import co.fexe.campusreport.data.*
import org.junit.Assert.*
import org.junit.Test

class FiltrosYEjemplosTest {
    private fun rep(cat: String = "Equipos", ub: String = "Edificio 3 · Piso 2 · Salón 203", est: String = Estados.PENDIENTE) =
        Reporte(categoria = cat, descripcion = "x", ubicacion = ub, fecha = 1L, estado = est)

    @Test fun sinFiltrosDejaPasarTodo() = assertTrue(Filtros().aplica(rep()))

    @Test fun filtraPorEstado() {
        assertTrue(Filtros(estado = Estados.RESUELTO).aplica(rep(est = Estados.RESUELTO)))
        assertFalse(Filtros(estado = Estados.RESUELTO).aplica(rep(est = Estados.EN_ATENCION)))
    }

    @Test fun filtraPorCategoria() {
        assertTrue(Filtros(categoria = "Equipos").aplica(rep()))
        assertFalse(Filtros(categoria = "Otro").aplica(rep()))
    }

    @Test fun filtraPorLugarEdificioAreaYOtro() {
        assertTrue(Filtros(lugar = "Edificio 3").aplica(rep()))
        assertFalse(Filtros(lugar = "Edificio 4").aplica(rep()))
        assertTrue(Filtros(lugar = "Biblioteca").aplica(rep(ub = "Biblioteca")))
        assertTrue(Filtros(lugar = "Otro").aplica(rep(ub = "Entrada principal")))
        assertFalse(Filtros(lugar = "Biblioteca").aplica(rep(ub = "Cafetería")))
    }

    @Test fun losFiltrosSeCombinan() {
        val f = Filtros(Estados.RESUELTO, "Equipos", "Edificio 3")
        assertTrue(f.aplica(rep(est = Estados.RESUELTO)))
        assertFalse(f.aplica(rep(est = Estados.RESUELTO, cat = "Otro")))
        assertFalse(f.aplica(rep(est = Estados.PENDIENTE)))
        assertEquals(3, f.activos)
        assertEquals(0, Filtros().activos)
    }

    @Test fun codigoIdaYVuelta() {
        val f = Filtros(Estados.EN_ATENCION, "Conectividad", "Zonas verdes")
        assertEquals(f, Filtros.desde(f.codigo()))
        assertEquals(Filtros(), Filtros.desde(Filtros().codigo()))
    }

    @Test fun opcionesDeLugarCubrenEdificiosYAreas() {
        assertEquals(Campus.EDIFICIOS.size + Campus.AREAS.size + 1, Filtros.LUGARES.size)
        assertTrue("Edificio 6" in Filtros.LUGARES && "Cafetería" in Filtros.LUGARES && "Otro" in Filtros.LUGARES)
    }

    // ---- Datos de ejemplo
    private val ahora = 1_800_000_000_000L
    private val datos = Ejemplos.generar(ahora)

    @Test fun hayLaCantidadPrometidaYEstanMarcadosComoEjemplo() {
        assertEquals(Ejemplos.CANTIDAD, datos.size)
        assertTrue(datos.all { it.first.ejemplo })
    }

    @Test fun elHistorialDeCadaEjemploEsCoherente() {
        datos.forEach { (r, evs) ->
            assertEquals(null, evs.first().estadoAnterior)
            assertEquals(Estados.PENDIENTE, evs.first().estadoNuevo)
            assertEquals(r.estado, evs.last().estadoNuevo)             // el último movimiento = estado actual
            evs.zipWithNext().forEach { (a, b) ->
                assertEquals(a.estadoNuevo, b.estadoAnterior)           // cadena sin saltos
                assertTrue(b.fecha >= a.fecha)                          // fechas no retroceden
            }
            assertTrue(evs.all { it.fecha <= ahora })
        }
    }

    @Test fun losEjemplosPasanLaValidacionYCubrenTodo() {
        datos.forEach { (r, _) ->
            assertFalse(validar(r.categoria, r.descripcion, Ubicacion.desdeTexto(r.ubicacion)).hayErrores)
        }
        assertEquals(CATEGORIAS.toSet(), datos.map { it.first.categoria }.toSet())
        assertEquals(Estados.TODOS.toSet(), datos.map { it.first.estado }.toSet())
        assertTrue(datos.map { Filtros.lugarDe(it.first.ubicacion) }.toSet().size >= 4)
    }

    @Test fun sonDeterministas() = assertEquals(datos, Ejemplos.generar(ahora))
}
