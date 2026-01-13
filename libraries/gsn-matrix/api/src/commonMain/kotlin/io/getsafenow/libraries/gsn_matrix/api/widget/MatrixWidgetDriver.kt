package io.getsafenow.libraries.gsn_matrix.api.widget

import kotlinx.coroutines.flow.Flow

interface MatrixWidgetDriver : AutoCloseable {
    val id: String
    val incomingMessages: Flow<String>

    suspend fun run()
    suspend fun send(message: String)
}
