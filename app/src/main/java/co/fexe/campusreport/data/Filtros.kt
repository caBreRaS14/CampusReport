package co.fexe.campusreport.data

/** Filtros combinables (estado + categoría + lugar). Cadena vacía = sin filtrar por ese criterio. */
data class Filtros(val estado: String = "", val categoria: String = "", val lugar: String = "") {
    val activos get() = listOf(estado, categoria, lugar).count { it.isNotEmpty() }

    fun codigo() = "$estado|$categoria|$lugar"

    fun aplicaSinEstado(r: Reporte) =
        (categoria.isEmpty() || r.categoria == categoria) && (lugar.isEmpty() || lugarDe(r.ubicacion) == lugar)

    fun aplica(r: Reporte) = (estado.isEmpty() || r.estado == estado) && aplicaSinEstado(r)

    companion object {
        /** Opciones del filtro «Lugar»: cada edificio, cada área y «Otro». */
        val LUGARES = Campus.EDIFICIOS.map { "Edificio $it" } + Campus.AREAS + Campus.OTRO

        fun desde(c: String): Filtros {
            val p = c.split("|", limit = 3)
            return if (p.size < 3) Filtros() else Filtros(p[0], p[1], p[2])
        }

        /** «Edificio 3», «Biblioteca», «Otro»… a partir del texto guardado de la ubicación. */
        fun lugarDe(texto: String): String {
            val u = Ubicacion.desdeTexto(texto)
            return if (u.lugar == Campus.EDIFICIO) "Edificio ${u.edificio}" else u.lugar
        }
    }
}
