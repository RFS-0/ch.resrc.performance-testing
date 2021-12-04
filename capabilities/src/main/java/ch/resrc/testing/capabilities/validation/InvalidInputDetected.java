package ch.resrc.testing.capabilities.validation;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Thrown if some validation finds that a one or more {@link Input}s represent invalid values
 * in a given context.
 */
public class InvalidInputDetected extends RuntimeException {

    private final List<ValidationError> validationErrors = new ArrayList<>();

    private InvalidInputDetected(Collection<ValidationError> validationErrors) {
        this.validationErrors.addAll(validationErrors);
    }

    private InvalidInputDetected(InvalidInputDetected other, Throwable cause) {
        super(cause);
        this.validationErrors.addAll(other.validationErrors);
    }

    public static InvalidInputDetected of(ValidationError validationError) {
        return new InvalidInputDetected(List.of(validationError));
    }

    public static InvalidInputDetected of(Collection<ValidationError> validationErrors) {
        return new InvalidInputDetected(validationErrors);
    }

    public InvalidInputDetected withCause(Throwable cause) {
        return new InvalidInputDetected(this, cause);
    }

    public InvalidInputDetected map(Function<ValidationError, ValidationError> f) {
        return new InvalidInputDetected(validationErrors.stream()
                                                        .map(f)
                                                        .collect(Collectors.toList()));
    }

    public List<ValidationError> errors() {
        return List.copyOf(validationErrors);
    }

    @Override
    public String getMessage() {
        return validationErrors.stream()
                               .map(ValidationError::errorMessage)
                               .collect(Collectors.joining("; "));
    }

}
