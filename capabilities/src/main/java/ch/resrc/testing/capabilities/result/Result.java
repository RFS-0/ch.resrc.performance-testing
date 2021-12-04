package ch.resrc.testing.capabilities.result;


import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static java.lang.String.*;
import static java.util.stream.Collectors.*;

/**
 * Represents the result of an operation that might fail with one or more errors.
 * Holds the result object of the operation if the operation was successful. Holds all errors if the operation has failed.
 * <p>
 * Accumulates the errors of multiple results.
 *
 * @param <T> the result object type of the operation
 * @param <E> the type of the errors
 */
public abstract class Result<T, E> {

    public static <T, E> Result<T, E> success(T value) {return new Success<>(value);}

    public static <T, E> Result<T, E> failure(E error) {return new Failure<>(List.of(error));}

    public static <T, E> Result<T, E> failure(Collection<? extends E> errors) {return new Failure<>(List.copyOf(errors));}

    public static <E> Result<Void, E> voidSuccess() {return new VoidSuccess<>();}

    public static <E> Result<Void, E> voidFailure(E error) {return new VoidFailure<>(List.of(error));}

    public static <E> Result<Void, E> voidFailure(Collection<? extends E> errors) {return new VoidFailure<>(List.copyOf(errors));}

    public static <T, E> Result<T, E> empty() {return new EmptySuccess<>();}

    /**
     * Applies the given mapping function to each element of the supplied collection.
     * If all elements map to a Success, a Success result is returned with the list
     * of the mapped values. If any of the elements maps to a Failure, a Failure is
     * returned that contains the errors of all elements that mapped to a Failure.
     *
     * @param elements the items to be mapped
     * @param mapping  applied to each element
     * @param <T>      the type of the source elements
     * @param <U>      the type of the target elements
     * @param <E>      the error type of the mapping
     * @return a {@code Result}, as explained
     */
    public static <T, U, E> Result<List<U>, E> ofMapping(Collection<T> elements, Function<T, Result<U, E>> mapping) {

        List<Result<U, E>> results = elements.stream()
                                             .map(mapping)
                                             .collect(toList());

        return Precondition.of(results)
                           .thenValueOf(() -> valuesOf(results));
    }

    /**
     * Returns a {@link Collector} that combines a stream of results into a VoidResult.
     * The VoidResult is a VoidSuccess if all results of the stream are Successes.
     * The VoidResult is a VoidFailure if any of the results in the stream is a Failure.
     * The errors of the VoidFailure are the union of all Failure results in the stream.
     *
     * @param <E> the error type of the results in the stream
     * @return a {@link Collector}, as explained
     */
    public static <E> Collector<Result<?, E>, ?, Result<Void, E>> combinedToVoidResult() {

        return Collectors.collectingAndThen(toList(), x -> Precondition.of(x).thenVoidSuccess());
    }

    /**
     * Returns a {@link Collector} that combines a stream of results into a result of a list.
     * The list contains the values of all results in the stream.
     * <p>
     * The result is a Success if all results in the stream are Successes.
     * The result is a Failure if any of the results in the stream is a Failure.
     * The errors of the Failure are the union of all Failure results in the stream.
     *
     * @param <T> the value type of the results in the stream
     * @param <E> the error type of the results in the stream
     * @return a {@link Collector}, as explained
     */
    public static <T, E> Collector<Result<T, E>, ?, Result<List<T>, E>> combinedToListResult() {

        return Collectors.collectingAndThen(
                toList(),
                (List<Result<T, E>> x) -> Precondition.of(x).thenValueOf(() -> valuesOf(x))
        );
    }

    public static <T, E> Result<List<T>, E> combinedToListResult(Collection<Result<T, E>> results) {

        return results.stream().collect(combinedToListResult());
    }

    /**
     * Tells whether this object is a success result. Note that an empty result
     * is also a success result.
     *
     * @return true, if this object is a Success. False otherwise.
     */
    public abstract boolean isSuccess();

    /**
     * Tells whether this object is a failure result. A failure result
     * contains at least one error.
     *
     * @return true, if this object is a Failure. False otherwise.
     */
    public abstract boolean isFailure();

    /**
     * Tells whether this object is an empty result. Note that an empty result
     * is also a success result.
     *
     * @return true, if this object is empty. False otherwise.
     */
    public abstract boolean isEmpty();

    /**
     * @return the result object if the result is a Success
     * @throws NoSuchElementException if this object is a Failure
     */
    public abstract T value() throws NoSuchElementException;

    public <U> U value(Function<? super T, U> f) throws NoSuchElementException {

        return f.apply(value());
    }

    /**
     * @return all errors if the result is a Failure. An empty list if the result is a Success.
     */
    public abstract List<E> errors();

    /**
     * Maps the result object to another object if the result is a Success.
     * The new result is a Success, too. If this result is a Failure, the mapping
     * function is not invoked.
     *
     * @param f   the mapping function
     * @param <U> the type of the mapping target
     * @return a new instance for the mapped object
     */
    public abstract <U> Result<U, E> map(Function<T, U> f);

    public abstract <U> Result<U, E> flatMap(Function<T, Result<U, E>> f);

    public <U, EE> Result<U, E> flatMap(Function<T, Result<U, EE>> f, Function<? super EE, E> fe) {

        if (isEmpty()) {
            return Result.empty();
        } else if (isSuccess()) {
            return f.apply(value()).mapErrors(fe);
        } else {
            return Result.failure(errors());
        }
    }

    public <U, EE, EEE> Result<U, EEE> flatMap(Function<T, Result<U, EE>> f, Class<EEE> errorType) {

        if (isEmpty()) {
            return Result.empty();
        } else if (isSuccess()) {
            return f.apply(value()).castErrors(errorType);
        } else {
            return Result.failure(this.castErrors(errorType).errors());
        }
    }

    /**
     * Maps a Collection of Results to a list of the values of the Results.
     *
     * @param results the results to map
     * @param <T>     the value type of the results
     * @param <E>     the error type of the results
     * @return the list of values
     * @throws NoSuchElementException if any of the results is a Failure
     */
    public static <T, E> List<T> valuesOf(Collection<Result<T, E>> results) throws NoSuchElementException {

        return results.stream().map(Result::value).collect(toList());
    }

    /**
     * Maps a collection of Results to a list of their values but only for the success results.
     * The result list may therefore be smaller than the input collection.
     *
     * @param <T> the value type of the results
     * @param <E> the error type of the results
     * @return the list of values
     */
    public static <T, E> List<T> successValuesOf(Collection<Result<T, E>> results) {

        return results.stream().filter(Result::isSuccess).map(Result::value).collect(toList());
    }

    /**
     * Syntactic sugar. Alias for {@link #flatMap(Function)}.
     *
     * @param f the mapping function to apply
     * @return the mapping result or this object depending on whether this object is a Success or Failure
     */
    public <U> Result<U, E> andThen(Function<T, Result<U, E>> f) {

        return this.flatMap(f);
    }

    /**
     * Maps the errors to a different error representation. Does nothing if this result is a Success.
     *
     * @param f    maps a single error to the new error representation. Invoked for each error.
     * @param <EE> the type of the new error representation
     * @return a new instance of this object with all errors mapped as prescribed
     */
    public abstract <EE> Result<T, EE> mapErrors(Function<? super E, EE> f);

    public Result<T, E> mapEmptyToError(Supplier<E> error) {

        if (this.isEmpty()) {
            return Result.failure(error.get());
        } else {
            return this;
        }
    }

    /**
     * Casts all errors to the given type if this object is a Failure.
     *
     * @param targetType the type to cast to
     * @param <EE>       the target type
     * @return a new result with the given error type.
     */
    public <EE> Result<T, EE> castErrors(Class<EE> targetType) {

        return mapErrors(targetType::cast);
    }

    /**
     * Applies the given error handling to the errors of this object.
     * The error handling may have side effects and can throw exceptions as an error handling strategy.
     * <p>
     * The error handling is only invoked if this object is a failure.
     *
     * @param errorHandling the error handling to apply
     * @param <EE>          the error type of the error handling result
     * @return the result produced by the error handling
     * @throws RuntimeException if the error handling decides to throw an exception
     */
    public abstract <EE> Result<T, EE> handleErrors(ErrorHandling<T, E, EE> errorHandling) throws RuntimeException;

    /**
     * Returns the result value if the result is a Success. Throws the specified exception if it is a Failure.
     *
     * @param errorMap converts any errors to the exception that should be thrown if the result is a Failure.
     * @return the result value, if the result is a Success.
     * @throws RuntimeException (as defined by the errorMap) if the result is a Failure.
     */
    public abstract T getOrThrow(Function<List<E>, ? extends RuntimeException> errorMap) throws RuntimeException;


    /**
     * Throws the specified exception if the result is a Failure.
     * Does nothing if the result is a Success.
     *
     * @param errorMap converts the errors to the exception that should be thrown if this object is a Failure
     */
    public abstract Result<T, E> ifFailureThrow(Function<List<E>, ? extends RuntimeException> errorMap);

    /**
     * Returns the provided object as a Success result if this object is a Failure.
     * Has no effect if this object is already a Success.
     *
     * @param alternativeValue the alternative value to use if this object is a Failure
     * @return the possibly alternative Success, as explained
     */
    public abstract <U extends T> Result<T, E> recover(U alternativeValue);

    public abstract <U extends T> Result<T, E> recoverEmpty(Supplier<U> alternative);

    public abstract Result<T, E> ifEmptySwitch(Supplier<Result<T, E>> alternative);

    public abstract Result<T, E> failIfSuccess(E error);

    /**
     * Returns a new Success with the specified value if this object is a Success.
     * Returns a Failure with the errors of this object if this object is a Failure.
     *
     * @param supplier supplies the value of the new Success
     * @param <U>      the type of the new Success value
     * @return the new Success or Failure as explained
     */
    public <U> Result<U, E> thenValueOf(Supplier<U> supplier) {

        return thenResultOf(() -> Result.success(supplier.get()));
    }

    /**
     * Returns a new VoidSuccess if this object is a Success.
     * Returns a VoidFailure with the errors of this object if this object is a Failure.
     *
     * @return the new VoidSuccess or VoidFailure as explained
     */
    public Result<Void, E> thenVoidSuccess() {

        return thenResultOf(Result::voidSuccess);
    }

    /**
     * Invokes the given supplier and returns its result if this object is a Success.
     * Returns a Failure with the errors of this object if this object is a Failure.
     *
     * @param supplier supplies the new result
     * @param <U>      the value type of the new result
     * @return the new result or Failure as explained
     */
    public abstract <U> Result<U, E> thenResultOf(Supplier<Result<U, E>> supplier);

    /**
     * Executes an effect if this object is a Success. Does nothing if this object is a Failure
     *
     * @param effect the effect to execute
     * @return this object
     */
    public abstract Result<T, E> effect(Runnable effect);

    /**
     * Executes an effect on the value of this object if this object is a Success.
     * Does nothing if this object is a Failure
     *
     * @param effect the effect to execute
     * @return this object
     */
    public abstract Result<T, E> effect(Consumer<T> effect);

    /**
     * Executes an effect for the errors of this object if this object is a Failure.
     * Does nothing if this object is a Success.
     *
     * @param effect the effect to execute for the list of errors
     * @return this object
     */
    public Result<T, E> failureEffect(Consumer<List<E>> effect) {

        if (isFailure()) {
            effect.accept(errors());
        }

        return this;
    }


    static UnsupportedOperationException unsupportedOperation(String resultType) {

        return new UnsupportedOperationException(format(
                "This is a %s result. It does not make sense to call this operation for this result type.",
                resultType));
    }

}
