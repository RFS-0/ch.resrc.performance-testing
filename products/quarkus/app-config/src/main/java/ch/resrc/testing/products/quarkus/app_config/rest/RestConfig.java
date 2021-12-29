package ch.resrc.testing.products.quarkus.app_config.rest;

import ch.resrc.testing.capabilities.json.*;

import javax.enterprise.context.ApplicationScoped;

public class RestConfig {

    @ApplicationScoped
    Json json() {
        return JsonCapability.json();
    }

}
