package co.fexe.campusreport

import co.fexe.campusreport.data.*
import org.junit.Assert.*
import org.junit.Test

class ValidacionTest {
    private val salon203 = Ubicacion(Campus.EDIFICIO, edificio = 3, piso = 2, salon = 203)

    @Test fun todoValido() = assertFalse(validar("Equipos", "Proyector dañado", salon203).hayErrores)
    @Test fun camposVacios() {
        val e = validar("", "", Ubicacion())
        assertNotNull(e.categoria); assertNotNull(e.descripcion); assertNotNull(e.ubicacion)
    }
    @Test fun soloEspaciosNoSirve() {
        val e = validar("Otro", "   \n ", Ubicacion(Campus.OTRO, otro = "  "))
        assertNull(e.categoria); assertNotNull(e.descripcion); assertNotNull(e.ubicacion)
    }
    @Test fun categoriaDesconocida() = assertNotNull(validar("Inventada", "x", salon203).categoria)
    @Test fun estadoInicialNoDiceEnviado() = assertEquals("Pendiente de sincronización", ESTADO_INICIAL)

    @Test fun edificioIncompletoEsInvalido() {
        assertNotNull(errorUbicacion(Ubicacion(Campus.EDIFICIO)))
        assertNotNull(errorUbicacion(Ubicacion(Campus.EDIFICIO, edificio = 3)))
        assertNotNull(errorUbicacion(Ubicacion(Campus.EDIFICIO, edificio = 3, piso = 2)))
    }
    @Test fun salonDeOtroPisoEsInvalido() =
        assertNotNull(errorUbicacion(Ubicacion(Campus.EDIFICIO, edificio = 3, piso = 4, salon = 203)))
    @Test fun zonaComunEsValida() =
        assertNull(errorUbicacion(Ubicacion(Campus.EDIFICIO, edificio = 1, piso = 1, salon = 0)))
    @Test fun areaSinMasDatosEsValida() = assertNull(errorUbicacion(Ubicacion(lugar = "Biblioteca")))
    @Test fun areaDesconocidaEsInvalida() = assertNotNull(errorUbicacion(Ubicacion(lugar = "Marte")))
    @Test fun otroConTextoEsValido() = assertNull(errorUbicacion(Ubicacion(Campus.OTRO, otro = "Entrada norte")))
}
