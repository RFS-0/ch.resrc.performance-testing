package ch.resrc.testing.adapters.rest.spring_boot.output;

import ch.resrc.testing.capabilities.json.*;
import com.google.common.io.CharStreams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.converter.*;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Component
public class JsonBodyHttpMessageConverter extends AbstractHttpMessageConverter<JsonBody> {

    private final Json json;

    @Autowired
    JsonBodyHttpMessageConverter(Json json) {
        super(MediaType.APPLICATION_JSON);
        this.json = json;
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return JsonBody.class.isAssignableFrom(clazz);
    }

    @Override
    protected JsonBody<?> readInternal(Class<? extends JsonBody> clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {

        BufferedReader requestReader = new BufferedReader(
                new InputStreamReader(inputMessage.getBody(), StandardCharsets.UTF_8)
        );

        String body = CharStreams.toString(requestReader);

        return JsonBody.requestBodyOf(body, json);
    }

    @Override
    protected void writeInternal(JsonBody output, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        outputMessage.getBody().write(output.toString().getBytes(StandardCharsets.UTF_8));
    }
}
