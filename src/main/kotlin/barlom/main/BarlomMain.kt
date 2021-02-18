package barlom.main

import barlom.dxl.codegen.CodeGenerator
import barlom.dxl.codegen.CodeStringBuilder
import barlom.dxl.parsing.impl.DxlParser
import org.http4k.cloudnative.asK8sServer
import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.cloudnative.health.Completed
import org.http4k.cloudnative.health.Health
import org.http4k.cloudnative.health.ReadinessCheck
import org.http4k.cloudnative.health.ReadinessCheckResult
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.events.AutoMarshallingEvents
import org.http4k.events.Event
import org.http4k.events.EventFilters
import org.http4k.events.then
import org.http4k.filter.ResponseFilters
import org.http4k.format.Jackson
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.ApacheServer
import org.http4k.server.Http4kServer
import java.io.BufferedReader

object BarlomMain {

    @JvmStatic
    fun main(args: Array<String>) {

        // TODO: https://www.http4k.org/guide/modules/cloud_native/

        lateinit var apiServer: Http4kServer

        // The default configuration.
        val defaultConfig = Environment.defaults(
            EnvironmentKey.k8s.SERVICE_PORT of 8080,
            EnvironmentKey.k8s.HEALTH_PORT of 8081
        )

        // Define the chain of environment properties.
        val k8sPodEnv = //Environment.fromResource("app.properties") overrides
            Environment.JVM_PROPERTIES overrides
                    Environment.ENV overrides
                    defaultConfig

        // Define the health app API for Kubernetes.
        val healthApplication = Health(
            "/config" bind GET to {
                getConfiguration(k8sPodEnv)
            },
            "/shutdown" bind GET to {
                // TODO: delayed for clean shut down
                apiServer.stop()
                Response(Status.OK).body("Shutting down ...")
            },
            checks = listOf(BarlomReadinessCheck())
        )

        // Configure logging.
        val events = EventFilters.AddTimestamp()
            .then(EventFilters.AddZipkinTraces())
            .then(AutoMarshallingEvents(Jackson))

        // Define the working API.
        val apiApplication = routes(
            "format" bind POST to { request ->
                val code = request.body.stream.bufferedReader().use(BufferedReader::readText)

                val parser = DxlParser("POST", code)

                val topLevel = parser.parseTopLevel()

                val builder = CodeStringBuilder()
                val codeGenerator = CodeGenerator(builder)
                codeGenerator.writeTopLevel(topLevel)

                Response(Status.OK).body(builder.toString())
            }
        )

        // Add logging.
        val apiWithLogging =
            ResponseFilters.ReportHttpTransaction { httpTransaction ->
                events(
                    IncomingHttpRequest(
                        httpTransaction.request.uri,
                        httpTransaction.response.status.code,
                        httpTransaction.duration.toMillis()
                    )
                )
            }.then(apiApplication)

        val healthWithLogging =
            ResponseFilters.ReportHttpTransaction { httpTransaction ->
                events(
                    IncomingHttpRequest(
                        httpTransaction.request.uri,
                        httpTransaction.response.status.code,
                        httpTransaction.duration.toMillis()
                    )
                )
            }.then(healthApplication)

        // Start the two servers for Kubernetes.
        apiServer = apiWithLogging.asK8sServer(::ApacheServer, k8sPodEnv, healthWithLogging).start()

    }

    private fun getConfiguration(k8sPodEnv: Environment): Response {
        var result = "Build #011\n"
        for (key in k8sPodEnv.keys().sorted()) {
            val value = k8sPodEnv[key]
            result += "$key = $value\n"
        }
        return Response(Status.OK).body(result)
    }
}

// Minimal readiness check.
class BarlomReadinessCheck : ReadinessCheck {
    override val name = "Barlom"
    override fun invoke(): ReadinessCheckResult {
        return Completed(name)
    }
}

// Simple access logging events.
data class IncomingHttpRequest(val uri: Uri, val httpStatus: Int, val durationMs: Long) : Event

