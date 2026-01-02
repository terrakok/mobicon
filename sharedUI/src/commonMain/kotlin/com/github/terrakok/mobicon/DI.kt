package com.github.terrakok.mobicon

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import co.touchlab.kermit.NoTagFormatter
import co.touchlab.kermit.Severity
import co.touchlab.kermit.loggerConfigInit
import co.touchlab.kermit.platformLogWriter
import com.sebaslogen.resaca.viewModelScoped
import dev.zacsweers.metro.*
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass

private const val DEBUG = false

internal val Log = object : co.touchlab.kermit.Logger(
    config = loggerConfigInit(
        platformLogWriter(NoTagFormatter),
        minSeverity = if (DEBUG) Severity.Verbose else Severity.Error,
    ),
    tag = "MobiCon"
) {}

@SingleIn(AppScope::class)
@DependencyGraph(AppScope::class)
internal interface AppGraph {
    val viewModelFactory: MetroViewModelFactory

    @SingleIn(AppScope::class)
    @Provides
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
        explicitNulls = false
    }

    @SingleIn(AppScope::class)
    @Provides
    fun provideHttpClient(json: Json): HttpClient = HttpClient {
        install(ContentNegotiation) { json(json) }
        install(HttpCache)
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    co.touchlab.kermit.Logger.d("httpClient") { message }
                }
            }
            level = if (DEBUG) LogLevel.ALL else LogLevel.NONE
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 50000
            socketTimeoutMillis = 50000
        }
    }
}

internal val LocalAppGraph = staticCompositionLocalOf<AppGraph> {
    error("No app graph provided")
}

@Composable
internal fun WithAppGraph(
    content: @Composable () -> Unit,
) {
    val graph = remember { createGraph<AppGraph>() }
    CompositionLocalProvider(LocalAppGraph provides graph, content = content)
}

@MapKey
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ViewModelKey(val value: KClass<out ViewModel>)

@Inject
internal class MetroViewModelFactory(
    private val viewModelProviders: Map<KClass<out ViewModel>, Provider<ViewModel>>
) {

    @Suppress("UNCHECKED_CAST")
    fun <T : ViewModel> create(modelClass: KClass<T>): T {
        val provider = viewModelProviders[modelClass]
            ?: throw IllegalArgumentException("Unknown model class $modelClass")
        return provider() as T
    }
}

@Composable
internal inline fun <reified T : ViewModel> metroVmScoped(): T {
    val factory = LocalAppGraph.current.viewModelFactory
    return viewModelScoped<T> { factory.create(T::class) }
}