package ch.resrc.testing.products.spring_boot.app_config.adapters.rest;

import ch.resrc.testing.adapters.rest.spring_boot.SpringRestAdapterConfig;
import ch.resrc.testing.capabilities.json.*;
import org.springframework.context.annotation.*;

@Configuration
@Import({SpringRestAdapterConfig.class})
public class TestingRestConfig {

    @Bean
    Json json() {
        return JsonCapability.json();
    }

}
