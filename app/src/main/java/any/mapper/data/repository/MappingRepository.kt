package any.mapper.data.repository

import any.mapper.data.db.MappingDao
import any.mapper.data.db.ProfileDao
import any.mapper.data.model.Mapping
import any.mapper.data.model.Profile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MappingRepository @Inject constructor(
    private val profileDao: ProfileDao,
    private val mappingDao: MappingDao
) {
    val allProfiles: Flow<List<Profile>> = profileDao.getAllProfiles()
    val activeProfile: Flow<Profile?> = profileDao.getActiveProfile()

    fun getMappingsForProfile(profileId: Long): Flow<List<Mapping>> =
        mappingDao.getMappingsForProfile(profileId)

    suspend fun getEnabledMappingsForProfile(profileId: Long): List<Mapping> =
        mappingDao.getEnabledMappingsForProfile(profileId)

    suspend fun getActiveProfileOnce(): Profile? = profileDao.getActiveProfileOnce()

    suspend fun getProfileForPackage(packageName: String): Profile? =
        profileDao.getProfileForPackage(packageName)

    suspend fun createProfile(name: String): Long {
        val profile = Profile(name = name)
        val id = profileDao.insert(profile)
        val allProfiles = profileDao.getActiveProfileOnce()
        if (allProfiles == null) profileDao.switchActive(id)
        return id
    }

    suspend fun updateProfile(profile: Profile) = profileDao.update(profile)

    suspend fun deleteProfile(profile: Profile) {
        mappingDao.deleteAllForProfile(profile.id)
        profileDao.delete(profile)
    }

    suspend fun switchActiveProfile(profileId: Long) = profileDao.switchActive(profileId)

    suspend fun duplicateProfile(profileId: Long, newName: String): Long {
        val mappings = mappingDao.getEnabledMappingsForProfile(profileId)
        val newProfileId = profileDao.insert(Profile(name = newName))
        mappings.forEach { mappingDao.insert(it.copy(id = 0, profileId = newProfileId)) }
        return newProfileId
    }

    suspend fun insertMapping(mapping: Mapping): Long = mappingDao.insert(mapping)

    suspend fun updateMapping(mapping: Mapping) = mappingDao.update(mapping)

    suspend fun deleteMapping(mapping: Mapping) = mappingDao.delete(mapping)

    suspend fun setMappingEnabled(id: Long, enabled: Boolean) =
        mappingDao.setEnabled(id, enabled)

    suspend fun createQuickSetupProfile(name: String, packageName: String, mappings: List<Mapping>) {
        val id = profileDao.insert(Profile(name = name, autoActivatePackage = packageName))
        mappings.forEach { mappingDao.insert(it.copy(id = 0, profileId = id)) }
    }
}
