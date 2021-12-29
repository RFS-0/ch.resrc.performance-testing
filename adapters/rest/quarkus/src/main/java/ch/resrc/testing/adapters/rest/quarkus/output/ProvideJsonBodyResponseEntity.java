package ch.resrc.testing.adapters.rest.quarkus.output;

import javax.ws.rs.core.Response;

public interface ProvideJsonBodyResponseEntity<T> {

    Response asResponseEntity();

}
