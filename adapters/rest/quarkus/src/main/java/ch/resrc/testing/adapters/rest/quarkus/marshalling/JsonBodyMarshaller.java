package ch.resrc.testing.adapters.rest.quarkus.marshalling;

import ch.resrc.testing.capabilities.json.*;
import com.google.common.io.CharStreams;
import org.jboss.resteasy.reactive.server.spi.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.Provider;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

@Provider
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@SuppressWarnings("rawtypes")
public class JsonBodyMarshaller implements ServerMessageBodyReader<JsonBody>, ServerMessageBodyWriter<JsonBody> {

    private final Json json = JsonCapability.json();

    public JsonBodyMarshaller() {
    }

    @Override
    public boolean isReadable(Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return isAssignableFrom(type);
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, ResteasyReactiveResourceInfo lazyMethod, MediaType mediaType) {
        return isAssignableFrom(type);
    }

    @Override
    public JsonBody<?> readFrom(Class type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        return read(entityStream);
    }

    @Override
    public JsonBody readFrom(Class<JsonBody> type, Type genericType, MediaType mediaType, ServerRequestContext context) throws WebApplicationException, IOException {
        return read(context.getInputStream());
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, ResteasyReactiveResourceInfo target, MediaType mediaType) {
        return isAssignableFrom(type);
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return isAssignableFrom(type);
    }

    @Override
    public void writeResponse(JsonBody jsonBody, Type genericType, ServerRequestContext context) throws WebApplicationException, IOException {
        write(context.getOrCreateOutputStream(), jsonBody);
    }

    @Override
    public void writeTo(JsonBody jsonBody, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        write(entityStream, jsonBody);
    }

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
