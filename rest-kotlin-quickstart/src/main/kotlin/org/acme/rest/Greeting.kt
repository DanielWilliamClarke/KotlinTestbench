package org.acme.rest

import jakarta.enterprise.context.ApplicationScoped

data class Greeting(val message: String = "")

@ApplicationScoped
class GreetingService {
    fun greeting(name: String): Greeting {
        return Greeting("hello $name")
    }
}
