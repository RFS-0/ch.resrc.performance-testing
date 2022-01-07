package ch.resrc.testing.adapters.rest.spring_boot.errorhandling;

import ch.resrc.testing.adapters.rest.errorhandling.*;
import ch.resrc.testing.capabilities.authentication.Client;
import ch.resrc.testing.capabilities.error_handling.*;
import ch.resrc.testing.capabilities.error_handling.faults.*;
import ch.resrc.testing.capabilities.json.*;
import ch.resrc.testing.capabilities.validation.InvalidInputDetected;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;
import java.util.stream.Collectors;

import static ch.resrc.testing.adapters.rest.errorhandling.RestProblem.HTTP_METHOD_NOT_SUPPORTED;
import static ch.resrc.testing.use_cases.support.habits.errorhandling.UseCaseProblem.INVALID_INPUT_DETECTED;
import static java.util.Objects.requireNonNullElse;


@ControllerAdvice
public class SpringErrorHandling {

    private static final Logger log = LoggerFactory.getLogger(SpringErrorHandling.class);

    private final ProblemCatalogue problemCatalogue;

    private final Json json;

    @Autowired
    public SpringErrorHandling(ProblemCatalogue problemCatalogue, Json json) {
        this.json = json;
        this.problemCatalogue = problemCatalogue;
    }

//    @ExceptionHandler(value = Exception.class)
//    public ResponseEntity<JsonBody<ErrorDto>> handle(Exception bad) {
//        return handleFault(OurFault.of(bad));
//    }

    @ExceptionHandler
    public ResponseEntity<JsonBody<ErrorDto>> handleFault(Fault bad) {
        var errorPresenter = new ErrorResponse(problemCatalogue, json);
        if (bad instanceof ClientFault) {
            errorPresenter.presentBusinessError(Client.anonymous(), (ClientFault) bad);
        } else {
            errorPresenter.presentSystemFailure(Client.anonymous(), bad);
        }

        return errorPresenter.asResponseEntity();
    }

    @ExceptionHandler
    public ResponseEntity<JsonBody<ErrorDto>> handleInvalidInputDetected(InvalidInputDetected bad) {
        return handleFault(ClientFault.of(ProblemDiagnosis.of(INVALID_INPUT_DETECTED)
                                                          .withContext("validationMessage", bad.getMessage())));
    }

    @ExceptionHandler(value = NoHandlerFoundException.class)
    public ResponseEntity<JsonBody<ErrorDto>> handle(NoHandlerFoundException bad) {

        return handleFault(ClientFault.of(ProblemDiagnosis.of(RestProblem.RESOURCE_NOT_FOUND)
                                                          .withContext("httpMethod", bad.getHttpMethod())
                                                          .withContext("path", bad.getRequestURL())
                                                          .withCause(bad))
        );
    }

    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<JsonBody<ErrorDto>> handle(HttpRequestMethodNotSupportedException bad) {

        // Sorter the supported methods so that we can reliably assert error messages in tests.
        var supportedMethods = requireNonNullElse(bad.getSupportedHttpMethods(), List.<HttpMethod>of())
                .stream()
                .map(HttpMethod::toString)
                .sorted()
                .collect(Collectors.joining(", "));

        return handleFault(ClientFault.of(ProblemDiagnosis.of(HTTP_METHOD_NOT_SUPPORTED)
                                                          .withContext("unsupportedHttpMethod", bad.getMethod())
                                                          .withContext("supportedMethods", supportedMethods)
                                                          .withCause(bad)));
    }

    @ExceptionHandler(value = HttpMessageConversionException.class)
    public ResponseEntity<JsonBody<ErrorDto>> handle(HttpMessageConversionException bad) {
        return handleFault(ClientFault.of(ProblemDiagnosis.of(INVALID_INPUT_DETECTED)
                                                          .withContext("validationMessage", "Request could not be parsed.")
                                                          .withCause(bad)
        ));
    }
}
