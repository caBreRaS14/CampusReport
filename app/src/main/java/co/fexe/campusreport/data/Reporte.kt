package co.fexe.campusreport.data

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

/** Estados de un reporte. Los cambios los hace una persona desde la sección Gestión (no hay backend). */
object Estados {
    /** Inicial: el reporte solo existe en el dispositivo, NO se ha enviado a nadie. */
    const val PENDIENTE = "Pendiente de sincronización"
    const val REPORTADO = "Reportado"
    const val EN_ATENCION = "En atención"
    const val RESUELTO = "Resuelto"
    val TODOS = listOf(PENDIENTE, REPORTADO, EN_ATENCION, RESUELTO)
}

const val ESTADO_INICIAL = Estados.PENDIENTE

val CATEGORIAS = listOf("Infraestructura", "Equipos", "Conectividad", "Otro")

@Entity(tableName = "reportes")
data class Reporte(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoria: String,
    val descripcion: String,
    val ubicacion: String,
    val fecha: Long,
    val estado: String = ESTADO_INICIAL,
    /** true si lo creó el botón «Cargar ejemplos» (datos de demostración, no reales). */
    val ejemplo: Boolean = false
)

/** Una línea del historial: creación del reporte (estadoAnterior = null) o cambio de estado. */
@Entity(
    tableName = "eventos",
    foreignKeys = [ForeignKey(entity = Reporte::class, parentColumns = ["id"], childColumns = ["reporteId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("reporteId")]
)
data class Evento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reporteId: Long,
    val fecha: Long,
    val estadoAnterior: String?,
    val estadoNuevo: String,
    val nota: String = ""
)

@Dao
abstract class ReporteDao {
    @Query("SELECT * FROM reportes ORDER BY fecha DESC, id DESC")
    abstract fun todos(): Flow<List<Reporte>>
    @Query("SELECT * FROM eventos ORDER BY fecha DESC, id DESC")
    abstract fun eventos(): Flow<List<Evento>>
    @Query("SELECT * FROM reportes WHERE id = :id") abstract suspend fun porId(id: Long): Reporte?
    @Insert abstract suspend fun insertar(r: Reporte): Long
    @Insert abstract suspend fun insertarEvento(e: Evento): Long
    @Update abstract suspend fun actualizar(r: Reporte)
    @Query("DELETE FROM reportes WHERE id = :id") abstract suspend fun eliminar(id: Long)
    @Query("DELETE FROM reportes WHERE ejemplo = 1") abstract suspend fun eliminarEjemplos(): Int
    @Query("SELECT COUNT(*) FROM reportes") abstract suspend fun contar(): Int
    @Query("SELECT COUNT(*) FROM reportes WHERE ejemplo = 1") abstract suspend fun contarEjemplos(): Int
    @Query("SELECT COUNT(*) FROM eventos") abstract suspend fun contarEventos(): Int

    /** Inserta el reporte y su primer evento en una sola transacción. */
    @Transaction open suspend fun crear(r: Reporte): Long {
        val id = insertar(r)
        insertarEvento(Evento(reporteId = id, fecha = r.fecha, estadoAnterior = null, estadoNuevo = r.estado))
        return id
    }

    /** Cambia el estado y deja el movimiento en el historial. false si no existe o ya tenía ese estado. */
    @Transaction open suspend fun cambiarEstado(id: Long, nuevo: String, nota: String, ahora: Long): Boolean {
        val r = porId(id) ?: return false
        if (r.estado == nuevo) return false
        actualizar(r.copy(estado = nuevo))
        insertarEvento(Evento(reporteId = id, fecha = ahora, estadoAnterior = r.estado, estadoNuevo = nuevo, nota = nota))
        return true
    }

    /** Reemplaza los ejemplos existentes por los dados (reportes + su historial). */
    @Transaction open suspend fun reemplazarEjemplos(lista: List<Pair<Reporte, List<Evento>>>) {
        eliminarEjemplos()
        lista.forEach { (r, evs) ->
            val id = insertar(r)
            evs.forEach { insertarEvento(it.copy(reporteId = id)) }
        }
    }
}

/** v1 → v2: columna «ejemplo», tabla «eventos» y un evento de creación por cada reporte ya guardado. */
val MIGRACION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE reportes ADD COLUMN ejemplo INTEGER NOT NULL DEFAULT 0")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS eventos (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "reporteId INTEGER NOT NULL, fecha INTEGER NOT NULL, estadoAnterior TEXT, " +
                "estadoNuevo TEXT NOT NULL, nota TEXT NOT NULL, " +
                "FOREIGN KEY(reporteId) REFERENCES reportes(id) ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_eventos_reporteId ON eventos (reporteId)")
        db.execSQL(
            "INSERT INTO eventos (reporteId, fecha, estadoAnterior, estadoNuevo, nota) " +
                "SELECT id, fecha, NULL, estado, '' FROM reportes"
        )
    }
}

@Database(entities = [Reporte::class, Evento::class], version = 2, exportSchema = false)
abstract class AppDb : RoomDatabase() {
    abstract fun reporteDao(): ReporteDao
    companion object {
        @Volatile private var inst: AppDb? = null
        fun get(ctx: android.content.Context): AppDb = inst ?: synchronized(this) {
            inst ?: Room.databaseBuilder(ctx.applicationContext, AppDb::class.java, "campusreport.db")
                .addMigrations(MIGRACION_1_2)
                .build().also { inst = it }
        }
    }
}
