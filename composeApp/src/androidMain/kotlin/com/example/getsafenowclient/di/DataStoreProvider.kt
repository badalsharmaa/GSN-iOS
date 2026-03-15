package com.example.getsafenowclient.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import io.getsafenow.libraries.kmputils.platformkmp.ContextFactory

// Extension property for DataStore
private val Context.callStateDataStore: DataStore<Preferences> by preferencesDataStore(name = "call_state")

/**
 * Android implementation of createCallStateDataStore.
 * Uses Android's preferencesDataStore extension.
 */
actual fun createCallStateDataStore(contextFactory: ContextFactory): DataStore<Preferences> {
    val context = contextFactory.getContext() as Context
    return context.callStateDataStore
}
