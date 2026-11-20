package co.fexe.campusreport.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/** Estado inicial: el reporte solo existe en el dispositivo, NO se ha enviado a nadie. */
const val ESTADO_INICIAL = "Pendiente de sincronización"

val CATEGORIAS = listOf("Infraestructura", "Equipos", "Conectividad", "Otro")

@Entity(tableName = "reportes")
data class Reporte(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoria: String,
    val descripcion: String,
    val ubicacion: String,
    val fecha: Long,
    val estado: String = ESTADO_INICIAL
)

@Dao
interface ReporteDao {
    @Query("SELECT * FROM reportes ORDER BY fecha DESC, id DESC")
    fun todos(): Flow<List<Reporte>>
    @Insert suspend fun insertar(r: Reporte): Long
}

@Database(entities = [Reporte::class], version = 1, exportSchema = false)
abstract class AppDb : RoomDatabase() {
    abstract fun reporteDao(): ReporteDao
    companion object {
        @Volatile private var inst: AppDb? = null
        fun get(ctx: android.content.Context): AppDb = inst ?: synchronized(this) {
            inst ?: Room.databaseBuilder(ctx.applicationContext, AppDb::class.java, "campusreport.db")
                .build().also { inst = it }
        }
    }
}
