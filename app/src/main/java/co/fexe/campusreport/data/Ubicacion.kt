package co.fexe.campusreport.data

/**
 * Estructura del campus usada por el selector de ubicación.
 * Supuestos de esta versión (ajustables aquí): 6 edificios, 5 pisos y 10 salones por piso.
 * Los salones se numeran por piso: piso 2 → 201…210, piso 4 → 401…410.
 */
object Campus {
    val EDIFICIOS = (1..6).toList()
    val PISOS = (1..5).toList()
    const val SALONES_POR_PISO = 10
    val AREAS = listOf("Biblioteca", "Cafetería", "Auditorio", "Canchas deportivas", "Parqueadero", "Zonas verdes")
    const val EDIFICIO = "Edificio"
    const val OTRO = "Otro"
    val LUGARES = listOf(EDIFICIO) + AREAS + OTRO

    fun salonesDe(piso: Int): List<Int> = (1..SALONES_POR_PISO).map { piso * 100 + it }
}

/**
 * Ubicación elegida por el usuario.
 * lugar: "" (sin elegir), "Edificio", un área de [Campus.AREAS] u "Otro".
 * salon: -1 sin elegir, 0 zona común del piso, o el número del salón.
 */
data class Ubicacion(
    val lugar: String = "",
    val edificio: Int = 0,
    val piso: Int = 0,
    val salon: Int = -1,
    val otro: String = ""
) {
    /** Texto legible que se guarda en la base y se muestra en el listado. */
    fun texto(): String = when (lugar) {
        Campus.EDIFICIO -> "Edificio $edificio · Piso $piso · " + if (salon == 0) "Zona común" else "Salón $salon"
        Campus.OTRO -> otro.trim()
        else -> lugar
    }

    /** Codificación para SavedStateHandle (admite selecciones incompletas). */
    fun codigo(): String = "$lugar|$edificio|$piso|$salon|$otro"

    companion object {
        fun desdeCodigo(c: String): Ubicacion {
            val p = c.split("|", limit = 5)
            if (p.size < 5) return Ubicacion()
            return Ubicacion(p[0], p[1].toIntOrNull() ?: 0, p[2].toIntOrNull() ?: 0, p[3].toIntOrNull() ?: -1, p[4])
        }

        private val REGEX = Regex("""^Edificio (\d+) · Piso (\d+) · (?:Zona común|Salón (\d+))$""")

        /** Reconstruye la selección desde el texto guardado; lo que no encaja se trata como «Otro». */
        fun desdeTexto(t: String): Ubicacion {
            REGEX.matchEntire(t)?.let { m ->
                return Ubicacion(Campus.EDIFICIO, m.groupValues[1].toInt(), m.groupValues[2].toInt(),
                    m.groups[3]?.value?.toInt() ?: 0)
            }
            if (t in Campus.AREAS) return Ubicacion(lugar = t)
            return Ubicacion(lugar = Campus.OTRO, otro = t)
        }
    }
}
