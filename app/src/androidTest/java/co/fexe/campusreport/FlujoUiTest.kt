package co.fexe.campusreport

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.fexe.campusreport.data.AppDb
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FlujoUiTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val dao get() = AppDb.get(ApplicationProvider.getApplicationContext()).reporteDao()

    @Before fun limpiar() { runBlocking { dao.contar(); AppDb.get(ApplicationProvider.getApplicationContext()).clearAllTables() }; rule.waitForIdle() }

    /** Elige la ubicación por selección: edificio 3, piso 2, salón 203. */
    private fun elegirEdificio(e: Int = 3, p: Int = 2, s: Int = 203) {
        rule.onNodeWithTag("lugar_Edificio").performScrollTo().performClick()
        rule.onNodeWithTag("edificio_$e").performScrollTo().performClick()
        rule.onNodeWithTag("piso_$p").performScrollTo().performClick()
        rule.onNodeWithTag("salon_$s").performScrollTo().performClick()
    }

    private fun llenar(cat: String, desc: String, area: String? = null) {
        rule.onNodeWithTag("cat_$cat").performClick()
        rule.onNodeWithTag("campo_descripcion").performTextInput(desc)
        if (area == null) elegirEdificio() else rule.onNodeWithTag("lugar_$area").performScrollTo().performClick()
        rule.onNodeWithText("Guardar reporte").performClick()
        rule.waitForIdle()
    }

    private fun total() = runBlocking { dao.contar() }

    @Test fun formularioVacioNoGuardaYMuestraErrores() {
        rule.onNodeWithText("Guardar reporte").performClick()
        rule.onNodeWithText("Elige una categoría.").assertIsDisplayed()
        rule.onNodeWithText("Describe la novedad (no basta con espacios).").assertIsDisplayed()
        rule.onNodeWithText("Elige dónde ocurre.").performScrollTo().assertIsDisplayed()
        assertEquals(0, total())
    }

    @Test fun ubicacionIncompletaNoGuarda() {
        rule.onNodeWithTag("cat_Equipos").performClick()
        rule.onNodeWithTag("campo_descripcion").performTextInput("Proyector")
        rule.onNodeWithTag("lugar_Edificio").performScrollTo().performClick()
        rule.onNodeWithTag("edificio_3").performScrollTo().performClick()
        rule.onNodeWithText("Guardar reporte").performClick()
        rule.onNodeWithText("Elige el piso.").performScrollTo().assertIsDisplayed()
        assertEquals(0, total())
    }

    @Test fun salonesSeNumeranSegunElPisoElegido() {
        rule.onNodeWithTag("lugar_Edificio").performScrollTo().performClick()
        rule.onNodeWithTag("edificio_1").performScrollTo().performClick()
        rule.onNodeWithTag("piso_4").performScrollTo().performClick()
        rule.onNodeWithTag("salon_405").assertExists()
        rule.onNodeWithTag("salon_203").assertDoesNotExist()
        rule.onNodeWithTag("piso_2").performScrollTo().performClick()
        rule.onNodeWithTag("salon_203").assertExists()
        rule.onNodeWithTag("salon_405").assertDoesNotExist()
    }

    @Test fun soloOtroPermiteEscribirElLugar() {
        rule.onNodeWithTag("lugar_Biblioteca").performScrollTo().performClick()
        rule.onNodeWithTag("campo_ubicacion").assertDoesNotExist()
        rule.onNodeWithTag("lugar_Otro").performScrollTo().performClick()
        rule.onNodeWithTag("campo_ubicacion").assertExists()
    }

    @Test fun guardaUbicacionDeEdificioConFormatoLegible() {
        llenar("Equipos", "Proyector no enciende")
        rule.waitUntil(3000) { total() == 1 }
        assertEquals("Edificio 3 · Piso 2 · Salón 203", runBlocking { dao.todos().first().first().ubicacion })
        rule.onNodeWithText("Edificio 3 · Piso 2 · Salón 203").assertIsDisplayed()
    }

    @Test fun guardarMuestraEnListadoConEstadoPendiente() {
        llenar("Equipos", "Proyector no enciende")
        rule.waitUntil(3000) { total() == 1 }
        rule.onNodeWithText("Proyector no enciende").assertIsDisplayed()
        rule.onNodeWithText("Pendiente de sincronización").assertIsDisplayed()
    }

    @Test fun filtrarPorCategoria() {
        llenar("Equipos", "Proyector no enciende")
        rule.onNodeWithText("Registrar").performClick()
        llenar("Conectividad", "Sin WiFi en biblioteca", area = "Biblioteca")
        rule.waitUntil(3000) { total() == 2 }
        rule.onNodeWithTag("filtro_Conectividad").performClick()
        rule.waitUntil(3000) { rule.onAllNodesWithText("Proyector no enciende").fetchSemanticsNodes().isEmpty() }
        rule.onNodeWithText("Sin WiFi en biblioteca").assertIsDisplayed()
        rule.onNodeWithTag("filtro_todas").performClick()
        rule.onNodeWithText("Proyector no enciende").assertIsDisplayed()
    }

    @Test fun editarYEliminar() {
        llenar("Equipos", "Proyector no enciende")
        rule.waitUntil(3000) { total() == 1 }
        rule.onNodeWithText("Proyector no enciende").performClick()
        rule.onNodeWithText("Editar").performClick()
        rule.onNodeWithTag("campo_descripcion").performTextReplacement("Proyector reparado")
        rule.onNodeWithText("Guardar cambios").performClick()
        rule.waitUntil(3000) { runBlocking { dao.todos().first().first().descripcion } == "Proyector reparado" }
        rule.onNodeWithText("Proyector reparado").assertIsDisplayed()

        rule.onNodeWithText("Proyector reparado").performClick()
        rule.onNodeWithText("Eliminar").performClick()
        rule.onNodeWithText("¿Eliminar este reporte?").assertIsDisplayed()
        assertEquals(1, total()) // aún no se borra: falta confirmar
        rule.onAllNodesWithText("Eliminar").onLast().performClick()
        rule.waitUntil(3000) { total() == 0 }
    }
}
