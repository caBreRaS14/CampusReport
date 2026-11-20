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

    private fun llenar(cat: String, desc: String, ubi: String) {
        rule.onNodeWithTag("cat_$cat").performClick()
        rule.onNodeWithTag("campo_descripcion").performTextInput(desc)
        rule.onNodeWithTag("campo_ubicacion").performTextInput(ubi)
        rule.onNodeWithText("Guardar reporte").performClick()
        rule.waitForIdle()
    }

    private fun total() = runBlocking { dao.contar() }

    @Test fun formularioVacioNoGuardaYMuestraErrores() {
        rule.onNodeWithText("Guardar reporte").performClick()
        rule.onNodeWithText("Elige una categoría.").assertIsDisplayed()
        rule.onNodeWithText("Describe la novedad (no basta con espacios).").assertIsDisplayed()
        rule.onNodeWithText("Escribe dónde ocurre (bloque, piso o salón).").assertIsDisplayed()
        assertEquals(0, total())
    }

    @Test fun guardarMuestraEnListadoConEstadoPendiente() {
        llenar("Equipos", "Proyector no enciende", "Bloque B 204")
        rule.waitUntil(3000) { total() == 1 }
        rule.onNodeWithText("Proyector no enciende").assertIsDisplayed()
        rule.onNodeWithText("Pendiente de sincronización").assertIsDisplayed()
    }

    @Test fun filtrarPorCategoria() {
        llenar("Equipos", "Proyector no enciende", "Bloque B 204")
        rule.onNodeWithText("Registrar").performClick()
        llenar("Conectividad", "Sin WiFi en biblioteca", "Biblioteca")
        rule.waitUntil(3000) { total() == 2 }
        rule.onNodeWithTag("filtro_Conectividad").performClick()
        rule.waitUntil(3000) { rule.onAllNodesWithText("Proyector no enciende").fetchSemanticsNodes().isEmpty() }
        rule.onNodeWithText("Sin WiFi en biblioteca").assertIsDisplayed()
        rule.onNodeWithTag("filtro_todas").performClick()
        rule.onNodeWithText("Proyector no enciende").assertIsDisplayed()
    }

    @Test fun editarYEliminar() {
        llenar("Equipos", "Proyector no enciende", "Bloque B 204")
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
