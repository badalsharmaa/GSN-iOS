package com.example.getsafenowclient.turn


import io.ktor.resources.Resource
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import net.folivo.trixnity.core.Auth
import net.folivo.trixnity.core.AuthRequired
import net.folivo.trixnity.core.HttpMethod
import net.folivo.trixnity.core.HttpMethodType.GET
import net.folivo.trixnity.core.MatrixEndpoint
import net.folivo.trixnity.core.serialization.events.EventContentSerializerMappings

/**
 * GET /_matrix/client/v3/voip/turnServer
 *
 * Requires authentication.
 * Returns temporary TURN credentials.
 *
 * @see https://spec.matrix.org/v1.10/client-server-api/#get_matrixclientv3voipturnserver
 */
@Serializable
@Resource("/_matrix/client/v3/voip/turnServer")
@HttpMethod(GET)
@Auth(AuthRequired.YES)
object GetTurnServer : MatrixEndpoint<Unit, GetTurnServer.Response> {

    @Serializable
    data class Response(
        @SerialName("username") val username: String,
        @SerialName("password") val password: String,
        @SerialName("uris") val uris: List<String>,
        @SerialName("ttl") val ttl: Long
    )

    override fun responseSerializerBuilder(
        mappings: EventContentSerializerMappings,
        json: Json,
        value: Response?
    ): KSerializer<Response>? {
        return Response.serializer()
    }
}


