package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkshopDao {

    // Clients
    @Query("SELECT * FROM clients ORDER BY name ASC")
    fun getAllClients(): Flow<List<Client>>

    @Query("SELECT * FROM clients WHERE UPPER(plate) = UPPER(:plate) LIMIT 1")
    suspend fun getClientByPlate(plate: String): Client?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: Client): Long

    @Delete
    suspend fun deleteClient(client: Client)

    // Maintenances
    @Query("SELECT * FROM maintenances ORDER BY id DESC")
    fun getAllMaintenances(): Flow<List<Maintenance>>

    @Query("SELECT * FROM maintenances WHERE UPPER(clientPlate) = UPPER(:plate) ORDER BY id DESC")
    fun getMaintenancesByPlate(plate: String): Flow<List<Maintenance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaintenance(maintenance: Maintenance): Long

    // Components Catalog
    @Query("SELECT * FROM components ORDER BY isLevelCheck ASC, name ASC")
    fun getAllComponents(): Flow<List<Component>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComponent(component: Component): Long

    @Query("DELETE FROM components WHERE id = :id")
    suspend fun deleteComponentById(id: Int)

    // VehicleChecks (General check reports)
    @Query("SELECT * FROM vehicle_checks ORDER BY id DESC")
    fun getAllVehicleChecks(): Flow<List<VehicleCheck>>

    @Query("SELECT * FROM vehicle_checks WHERE UPPER(clientPlate) = UPPER(:plate) ORDER BY id DESC")
    fun getVehicleChecksByPlate(plate: String): Flow<List<VehicleCheck>>

    @Query("SELECT * FROM vehicle_checks WHERE id = :id LIMIT 1")
    suspend fun getVehicleCheckById(id: Int): VehicleCheck?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicleCheck(check: VehicleCheck): Long

    // ComponentCheckResults (individual answers inside a check)
    @Query("SELECT * FROM component_checks WHERE vehicleCheckId = :checkId ORDER BY id ASC")
    suspend fun getComponentChecksForReport(checkId: Int): List<ComponentCheck>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComponentChecks(checks: List<ComponentCheck>)
}

@Database(
    entities = [
        Client::class,
        Maintenance::class,
        Component::class,
        VehicleCheck::class,
        ComponentCheck::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val workshopDao: WorkshopDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "workshop_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
