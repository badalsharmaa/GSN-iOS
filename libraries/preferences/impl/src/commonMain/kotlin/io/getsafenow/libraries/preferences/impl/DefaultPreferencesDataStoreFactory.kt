package io.getsafenow.libraries.preferences.impl


import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import co.touchlab.stately.collections.ConcurrentMutableMap
import io.getsafenow.libraries.di.AppScopeGsn
import io.getsafenow.libraries.di.ApplicationContextGsn
import io.getsafenow.libraries.kmputils.platformkmp.ContextFactory
import io.getsafenow.libraries.kmputils.platformkmp.PlatformFile
import io.getsafenow.libraries.kmputils.preferences.DefaultPreferencesCorruptionHandlerFactory
import io.getsafenow.libraries.preferences.api.PreferenceDataStoreFactory
import me.tatarka.inject.annotations.Inject
import okio.Path.Companion.toPath
import software.amazon.lastmile.kotlin.inject.anvil.ContributesBinding
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

@SingleIn(AppScopeGsn::class)
@ContributesBinding(AppScopeGsn::class)
class DefaultPreferencesDataStoreFactory @Inject constructor(
    @ApplicationContextGsn private val platformContext: ContextFactory,
) : PreferenceDataStoreFactory {
    private val dataStoreHolders = ConcurrentMutableMap<String, DataStore<Preferences>>()

    override fun create(name: String): DataStore<Preferences> {
        return dataStoreHolders.getOrPut(name) {
            createDataStore(name)
        }
    }

    private fun createDataStore(name: String): DataStore<Preferences> {
        // Use string concatenation instead of resolve() for KMP compatibility
        val cacheDir = platformContext.cacheDir()
        val dataStoreFile = PlatformFile("${cacheDir.path}/$name.preferences_pb")

        return androidx.datastore.preferences.core.PreferenceDataStoreFactory.createWithPath(
            corruptionHandler = DefaultPreferencesCorruptionHandlerFactory.replaceWithEmpty(),
            produceFile = { dataStoreFile.path.toPath() }
        )
    }
}