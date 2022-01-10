package ch.resrc.testing.adapters.rest.quarkus.marshalling;

import ch.resrc.testing.capabilities.json.*;
import com.google.common.io.CharStreams;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.*;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * When using RESTEasy Reactive do the following:
 * <ul>
 *     <li>
 *         Replace <pre>{@code MessageBodyReader<JsonBody>}</pre> with <pre>{@code ServerMessageBodyReader<JsonBody>}</pre>
 *     </li>
 *     <li>
 *         Replace <pre>{@code MessageBodyWriter<JsonBody>}</pre> with <pre>{@code ServerMessageBodyWriter<JsonBody>}</pre>
 *     </li>
 *     <li>
 *         Remove ch.resrc.testing.adapters.rest.quarkus.marshalling.JsonBodyMarshaller#writeTo(ch.resrc.testing.capabilities.json.JsonBody, java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, java.io.OutputStream)
 *     </li>
 *     <li>
 *         Uncomment methods
 *     </li>
 * </ul>
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@SuppressWarnings("rawtypes")
public class JsonBodyMarshaller implements MessageBodyReader<JsonBody>, MessageBodyWriter<JsonBody> {

    private final Json json = JsonCapability.json();

    public JsonBodyMarshaller() {
    }

    @Override
    public boolean isReadable(Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return isAssignableFrom(type);
    }

    @Override
    public JsonBody<?> readFrom(Class type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        return read(entityStream);
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return isAssignableFrom(type);
    }

    @Override
    public void writeTo(JsonBody jsonBody, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        write(entityStream, jsonBody);
    }

//    @Override
//    public boolean isReadable(Class<?> type, Type genericType, ResteasyReactiveResourceInfo lazyMethod, MediaType mediaType) {
//        return isAssignableFrom(type);
//    }

//    @Override
//    public JsonBody readFrom(Class<JsonBody> type, Type genericType, MediaType mediaType, ServerRequestContext context) throws WebApplicationException, IOException {
//        return read(context.getInputStream());
//    }

//    @Override
//    public boolean isWriteable(Class<?> type, Type genericType, ResteasyReactiveResourceInfo target, MediaType mediaType) {
//        return isAssignableFrom(type);
//    }

//    @Override
//    public void writeResponse(JsonBody jsonBody, Type genericType, ServerRequestContext context) throws WebApplicationException, IOException {
//        write(context.getOrCreateOutputStream(), jsonBody);
//    }
//
//    @Override
//    public void writeTo(JsonBody jsonBody, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
//        write(entityStream, jsonBody);
//    }

    private boolean isAssignableFrom(Class<?> type) {
        return JsonBody.class.isAssignableFrom(type);
    }

    private JsonBody read(InputStream context) throws IOException {
        BufferedReader requestReader = new BufferedReader(
                new InputStreamReader(context, StandardCharsets.UTF_8)
        );

        String body = CharStreams.toString(requestReader);

        return JsonBody.requestBodyOf(body, json);
    }

    private void write(OutputStream context, JsonBody jsonBody) throws IOException {
        context.write(jsonBody.toString().getBytes(StandardCharsets.UTF_8));
    }
}
