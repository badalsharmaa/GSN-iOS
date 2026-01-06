package io.getsafenow.libraries.network



import io.getsafenow.libraries.gsn_core.meta.BuildMeta
import io.getsafenow.libraries.network.interceptors.UserAgentInterceptor
import io.getsafenow.libraries.di.AppScopeGsn
import io.getsafenow.libraries.network.interceptors.FormattedJsonHttpLogger
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

@Component
@ContributesTo(AppScopeGsn::class)
@SingleIn(AppScopeGsn::class)
class NetworkModule @Inject constructor(
    private val buildMeta: BuildMeta,
    private val userAgentInterceptor: UserAgentInterceptor,
    private val httpClientEngine: HttpClientEngine,
) {

    @SingleIn(AppScopeGsn::class)
    fun providesHttpClient(): HttpClient = HttpClient(httpClientEngine) {
        // Platform-specific timeout configuration
        configureEngine(httpClientEngine)

        // User agent interceptor
        install(userAgentInterceptor.plugin)

        // Content negotiation for JSON
        install(ContentNegotiation) {
            json(providesJson())
        }

        // Logging interceptor (only in debug mode)
        if (buildMeta.isDebuggable) {
            install(Logging) {
                level = LogLevel.BODY
                logger = FormattedJsonHttpLogger(LogLevel.BODY)
            }
        }
    }

    @SingleIn(AppScopeGsn::class)
    fun providesJson(): Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }
}

expect fun configureEngine(engine: HttpClientEngine)
