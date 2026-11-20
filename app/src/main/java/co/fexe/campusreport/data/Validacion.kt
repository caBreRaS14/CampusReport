package co.fexe.campusreport.data

/** Errores por campo; null = campo válido. */
data class ErroresForm(
    val categoria: String? = null,
    val descripcion: String? = null,
    val ubicacion: String? = null
) {
    val hayErrores get() = categoria != null || descripcion != null || ubicacion != null
}

fun errorUbicacion(u: Ubicacion): String? = when {
    u.lugar !in Campus.LUGARES -> "Elige dónde ocurre."
    u.lugar == Campus.EDIFICIO && u.edificio !in Campus.EDIFICIOS -> "Elige el edificio."
    u.lugar == Campus.EDIFICIO && u.piso !in Campus.PISOS -> "Elige el piso."
    u.lugar == Campus.EDIFICIO && u.salon != 0 && u.salon !in Campus.salonesDe(u.piso) -> "Elige el salón o la zona común."
    u.lugar == Campus.OTRO && u.otro.isBlank() -> "Escribe el lugar (no basta con espacios)."
    else -> null
}

fun validar(categoria: String, descripcion: String, ubicacion: Ubicacion) = ErroresForm(
    categoria = if (categoria !in CATEGORIAS) "Elige una categoría." else null,
    descripcion = if (descripcion.isBlank()) "Describe la novedad (no basta con espacios)." else null,
    ubicacion = errorUbicacion(ubicacion)
)
