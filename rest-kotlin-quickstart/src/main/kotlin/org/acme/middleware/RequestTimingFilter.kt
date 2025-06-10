package org.acme.middleware

import jakarta.annotation.Priority
import jakarta.ws.rs.Priorities
import jakarta.ws.rs.container.*
import jakarta.ws.rs.ext.Provider
import org.jboss.logging.Logger

@Provider
@Priority(Priorities.USER)
class RequestTimingFilter : ContainerRequestFilter, ContainerResponseFilter {

    private val log: Logger = Logger.getLogger(RequestTimingFilter::class.java)
    private val startTimeKey = "RequestStartTime"

    override fun filter(requestContext: ContainerRequestContext) {
        requestContext.setProperty(startTimeKey, System.nanoTime())
    }

    override fun filter(
        requestContext: ContainerRequestContext,
        responseContext: ContainerResponseContext
    ) {
        val start = requestContext.getProperty(startTimeKey) as? Long ?: return
        val durationNs = System.nanoTime() - start
        val durationMs = durationNs / 1_000_000

        val method = requestContext.method
        val uri = requestContext.uriInfo.requestUri
        val status = responseContext.status

        log.info("⏱️  $method $uri -> $status in ${durationMs}ms")
    }
}
