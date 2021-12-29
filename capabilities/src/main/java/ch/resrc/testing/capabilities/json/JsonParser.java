package ch.resrc.testing.capabilities.json;

import ch.resrc.testing.capabilities.error_handling.faults.*;
import ch.resrc.testing.capabilities.result.Result;
import ch.resrc.testing.capabilities.validation.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.exc.*;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Optional;
import java.util.stream.Collectors;

class JsonParser {

    private final ObjectMapper objectMapper;

    public JsonParser(ObjectMapper objectMapper) {

        this.objectMapper = objectMapper;
    }

    public <T> Result<T, ValidationError> parsingResult(Input<String> input, Class<T> targetType) {

        try {

            return Result.success(objectMapper.readValue(input.value(), targetType));

        } catch (MismatchedInputException bad) {

            String propertyPath = extractPropertyPathFrom(bad);

            return Result.failure(input.invalidate("Illegal JSON value")
                                       .butOrigin(propertyPath)
            );

        } catch (InvalidDefinitionException bad) {

            throw OurFault.of(bad);

        } catch (JsonMappingException bad) {

            String propertyPath = extractPropertyPathFrom(bad);

            return Result.failure(input.invalidate("Invalid JSON").butOrigin(propertyPath));

        } catch (JsonParseException bad) {

            return Result.failure(input.invalidate("Invalid JSON"));

        } catch (Exception bad) {
            throw Defect.of(bad);
        }
    }

    private <X extends Exception> Optional<X> findInCauseChainOf(Exception bad, Class<X> causeType) {

        return ExceptionUtils.getThrowableList(bad).stream()
                             .filter(cause -> causeType.isAssignableFrom(cause.getClass()))
                             .map(causeType::cast)
                             .findFirst();
    }

    private String extractPropertyPathFrom(JsonMappingException bad) {

        return bad.getPath().stream()
                  .map(JsonMappingException.Reference::getFieldName)
                  .collect(Collectors.joining("."));
    }
}
