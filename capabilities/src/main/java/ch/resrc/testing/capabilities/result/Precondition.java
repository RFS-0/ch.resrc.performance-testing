package ch.resrc.testing.capabilities.result;

import java.util.*;
import java.util.function.*;

import static java.util.stream.Collectors.*;

/**
 * Models a precondition control flow in terms of a collection of precondition {@link Result}s.
 * <p>
 * Invokes a specified operation if a given collection of {@code Result}s are all {@link Success}es. Returns the result
 * of the operation as a {@code Result} object.
 * <p>
 * Produces a {@link Failure} if at least one of the precondition {@code Result}s is a {@code Failure}.
 * The {@code VoidFailure} contains the errors of all precondition {@code Result}s that are {@code Failure}s.
 * <p>
 * This object modularizes the typical control flow pattern, where some operation should only be executed if
 * a number of independent other operations have succeeded. In case of failure, all failures of the preconditions
 * should be reported.
 *
 * @param <EE> The (common) error type of the precondition results and the operation that is invoked in case all
 *             preconditions are satisfied.
 */
public class Precondition<EE> {

    private final List<Result<?, EE>> results;

    Precondition(List<? extends Result<?, EE>> results) {this.results = List.copyOf(results);}

    @SafeVarargs
    public static <EE> Precondition<EE> of(Result<?, EE>... results) {

        return Precondition.of(List.of(results));
    }

    public static <EE> Precondition<EE> of(List<? extends Result<?, EE>> results) {

        return new Precondition<>(results);
    }

    /**
     * Maps this combination to a new result. Returns a Success if all combined results are Successes.
     * Returns a Failure if any of the combined results is a Failure. The returned Failure contains the union of
     * all errors of all Failures in the combined results.
     * <p>
     * The given supplier produces the success value. It is only invoked if all combined results are Successes.
     * The supplier must not throw an exception if this combination is a Success.
     * </p>
     *
     * @param supplier supplies the result value if this combination is a Success
     * @param <U>      the type of the result value
     * @return a {@link Result} as explained.
     */
    public <U> Result<U, EE> thenValueOf(Supplier<U> supplier) {

        return this.thenResultOf(() -> Result.success(supplier.get()));
    }

    /**
     * Maps this combination to a VoidSuccess if all combined results are successes.
     * Returns a Failure if any of the combined results is a Failure. The returned Failure contains the union of
     * all errors of all Failures in the combined results.
     *
     * @return a {@link Result} as explained
     */
    public Result<Void, EE> thenVoidSuccess() {

        return this.thenResultOf(Result::voidSuccess);
    }

    /**
     * Maps this combination to a new result. Returns a Success if all combined results are Successes.
     * Returns a Failure if any of the combined results is a Failure. The returned Failure contains the union of
     * all errors of all Failures in the combined results.
     * <p>
     * The given supplier produces the returned new result if this combination is a Success. It is only invoked if this
     * combination is a Success. The supplier must not throw an exception if this combination is a Success.
     * However, the supplied result may be a Failure.
     * </p>
     * This function is useful, for example, for chaining validations, where a downstream validation can only
     * be carried out if a combination of upstream validations succeeds. For instance, the downstream validation
     * could be a cross validation of object attributes. The cross validation can only be done if the individual
     * attributes are syntactically valid.
     *
     * @param supplier supplies the new result if this combination is a Success
     * @param <U>      the type of the result value
     * @return a {@link Result} as explained.
     */
    public <U> Result<U, EE> thenResultOf(Supplier<Result<U, EE>> supplier) {

        List<EE> errors = results.stream()
                                 .filter(Result::isFailure)
                                 .flatMap(x -> x.errors().stream())
                                 .collect(toList());

        if (errors.isEmpty()) {
            try {
                return supplier.get();
            } catch (Exception bad) {
                throw new IllegalStateException(
                        "Failed to map to a new result although all preconditions were satisfied.", bad
                );
            }
        } else {
            return new Failure<>(errors);
        }
    }

}
