package co.fexe.campusreport

import co.fexe.campusreport.data.*
import org.junit.Assert.*
import org.junit.Test

class UbicacionTest {
    @Test fun salonesSeNumeranPorPiso() {
        assertEquals((201..210).toList(), Campus.salonesDe(2))
        assertEquals((401..410).toList(), Campus.salonesDe(4))
        assertTrue(405 in Campus.salonesDe(4))
        assertFalse(203 in Campus.salonesDe(4))
    }
    @Test fun textoDeEdificio() =
        assertEquals("Edificio 3 · Piso 2 · Salón 203", Ubicacion(Campus.EDIFICIO, 3, 2, 203).texto())
    @Test fun textoZonaComun() =
        assertEquals("Edificio 1 · Piso 1 · Zona común", Ubicacion(Campus.EDIFICIO, 1, 1, 0).texto())
    @Test fun textoDeAreaYOtro() {
        assertEquals("Cafetería", Ubicacion(lugar = "Cafetería").texto())
        assertEquals("Entrada norte", Ubicacion(Campus.OTRO, otro = "  Entrada norte ").texto())
    }
    @Test fun idaYVueltaPorTexto() {
        listOf(Ubicacion(Campus.EDIFICIO, 6, 5, 510), Ubicacion(Campus.EDIFICIO, 2, 3, 0),
            Ubicacion(lugar = "Biblioteca"), Ubicacion(Campus.OTRO, otro = "Entrada norte")).forEach {
            assertEquals(it, Ubicacion.desdeTexto(it.texto()))
        }
    }
    @Test fun textoAntiguoLibreSeTrataComoOtro() =
        assertEquals(Ubicacion(Campus.OTRO, otro = "Bloque B piso 2 salon 204"), Ubicacion.desdeTexto("Bloque B piso 2 salon 204"))
    @Test fun codigoConservaSeleccionIncompleta() {
        val u = Ubicacion(Campus.EDIFICIO, edificio = 3)
        assertEquals(u, Ubicacion.desdeCodigo(u.codigo()))
        val o = Ubicacion(Campus.OTRO, otro = "a|b|c")
        assertEquals(o, Ubicacion.desdeCodigo(o.codigo()))
    }
}
