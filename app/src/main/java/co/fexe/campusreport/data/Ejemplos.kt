package co.fexe.campusreport.data

import java.util.Random

/**
 * DATOS DE EJEMPLO para demostrar la app. NO son reportes reales: se marcan con ejemplo = true,
 * se muestran con la etiqueta «Ejemplo» y se pueden quitar desde Gestión. Son deterministas (misma semilla).
 */
object Ejemplos {
    const val CANTIDAD = 40

    private val DESCRIPCIONES = mapOf(
        "Infraestructura" to listOf("Luminaria fuera de servicio", "Gotera en el techo", "Puerta que no cierra bien", "Silla rota",
            "Ventana con el vidrio roto", "Baño sin agua", "Filtración en la pared", "Tablero con el marco suelto"),
        "Equipos" to listOf("Proyector no enciende", "Computador sin respuesta", "Control del aire acondicionado dañado", "Parlante sin sonido",
            "Tablero digital sin señal", "Cable HDMI dañado", "Impresora atascada", "Micrófono sin batería"),
        "Conectividad" to listOf("Sin WiFi en el salón", "WiFi muy lenta", "Red cableada caída", "Punto de acceso apagado",
            "No conecta a la red institucional", "Señal intermitente", "Sin internet en el laboratorio", "Cobertura débil en el pasillo"),
        "Otro" to listOf("Olor extraño en el pasillo", "Falta señalización", "Objeto abandonado", "Ruido constante de obra",
            "Extintor sin revisar", "Basura acumulada", "Zona resbalosa", "Cerradura de casillero dañada")
    )

    fun generar(ahora: Long): List<Pair<Reporte, List<Evento>>> {
        val rnd = Random(7)
        val hora = 3_600_000L
        return (0 until CANTIDAD).map { i ->
            val cat = CATEGORIAS[i % 4]
            val desc = DESCRIPCIONES.getValue(cat)[(i / 4) % 8]
            val ubic = when (i % 5) {
                0, 1, 2 -> {
                    val piso = 1 + rnd.nextInt(Campus.PISOS.size)
                    Ubicacion(Campus.EDIFICIO, 1 + rnd.nextInt(Campus.EDIFICIOS.size), piso,
                        if (rnd.nextInt(6) == 0) 0 else Campus.salonesDe(piso)[rnd.nextInt(Campus.SALONES_POR_PISO)])
                }
                3 -> Ubicacion(lugar = Campus.AREAS[rnd.nextInt(Campus.AREAS.size)])
                else -> Ubicacion(Campus.OTRO, otro = "Entrada principal")
            }.texto()
            val creado = ahora - (1 + rnd.nextInt(44)) * 24 * hora - rnd.nextInt(10) * hora
            val destino = when (rnd.nextInt(10)) {   // 30 % pendiente, 20 % reportado, 20 % en atención, 30 % resuelto
                in 0..2 -> 0
                in 3..4 -> 1
                in 5..6 -> 2
                else -> 3
            }
            var t = creado
            var anterior: String? = null
            val eventos = (0..destino).map { k ->
                if (k > 0) t = minOf(ahora - hora, t + (2 + rnd.nextInt(40)) * hora)
                val e = Evento(reporteId = 0, fecha = t, estadoAnterior = anterior, estadoNuevo = Estados.TODOS[k],
                    nota = if (k == 0) "" else notaDe(Estados.TODOS[k]))
                anterior = Estados.TODOS[k]
                e
            }
            Reporte(categoria = cat, descripcion = desc, ubicacion = ubic, fecha = creado,
                estado = Estados.TODOS[destino], ejemplo = true) to eventos
        }
    }

    private fun notaDe(estado: String) = when (estado) {
        Estados.REPORTADO -> "Enviado al área de mantenimiento"
        Estados.EN_ATENCION -> "Técnico asignado"
        else -> "Revisado y arreglado"
    }
}
