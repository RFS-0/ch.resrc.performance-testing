package ch.resrc.testing.capabilities.json;


import ch.resrc.testing.capabilities.result.Result;
import ch.resrc.testing.capabilities.validation.*;

import java.util.Map;

import static ch.resrc.testing.capabilities.validation.ValidationError.Claim.*;
import static ch.resrc.testing.capabilities.validation.ValidationErrorModifier.*;
import static java.util.Objects.requireNonNull;

/**
 * Represents the Json body of a REST request or response that is derived from some object
 * representation. The object representation can be parsed from the string body.
 *
 * <p>This object wraps the String of the REST request or response body. It assumes that the String is
 * valid Json. It does not hold any object representation of the REST body.
 * But it is able to parse the String into an object representation using some {@link Json} implementation.</p>
 *
 * <p>This object exists so that we can fully control the parsing and marshalling of Json bodies in our
 * REST resources. No container such as Spring is required for that. This allows us to fully unit test the parsing
 * and marshalling behaviour of our REST resources without launching a container.
 * At the same time we can still communicate the structure of the REST bodies in the method signatures of the REST controller
 * through the type parameter of this object</p>
 *
 * <p>Method signatures are designed in such a way that it is impossible to parse or marshal the body into anything else
 * than the type declared in the type parameter. With the exception of error responses. Error responses can have any
 * structure, even a non-Json structure.</p>
 * <p>
 * Moreover, any {@code JsonBody} can be parsed into a map, because a JSON string is
 * always representable by a {@code Map<String,Object>}</p>
 *
 * <p>You need to configure a container-specific message converter for {@code JsonBody} so that the container can
 * instantiate a {@code JsonBody} from the REST request and serialize it from the REST response.
 * Such message converters are simple. They just use the {@code toString} method to get the response
 * body for output. Or they instantiate a {@code JsonBody.Input}, providing the
 * string body of the request to it and a {@link Json} implementation.
 * The actual parsing or marshalling is done by this object in the context of the REST handler method.</p>
 *
 * @param <T> the type of the object representation of the Json body.
 */
@SuppressWarnings("rawtypes")
public abstract class JsonBody<T> {

    private final String content;

    final Json json;

    private JsonBody(String content, Json json) {

        this.content = requireNonNull(content);
        this.json = json;
    }

    public T parse(Class<T> type) {

        return json.parsingResult(Input.of(content(), "body"), type).getOrThrow(InvalidInputDetected::of);
    }

    public Result<T, ValidationError> parsingResult(Class<T> type) {

        return json.parsingResult(Input.of(content()), type)
                   // The JSON string "null" is parsed into null. Null makes no sense as a Json body. Reject it:
                   .andThen(Validations.notNull(origin("body"),
                                                claimed(SAFE_VALUE),
                                                claimRevoked(UNINTERESTING_VALUE))
                   ).mapErrors(origin("body"));
    }

    public Map parseMap() {

        return json.parsingResult(Input.of(content(), "body"), Map.class).getOrThrow(InvalidInputDetected::of);
    }

    public <U> JsonBody<U> castToResponse(Class<U> responseType) {

        return new Response<>(this.content, this.json);
    }

    @Override
    public String toString() {

        return content;
    }

    public String content() { return content; }


    public static <T> Request<T> requestBodyOf(String body, Json json) {

        return new Request<>(body, json);
    }

    public static <T> Response<T> responseBodyOf(T body, Json json) {

        return new Response<>(body, json);
    }

    public static <T> ErrorResponse<T> errorBodyOf(String body, Json json) {

        return new ErrorResponse<>(body, json);
    }

    public static <T, E> ErrorResponse<T> errorBodyOf(E body, Json json) {

        return new ErrorResponse<>(json.toJsonString(body), json);
    }

    static final class Request<T> extends JsonBody<T> {

        Request(String body, Json json) {

            super(body, json);
        }

    }

    static final class Response<T> extends JsonBody<T> {

        private Response(String body, Json json) {

            super(body, json);
        }

        Response(T body, Json json) {

            super(json.toJsonString(body), json);
        }
    }

    final static class ErrorResponse<T> extends JsonBody<T> {

        ErrorResponse(String errorBody, Json json) {

            super(errorBody == null ? "" : errorBody, json);
        }
    }


}
