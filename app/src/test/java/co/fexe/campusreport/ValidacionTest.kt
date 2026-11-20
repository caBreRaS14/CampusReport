package co.fexe.campusreport

import co.fexe.campusreport.data.*
import org.junit.Assert.*
import org.junit.Test

class ValidacionTest {
    @Test fun todoValido() = assertFalse(validar("Equipos", "Proyector dañado", "Bloque A 101").hayErrores)
    @Test fun camposVacios() {
        val e = validar("", "", "")
        assertNotNull(e.categoria); assertNotNull(e.descripcion); assertNotNull(e.ubicacion)
    }
    @Test fun soloEspaciosNoSirve() {
        val e = validar("Otro", "   \n ", "  ")
        assertNull(e.categoria); assertNotNull(e.descripcion); assertNotNull(e.ubicacion)
    }
    @Test fun categoriaDesconocida() = assertNotNull(validar("Inventada", "x", "y").categoria)
    @Test fun estadoInicialNoDiceEnviado() = assertEquals("Pendiente de sincronización", ESTADO_INICIAL)
}
