package ch.resrc.testing.capabilities.json;

import ch.resrc.testing.capabilities.result.Result;
import ch.resrc.testing.capabilities.validation.*;

/**
 * Implementations are able to serialize objects to Json strings and to deserialize Json strings
 * into objects.
 * <p>
 * This interface serves to decouple code that needs to do Json (de)serialization from the concrete
 * Json technology used in the application so that it can be easily exchanged.
 * </p>
 */
public interface Json {

    String toJsonString(Object object);

    <T> Result<T, ValidationError> parsingResult(Input<String> json, Class<T> type);

    default <T> T parse(String json, Class<? extends T> type) {
        return parsingResult(Input.of(json, ""), type)
                .getOrThrow(InvalidInputDetected::of);
    }
}
