package ch.resrc.testing.capabilities.result;

import java.util.*;

/**
 * Handles the errors of a {@code Failure} by producing a new {@code Result} with a possibly different error content or
 * by throwing an exception.
 * <p>
 * Implementations make application specific error handling patterns reusable at different locations in the application.
 *
 * @param <T>  the value type of the {@code Result} whose errors are handled
 * @param <E>  the type of the errors that get handled by this object
 * @param <EE> the error type of the result
 */
public interface ErrorHandling<T, E, EE> {

    /**
     * Transforms the list of errors of a {@code Failure} result to a {@code Result} with a possibly different error
     * content. The transform may have side effects. In particular, it may throw exceptions depending on the
     * handled errors.
     * <p>
     * The list of errors to be handled is never empty. This method is called by the {@code Failure} whose errors
     * should be handled.
     *
     * @param errors the errors to be handled
     * @return a {@code Result} which can be a {@code Success} or {@code Failure}
     *
     * @throws RuntimeException if the error handling decides to throw an exception
     */
    Result<T, EE> apply(List<E> errors) throws RuntimeException;
}
