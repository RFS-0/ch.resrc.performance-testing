package ch.resrc.testing.capabilities.validation;


import ch.resrc.testing.capabilities.result.*;

import java.util.function.*;

/**
 * Validates an object and returns the validation result as a {@link Result} object.
 *
 * @param <E> the error type
 * @param <T> the type of the validated object
 */
@FunctionalInterface
public interface Validation<T,E> extends Function<T, Result<T,E>>  {

    /**
     * Applies the validation to the given value.
     *
     * @param toBeValidated the object to be validated
     * @return a {@code Result} object that contains the validated object if it is valid or all validation
     *         errors if it is invalid.
     */
    Result<T,E> applyTo(T toBeValidated);

    default Result<T,E> apply(T toBeValidated) {
        return applyTo(toBeValidated);
    }

    default <EE> Validation<T,EE> mapErrors(Function<E,EE> errorMap) {
        return this.asFunction().andThen(validated -> validated.mapErrors(errorMap))::apply;
    }

    default Function<T, Result<T,E>> asFunction() {
        return this::applyTo;
    }
 }
