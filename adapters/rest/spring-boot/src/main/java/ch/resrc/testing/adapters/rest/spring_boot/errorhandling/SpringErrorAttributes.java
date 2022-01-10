package ch.resrc.testing.adapters.rest.spring_boot.errorhandling;

import ch.resrc.testing.adapters.rest.errorhandling.ErrorDto;
import ch.resrc.testing.capabilities.authentication.Client;
import ch.resrc.testing.capabilities.error_handling.*;
import ch.resrc.testing.capabilities.error_handling.faults.*;
import ch.resrc.testing.capabilities.json.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.*;

import java.util.Map;

import static ch.resrc.testing.capabilities.error_handling.GenericProblem.*;

/**
 * Customizes the error response generated by Spring Boot's {@link BasicErrorController}
 * such that it conforms to our {@link ErrorDto}. The {@code BasicErrorController}
 * picks up and uses this object for error response rendering instead of its
 * {@link DefaultErrorAttributes}.
 *
 * <p>This object only handles errors that arrive at the {@code BasicErrorController}.
 * For example errors that arise in the filter chain that are not delegated to our
 * {@code DefaultExceptionHandler}. Exceptions raised by our REST controllers are handled
 * by the {@code DefaultExceptionHandler}.
 * </p>
 *
 * <p>Errors that are processed by this object are formulated in terms of HTTP response status codes.
 * This object translates the response codes back to our problem codes.</p>
 */
@Component
public class SpringErrorAttributes implements ErrorAttributes {

    private static final Logger LOG = LoggerFactory.getLogger(SpringErrorAttributes.class);

    private final ProblemCatalogue problemCatalogue;
    private final Json json;

    private static final Map<String, Object> FAIL_SILENT_ERROR_ATTRIBUTES = Map.of();

    @Autowired
    public SpringErrorAttributes(ProblemCatalogue problemCatalogue, Json json) {

        this.problemCatalogue = problemCatalogue;
        this.json = json;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions opts) {

        try {
            Throwable error = getError(webRequest);

            var httpStatus = getStatus(webRequest);

            var errorResponse = new ErrorResponse(problemCatalogue, json);

            switch (httpStatus) {
                case UNAUTHORIZED:
                case NOT_FOUND:
                case FORBIDDEN:
                case BAD_REQUEST:
                    errorResponse.presentBusinessError(
                            Client.anonymous(), ClientFault.of(ProblemDiagnosis.of(BAD_REQUEST).withCause(error))
                    );
                    break;
                default:
                    errorResponse.presentSystemFailure(
                            Client.anonymous(), OurFault.of(ProblemDiagnosis.of(SYSTEM_FAILURE).withCause(error))
                    );
            }

            JsonBody<?> errorJson = errorResponse.asResponseEntity().getBody();
            return errorJson.parseMap();

        } catch (RuntimeException bad) {
            LOG.error("Failed to make error attributes for web request: {}", webRequest.getDescription(true), bad);
            return FAIL_SILENT_ERROR_ATTRIBUTES;
        }
    }

    private HttpStatus getStatus(RequestAttributes requestAttributes) {

        Integer status = getAttribute(requestAttributes, "javax.servlet.error.status_code");
        if (status == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        try {
            return HttpStatus.valueOf(status);
        } catch (Exception ex) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    @Override
    public Throwable getError(WebRequest webRequest) {

        return getAttribute(webRequest, "javax.servlet.error.exception");
    }

    private String getErrorMessage(WebRequest webRequest) {

        return getAttribute(webRequest, "javax.servlet.error.message");
    }

    @SuppressWarnings("unchecked")
    private <T> T getAttribute(RequestAttributes requestAttributes, String name) {

        return (T) requestAttributes.getAttribute(name, RequestAttributes.SCOPE_REQUEST);
    }
}