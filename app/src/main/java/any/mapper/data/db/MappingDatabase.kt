package any.mapper.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import any.mapper.data.model.Mapping
import any.mapper.data.model.Profile

@Database(
    entities = [Profile::class, Mapping::class],
    version = 1,
    exportSchema = false
)
abstract class MappingDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun mappingDao(): MappingDao
}
