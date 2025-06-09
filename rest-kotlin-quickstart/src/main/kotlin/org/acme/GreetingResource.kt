package org.acme

import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.infrastructure.Infrastructure
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.acme.rest.Greeting
import java.time.Duration

@ApplicationScoped
class GreetingService {
    fun greeting(name: String): Greeting {
        return Greeting("hello $name")
    }
}

@Path("/")
class ReactiveGreetingResource(
    private val service: GreetingService
) {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/hello/{name}")
    fun hello(@PathParam("name") name: String): Uni<Greeting> {
        return Uni.createFrom().item(name)
            .emitOn(Infrastructure.getDefaultWorkerPool())
            .onItem()
            .transform { n -> service.greeting(n) }
    }

    @GET
    @Path("/delayed/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    fun delayed(@PathParam("name") name: String): Uni<Greeting> {
        return Uni.createFrom().item(name)
            .emitOn(Infrastructure.getDefaultWorkerPool()) // shift blocking work off IO thread
            .onItem()
            .transform { n -> service.greeting(n) } // sync transform
            .onItem()
            .delayIt()
            .by(Duration.ofSeconds(1)) // simulate delay
    }

    @GET
    @Path("/timeout/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    fun delayedTimeout(@PathParam("name") name: String): Uni<Greeting> {
        return Uni.createFrom().item(name)
            .emitOn(Infrastructure.getDefaultWorkerPool()) // shift blocking work off IO thread
            .onItem()
            .transform { n -> service.greeting(n) } // sync transform
            .onItem()
            .delayIt().by(Duration.ofSeconds(2))
            .ifNoItem().after(Duration.ofSeconds(1))
            .fail()
            .onFailure()
            .recoverWithUni { _ ->
                val errorResponse = Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(mapOf("error" to "Request timed out before completing"))
                    .build()
                Uni.createFrom().failure(WebApplicationException(errorResponse))
            }
    }
}
