package co.fexe.campusreport.data

/** Errores por campo; null = campo válido. */
data class ErroresForm(
    val categoria: String? = null,
    val descripcion: String? = null,
    val ubicacion: String? = null
) {
    val hayErrores get() = categoria != null || descripcion != null || ubicacion != null
}

fun validar(categoria: String, descripcion: String, ubicacion: String) = ErroresForm(
    categoria = if (categoria !in CATEGORIAS) "Elige una categoría." else null,
    descripcion = if (descripcion.isBlank()) "Describe la novedad (no basta con espacios)." else null,
    ubicacion = if (ubicacion.isBlank()) "Escribe dónde ocurre (bloque, piso o salón)." else null
)
