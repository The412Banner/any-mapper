package any.mapper.di

import android.content.Context
import androidx.room.Room
import any.mapper.data.db.MappingDatabase
import any.mapper.data.db.MappingDao
import any.mapper.data.db.ProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MappingDatabase =
        Room.databaseBuilder(context, MappingDatabase::class.java, "anymapper.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideProfileDao(db: MappingDatabase): ProfileDao = db.profileDao()

    @Provides
    fun provideMappingDao(db: MappingDatabase): MappingDao = db.mappingDao()
}
