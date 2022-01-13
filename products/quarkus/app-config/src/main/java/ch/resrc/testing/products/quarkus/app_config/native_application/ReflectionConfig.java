package ch.resrc.testing.products.quarkus.app_config.native_application;

import ch.resrc.testing.adapters.rest.endpoints.sort.dto.*;
import ch.resrc.testing.adapters.rest.errorhandling.ErrorDto;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection(targets = {
        UnsortedListDto.class,
        SortedListDto.class,
        ErrorDto.class,
})
public class ReflectionConfig {
}
