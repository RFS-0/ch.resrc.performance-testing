package ch.resrc.testing.adapters.rest.spring_boot.output;

import ch.resrc.testing.capabilities.json.JsonBody;
import org.springframework.http.ResponseEntity;

public interface ProvideJsonBodyResponseEntity<T> {

    ResponseEntity<JsonBody<T>> asResponseEntity();
}
