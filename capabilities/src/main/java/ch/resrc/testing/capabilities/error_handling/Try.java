package ch.resrc.testing.capabilities.error_handling;

import org.apache.commons.lang3.builder.*;

import java.util.*;
import java.util.function.*;

import static java.util.Objects.requireNonNull;

/**
 * A container object that represents the outcome of an operation. Either
 * <ul>
 *     <li>a successful outcome with a value,</li>
 *     <li>a successful outcome of a void operation without a value,</li>
 *     <li>a failure with an exception,</li>
 *     <li>an empty result if the operation produced no value but is not a void operation</li>
 * </ul>
 *
 * <p>{@code Try} is a similar concept like the Java {@link Optional}, but with the additional ability to represent a failure.
 * The failure {@code Try} object holds the exception that signaled the failure and provides convenient exception mapping.</p>
 *
 * <p>{@code Try} is useful because it provides a fluent interface that allows you to write typical mapping and failure
 * reaction sequences in a compact and readable way without the verbosity of try-catch blocks.</p>
 *
 * <p>If an operation produces a null value, that value is treated as an empty result. {@code Try} never returns null
 * when asked for the success value. Processing functions and consumers of the fluent interface are never passed a null value.</p>
 *
 * <p>Supports void operations that don't produce any result value. Successful completion of a void operation can be
 * represented by the {@code Result<Void>} object returned by {@link #voidSuccess()} or {@link Try#ofVoid(Runnable)}.
 * </p>
 *
 * <p>Result processing steps like maps and filters are only invoked on a success {@code Try}. Processing steps
 * are ignored on failure and empty {@code Try}s.</p>
 *
 * <p>Each processing step is checked for exceptions. If a processing step produces an exception, it turns the success
 * result, on which the step was invoked, into a failure result that contains the thrown exception. Any further processing
 * steps of your fluent call chain that follow the failing step are effectively skipped.</p>
 *
 * <p>So, as soon as a failure occurs in the processing chain, the chain is effectively short-circuited and no further
 * steps will be applied.</p>
 *
 * <p>The fluent interface together with the short-circuiting behaviour allows you to write code that emphasizes the happy
 * path of result processing, because it minimizes the clutter coming from failure handling code, such as try-catch blocks.</p>
 *
 * <p>Moreover, the {@code Try} object is a better fit for processing operation results using other functional APIs
 * like the Java streaming API. Failures are treated as special return values on equal footing with success return values.
 * The control flow in the stream is not interrupted by exceptions.</p>
 *
 * <p> Uses a "Gang of Four" State Machine pattern in order to represent success, failure and empty results. The different
 * result processing behaviours of these states are encapsulated in a dedicated class for each state.</p>
 *
 * @param <V> value type in case of success
 */
public abstract class Try<V> {


    /**
     * Creates a success result that contains the specified non-null value. Intended to be used by operations
     * as a return value to signal successful completion.
     *
     * <p>Use {@link #voidSuccess()} of {@link #empty()} if you feel tempted to pass a null value to represent
     * either a void result or an empty result.</p>
     *
     * @param value the value produced by the successful operation. Must not be null.
     * @param <V>   the value's type
     * @return a {@code Try} that represents a successful result and that contains the specified value
     * @throws NullPointerException if the value is null
     */
    public static <V> Try<V> success(V value) {
        return new SuccessWithValue<>(requireNonNull(value, "Value must not be null. Use voidSuccess() if you want to signal a successful void result."));
    }

    /**
     * Creates a success result for a void operation. Intended to be used by void operations as a return value
     * to signal successful completion.
     *
     * @return a {@code Result<Void>} that represents the success.
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public static Try<Void> voidSuccess() {
        return new VoidSuccess();
    }

    /**
     * Creates a failure result that contains the specified exception. Intended to be used by operations
     * as a return value to signal failure.
     *
     * @param failureSignal the exception that signalled the failure
     * @param <V>           the result value type if the operation had completed successfully
     * @return a {@code Try} that represents a failure and that contains the specified exception
     */
    public static <V> Try<V> failure(RuntimeException failureSignal) {
        return new Failure<>(failureSignal);
    }

    /**
     * Creates an empty result that contains no value. Use this as the result of an operation if the operation is not
     * a void operation and if it was unable to produce any result but if this does not necessarily mean a failure.
     *
     * <p>Use {@link #voidSuccess()} if the operation is a void operation that never produces a result value.</p>
     *
     * @param <V> the result value type if the operation had yielded a value
     * @return a {@code Try} that represents an empty result
     */
    @SuppressWarnings("WeakerAccess")
    public static <V> Try<V> empty() {
        return new Empty<>();
    }

    /**
     * Invokes the specified operation and produces a suitable {@code Try} that represents the outcome.
     *
     * <ul>
     * <li>A success {@code Try} if the operation completes with a non-null value</li>
     * <li>A failure {@code Try} if the operation throws an exception. The failure {@code Try} contains that exception.</li>
     * <li>An empty {@code Try} if the operation returns null</li>
     * </ul>
     *
     * @param operation the operation to invoke
     * @param <V>       the type of value returned by the operation
     * @return a {@code Try} that represents the outcome of the operation
     */
    public static <V> Try<V> of(Supplier<V> operation) {
        requireNonNull(operation, "Operation must not be null");
        try {
            V value = operation.get();
            return value != null ? success(value) : empty();
        } catch (RuntimeException bad) {
            return failure(bad);
        }
    }

    /**
     * Shortcut for {@code Result.of(() -> value)}. Use this if the return value of an operation is already
     * known and you want to process it with the fluent interface of {@code Try}.
     * The processing may turn the {@code Try} into a failure if one of the processing steps throws an exception.
     *
     * @param operationOutcome the value that the operation has produced
     * @param <V>              the type of the value that the operation produced
     * @return a success {@code Try} if the operation outcome is not null. An empty {@code Try} if the operation
     * outcome is null
     * @see #of(Supplier)
     */
    public static <V> Try<V> of(V operationOutcome) {
        return of(() -> operationOutcome);
    }

    /**
     * Invokes the specified operation and checks it for exceptions.
     *
     * @param operation the operation to invoke and check for exceptions
     * @return a success {@code Result<Void>} if the operation completes successfully. A failure {@code Try} if the
     * operation throws an exception. The failure {@code Try} contains that exception.
     */
    public static Try<Void> ofVoid(Runnable operation) {
        requireNonNull(operation, "Operation must not be null");
        try {
            operation.run();
            return new VoidSuccess();
        } catch (RuntimeException bad) {
            return failure(bad);
        }
    }

    /**
     * Invokes the specified operation and checks it for exceptions.
     *
     * @param operation the operation to invoke
     * @param <V>       the value type of the {@code Try} returned by the operation
     * @return the {@code Try} returned by the operation or a failure {@code Try} if the operation throws an exception.
     * The failure {@code Try} contains that exception.
     */
    public static <V> Try<V> ofFlat(Supplier<Try<V>> operation) {
        requireNonNull(operation, "Operation must not be null");
        try {
            return operation.get();
        } catch (RuntimeException bad) {
            return failure(bad);
        }
    }

    /**
     * Invokes the specified operation and interprets the returned {@code Optional} in terms of a
     * {@code Try}.
     *
     * @param operation the operation to invoke
     * @param <V>       the type of the {@code Optional}'s value that the operation produces
     * @return a successful {@code Try} with the {@code Optional}'s value if the {@code Optional} is non-empty. An
     * empty {@code Try} if the {@code Optional} is empty. A failure {@code Try} if the operation throws an exception.
     * The failure {@code Try} contains that exception.
     */
    public static <V> Try<V> ofOptional(Supplier<Optional<V>> operation) {
        requireNonNull(operation, "Operation must not be null");
        try {
            Optional<V> optional = operation.get();
            return optional.map(Try::success).orElse(Try.empty());
        } catch (RuntimeException bad) {
            return failure(bad);
        }
    }

    /**
     * Shortcut for {@code Result.of(() -> optional)}. Use this if the {@code Optional} outcome of an operation
     * is already known and you want to process the result using this object's fluent interface.
     *
     * @param operationOutcome the {@code Optional} that was produced by the operation
     * @param <V>              the type of the value of the {@code Optional}
     * @return a success {@code Try} with the {@code Optional}'s value if the {@code Optional} is non-empty. An empty
     * {@code Try} if the {@code Optional} is empty
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <V> Try<V> ofOptional(Optional<V> operationOutcome) {
        return ofOptional(() -> operationOutcome);
    }

    // ----------------------------------------------------------------------------------------------------
    //  Methods depending on success, failure or empty
    // ----------------------------------------------------------------------------------------------------

    /**
     * Applies the given mapping function to the result value if this object and checks it for exceptions.
     *
     * <p>If the result is a failure or empty, the mapping is not applied.</p>
     *
     * @param mapping maps a success value to another success value
     * @param <U>     the target type of the mapping
     * @return a new success {@code Try} with the mapped value if this {@code Try} is a success. This {@code Try}
     * if it is not a success. A failure {@code Try} if the mapping produces an exception. The failure {@code Try}
     * contains that exception.
     * @throws NoSuchElementException if invoked on a void success result
     */
    public abstract <U> Try<U> map(Function<V, U> mapping);

    /**
     * Turns a success {@code Try} into a new {@code Try} that represents the outcome of the
     * specified operation. Does nothing if this {@code Try} is not a success.
     *
     * @param operation the operation to invoke
     * @param <U>       the type of value produced by the operation
     * @return a success {@code Try} with the outcome of the operation. A failure {@code Try} if the operation
     * throws an exception. The failure {@code Try} contains that exception.
     * An empty {@code Try} if the operation returns null.
     */
    public abstract <U> Try<U> thenResultOf(Supplier<U> operation);

    /**
     * Applies the given function to the result value if this object is a success.
     * Returns the {@code Try} produced by the mapping.
     *
     * @param mapping maps the value of this {@code Try} to another {@code Try}
     * @param <U>     the value type of the mapped {@code Try}
     * @return the {@code Try} produced by the mapping or a failure {@code Try} if the mapping throws an exception.
     * The failure {@code Try} contains that exception.
     * @throws NoSuchElementException if invoked on a void success result
     */
    public abstract <U> Try<U> flatMap(Function<V, Try<U>> mapping);

    /**
     * Applies the specified side effect if this {@code Try} is a success and checks it for exceptions.
     * Does nothing if this result is not a success.
     *
     * <p>Use this method if you want to apply a side effect on a successful {@code Try} but not on an empty or failure
     * {@code Try}.</p>
     *
     * @param sideEffect the side effect to apply
     * @return this {@code Try} if the effect completes normally. A failure {@code Try} if the effect throws an
     * exception. The failure {@code Try} contains that exception.
     */
    public abstract Try<V> onSuccess(Runnable sideEffect);

    /**
     * Maps a failure to another failure whose exception is the result of applying the given mapping function.
     *
     * @param mapping maps the exception of this {@code Try} to another exception
     * @return a failure {@code Try} with the mapped exception if this {@code Try} is a failure. This {@code Try}
     * if it is not a failure. A new failure {@code Try} if the mapping throws an exception. The failure {@code Try}
     * contains that exception.
     */
    public abstract Try<V> mapFailure(Function<? super RuntimeException, ? extends RuntimeException> mapping);

    /**
     * Maps an empty result to a success whose value is the given value.
     * <p>
     * Returns the failure or success if the result is non-empty.
     *
     * @param value the value to map an empty result to. Must not be null.
     * @return a success {@code Try} with the given value or this {@code Try} if the result is non-empty
     * @throws NullPointerException if the value is null
     */
    public abstract Try<V> mapEmptyTo(V value);

    /**
     * Maps an empty {@code Try} to a failure {@code Try}. Use this if  an empty {@code Try}
     * means a failure in your context.
     *
     * @param failureSignal supplies the exception that represents the failure
     * @return a failure {@code Try} with the specified exception if this {@code Try} is empty. This {@code Try}
     * if it is non-empty. A failure {@code Try} if the failureSignal throws an exception. The failure {@code Try}
     * contains that exception.
     */
    public abstract Try<V> mapEmptyToFailure(Supplier<RuntimeException> failureSignal);

    /**
     * Returns the success value if present or the specified default value.
     *
     * @param defaultValue the value to return of this {@code Try} is not a success
     * @return the success value if this {@code Try} is a success. The specified default value if this {@code Try}
     * is empty or a failure or a void success result.
     */
    @SuppressWarnings("unused")
    public abstract V getOrElse(V defaultValue);

    /**
     * Returns the success value if present or throws an exception.
     *
     * @return the success value if this {@code Try} is a success.
     * @throws RuntimeException       if this {@code Try} is a failure. Throws the exception contained in the failure.
     * @throws NoSuchElementException if this {@code Try} is empty or a void success result
     */
    public abstract V getOrThrow();

    /**
     * @return the success value, if present. Null if the result is empty or a void success result. Throws the exception if the result is a failure.
     * @throws RuntimeException if this {@code Try} is a failure. Throws the exception contained in the failure.
     */
    public abstract V getOrNullOrThrow();

    /**
     * Throws an exception if this {@code Try} is a failure. Throws the exception contained in the failure {@code Try}.
     * Does nothing if this {@code Try} is a success or empty.
     *
     * <p>This method terminates the fluent call chain. There is no need to call this method in the middle of a fluent call chain
     * because the short-circuiting behaviour of failures always allow you to put this statement at the end of the chain</p>
     *
     * @throws RuntimeException if this {@code Try} is a failure. Throws the exception contained in the failure.
     */
    public abstract void onFailureThrow();

    /**
     * Turns a failure into a success.
     *
     * @param mapping maps the failure's exception to the success value
     * @return a success {@code Try} if this result is a failure. Empty if this result is empty. A failure {@code Try}
     * if the mapping throws an exception. The failure {@code Try} contains that exception.
     * This {@code Try} if it is already a success.
     */
    public abstract Try<V> recover(Function<RuntimeException, V> mapping);

    /**
     * Tells whether this {@code Try} is a failure.
     *
     * @return true if this {@code Try} is a failure, false if this {@code Try} is a success or empty.
     */
    public abstract boolean isFailure();

    /**
     * Tells whether this {@code Try} is empty.
     *
     * @return true if this {@code Try} is empty, false if this {@code Try} is a success or failure.
     */
    public abstract boolean isEmpty();


    // ----------------------------------------------------------------------------------------------------
    //  Common methods independent of the result type
    // ----------------------------------------------------------------------------------------------------


    /**
     * Invokes the given consumer on the success value if this {@code Try} is a success and checks the consumption for exceptions.
     * Does nothing if this {@code Try} is not a success.
     *
     * <p>Use this method if you want to apply side effects to the success value.</p>
     *
     * @param consumer the effect to apply to the success value
     * @return this {@code Try} if it is a success and the effect completes normally. A failure {@code Try} if the
     * effect throws an exception.
     */
    public final Try<V> onSuccess(Consumer<V> consumer) {
        return map(value -> {
            consumer.accept(value);
            return value;
        });
    }

    /**
     * Maps a failure to another failure if the failure's exception is of the specified type. Identity if the result
     * is not a failure or if the exception does not match the required type.
     *
     * @param tested  the exception type that is mapped if it matches
     * @param mapping maps a matching exception to a new exception
     * @param <X>     the exception type that is matched
     * @return a new failure with the mapped exception or this result if it is not a failure
     */
    public final <X extends RuntimeException> Try<V> mapFailure(Class<X> tested, Function<? super X, ? extends RuntimeException> mapping) {
        return mapFailure(bad -> {
            if (!tested.isAssignableFrom(bad.getClass())) {
                return bad;
            }

            return mapping.apply(tested.cast(bad));
        });
    }

    /**
     * Applies the given effect to the exception if this {@code Try} is a failure.
     * Does nothing if this {@code Try} is either a success or empty.
     *
     * @param failureEffect the effect to apply to the failure exception
     * @return this {@code Try} if the effect completes normally. A new failure {@code Try} if the effect throws an
     * exception. The new failure contains that exception.
     */
    public final Try<V> onFailure(Consumer<? super RuntimeException> failureEffect) {
        return mapFailure(bad -> {
            failureEffect.accept(bad);
            return bad;
        });
    }

    /**
     * Checks whether the success value of this {@code Try} satisfies the specified condition.
     * Does nothing if this {@code Try} is not a success.
     *
     * @param condition the condition to check on the success value
     * @return this {@code Try} if the condition matches or if this {@code Try} is not a success.
     * An empty {@code Try} if this {@code Try} is a success but the value does not match the condition.
     */
    public final Try<V> filter(Predicate<V> condition) {
        return flatMap(value -> condition.test(value) ? this : empty());
    }

    /**
     * Tells whether this {@code Try} represents a success. This
     * means it is neither a failure nor empty.
     *
     * @return true, if this {@code Try} is a success, false otherwise.
     */
    public final boolean isSuccess() {
        return !isFailure() && !isEmpty();
    }

    /**
     * Produces a human readable string representation for the {@code Try}.
     *
     * @return a human readable string representation
     */
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    // ----------------------------------------------------------------------------------------------------
    //  Result state representations
    // ----------------------------------------------------------------------------------------------------

    private static abstract class Success<V> extends Try<V> {

        Success() {
            super();
        }

        @Override
        public final <U> Try<U> map(Function<V, U> mapping) {
            return flatMap(value -> Try.of(() -> mapping.apply(value)));
        }

        @Override
        public final <U> Try<U> thenResultOf(Supplier<U> operation) {
            return Try.of(operation);
        }

        @Override
        public final Try<V> mapFailure(Function<? super RuntimeException, ? extends RuntimeException> mapping) {
            return this;
        }

        @Override
        public final Try<V> recover(Function<RuntimeException, V> mapping) {
            return this;
        }

        @Override
        public final Try<V> mapEmptyTo(V value) {
            return this;
        }

        @Override
        public final Try<V> mapEmptyToFailure(Supplier<RuntimeException> failureSignal) {
            return this;
        }

        @Override
        public final void onFailureThrow() {
            // Do nothing because we are a success.
        }

        @Override
        public boolean isFailure() {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }

    private final static class SuccessWithValue<V> extends Success<V> {

        private final V value;

        SuccessWithValue(V value) {
            super();
            this.value = requireNonNull(value, "Success value cannot be null. Create an Empty result or a VoidSuccess if null means empty or void.");
        }

        @Override
        public <U> Try<U> flatMap(Function<V, Try<U>> mapping) {
            return Try.ofFlat(() -> mapping.apply(value));
        }

        @Override
        public Try<V> onSuccess(Runnable sideEffect) {
            return Try.of(() -> {
                sideEffect.run();
                return value;
            });
        }

        @Override
        public V getOrElse(V defaultValue) {
            return value;
        }

        @Override
        public V getOrThrow() {
            return value;
        }

        @Override
        public V getOrNullOrThrow() {
            return getOrThrow();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            SuccessWithValue<?> success = (SuccessWithValue<?>) o;

            return new EqualsBuilder()
                    .append(value, success.value)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(value)
                    .toHashCode();
        }
    }

    private final static class VoidSuccess extends Success<Void> {

        VoidSuccess() {
            super();
        }

        @Override
        public <U> Try<U> flatMap(Function<Void, Try<U>> mapping) {
            throw new NoSuchElementException("This is a void success result that contains no value");
        }

        @Override
        public Try<Void> onSuccess(Runnable sideEffect) {
            return Try.ofVoid(sideEffect);
        }

        @Override
        public Void getOrElse(Void defaultValue) {
            return defaultValue;
        }

        @Override
        public Void getOrThrow() {
            throw new NoSuchElementException("This is a void success result that contains no value");
        }

        @Override
        public Void getOrNullOrThrow() {
            return null;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            return o != null && getClass() == o.getClass();
        }

        @Override
        public int hashCode() {
            return getClass().hashCode();
        }
    }

    private final static class Failure<V> extends Try<V> {

        private final RuntimeException exception;

        private Failure(RuntimeException exception) {
            super();
            this.exception = exception;
        }

        @Override
        public <U> Try<U> map(Function<V, U> mapping) {
            return failure(exception);
        }

        @Override
        public <U> Try<U> thenResultOf(Supplier<U> operation) {
            return failure(exception);
        }

        @Override
        public <U> Try<U> flatMap(Function<V, Try<U>> mapping) {
            return failure(exception);
        }

        @Override
        public Try<V> onSuccess(Runnable sideEffect) {
            return this;
        }

        @Override
        public V getOrElse(V defaultValue) {
            return defaultValue;
        }

        @Override
        public V getOrThrow() {
            throw exception;
        }

        @Override
        public V getOrNullOrThrow() {
            return getOrThrow();
        }

        @Override
        public Try<V> mapFailure(Function<? super RuntimeException, ? extends RuntimeException> mapping) {
            return failure(mapping.apply(exception));
        }

        @Override
        public Try<V> recover(Function<RuntimeException, V> mapping) {
            return Try.of(() -> mapping.apply(exception));
        }

        @Override
        public Try<V> mapEmptyTo(V value) {
            return this;
        }

        @Override
        public Try<V> mapEmptyToFailure(Supplier<RuntimeException> failureSignal) {
            return this;
        }

        @Override
        public void onFailureThrow() {
            throw exception;
        }

        @Override
        public boolean isFailure() {
            return true;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            Failure<?> failure = (Failure<?>) o;

            return new EqualsBuilder()
                    .append(exception, failure.exception)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(exception)
                    .toHashCode();
        }
    }

    private final static class Empty<V> extends Try<V> {

        @Override
        public <U> Try<U> map(Function<V, U> mapping) {
            return empty();
        }

        @Override
        public <U> Try<U> thenResultOf(Supplier<U> operation) {
            return empty();
        }

        @Override
        public <U> Try<U> flatMap(Function<V, Try<U>> mapping) {
            return empty();
        }

        @Override
        public Try<V> onSuccess(Runnable sideEffect) {
            return this;
        }

        @Override
        public V getOrElse(V defaultValue) {
            return defaultValue;
        }

        @Override
        public V getOrThrow() {
            throw new NoSuchElementException("Result is empty");
        }

        @Override
        public V getOrNullOrThrow() {
            return null;
        }


        @Override
        public Try<V> mapFailure(Function<? super RuntimeException, ? extends RuntimeException> mapping) {
            return this;
        }

        @Override
        public Try<V> recover(Function<RuntimeException, V> mapping) {
            return this;
        }

        @Override
        public Try<V> mapEmptyTo(V value) {
            return success(value);
        }

        @Override
        public Try<V> mapEmptyToFailure(Supplier<RuntimeException> failureSignal) {
            try {
                return failure(failureSignal.get());
            } catch (RuntimeException bad) {
                return failure(bad);
            }
        }

        @Override
        public void onFailureThrow() {
            // No-op because empty is not a failure.
        }

        @Override
        public boolean isFailure() {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            return o != null && getClass() == o.getClass();
        }

        @Override
        public int hashCode() {
            return getClass().hashCode();
        }

    }

}
