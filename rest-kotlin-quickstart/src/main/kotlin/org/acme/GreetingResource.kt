package org.acme

import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
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
        return Uni.createFrom().item(service.greeting(name))
    }

    @GET
    @Path("/delayed/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    fun delayed(@PathParam("name") name: String): Uni<Greeting> {
        return Uni.createFrom().item(service.greeting(name))
            .onItem().delayIt().by(Duration.ofSeconds(1))
    }

    @GET
    @Path("/timeout/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    fun delayedTimeout(@PathParam("name") name: String): Uni<Greeting> {
        return Uni.createFrom().item { service.greeting(name) }
            .onItem().delayIt().by(Duration.ofSeconds(2))
            .ifNoItem().after(Duration.ofSeconds(1))
            .fail()
            .onFailure().recoverWithUni(
                Uni.createFrom().failure(BadRequestException("Request timed out before completing"))
            )
    }
}
