package any.mapper.data.db

import androidx.room.*
import any.mapper.data.model.Mapping
import kotlinx.coroutines.flow.Flow

@Dao
interface MappingDao {
    @Query("SELECT * FROM mappings WHERE profileId = :profileId ORDER BY id ASC")
    fun getMappingsForProfile(profileId: Long): Flow<List<Mapping>>

    @Query("SELECT * FROM mappings WHERE profileId = :profileId AND enabled = 1")
    suspend fun getEnabledMappingsForProfile(profileId: Long): List<Mapping>

    @Query("SELECT * FROM mappings WHERE id = :id")
    suspend fun getById(id: Long): Mapping?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mapping: Mapping): Long

    @Update
    suspend fun update(mapping: Mapping)

    @Delete
    suspend fun delete(mapping: Mapping)

    @Query("DELETE FROM mappings WHERE profileId = :profileId")
    suspend fun deleteAllForProfile(profileId: Long)

    @Query("UPDATE mappings SET enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)
}
