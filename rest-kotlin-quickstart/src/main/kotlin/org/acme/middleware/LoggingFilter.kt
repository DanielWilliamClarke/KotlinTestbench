package org.acme.middleware

import jakarta.annotation.Priority
import jakarta.ws.rs.Priorities
import jakarta.ws.rs.container.*
import jakarta.ws.rs.ext.Provider
import org.jboss.logging.Logger

@Provider
@Priority(Priorities.USER)
class LoggingFilter : ContainerRequestFilter, ContainerResponseFilter {

    private val log: Logger = Logger.getLogger(LoggingFilter::class.java)

    override fun filter(requestContext: ContainerRequestContext) {
        val method = requestContext.method
        val path = requestContext.uriInfo.requestUri.toString()
        val headers = requestContext.headers

        log.info("➡️  $method $path")
        headers.forEach { (key, value) ->
            log.debug("   Header: $key = ${value.joinToString()}")
        }
    }

    override fun filter(
        requestContext: ContainerRequestContext,
        responseContext: ContainerResponseContext
    ) {
        val status = responseContext.status
        val method = requestContext.method
        val path = requestContext.uriInfo.requestUri.toString()

        log.info("⬅️  $method $path -> HTTP $status")
    }
}
