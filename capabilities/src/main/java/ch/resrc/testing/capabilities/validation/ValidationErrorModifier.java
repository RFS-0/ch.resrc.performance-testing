package ch.resrc.testing.capabilities.validation;

import com.google.common.base.CaseFormat;

import java.util.List;
import java.util.function.UnaryOperator;

import static ch.resrc.testing.capabilities.functional.Reduce.reduce;
import static java.lang.String.format;

/**
 * A collection of useful operators that map {@link ValidationError}s.
 * <p>
 * The operators reflect typical error handling patterns. Use them to assemble consistent, a-la-carte error messages.
 */
public class ValidationErrorModifier {

    /**
     * Returns an operator that adds the given claim to the {@link ValidationError}.
     *
     * @param claim the claim to add
     *
     * @return the operator, as explained.
     */
    public static UnaryOperator<ValidationError> claimed(ValidationError.Claim claim) {

        return (ValidationError x) -> x.butClaimed(claim);
    }

    /**
     * Returns an operator that removes the given claim from the {@link ValidationError}.
     *
     * @param revokedClaim the claim to be removed
     *
     * @return the operator, as explained.
     */
    public static UnaryOperator<ValidationError> claimRevoked(ValidationError.Claim revokedClaim) {

        return (ValidationError x) -> x.butClaimRevoked(revokedClaim);
    }

    /**
     * Returns an operator that prepends the given text to the error details of a validation error.
     * The text serves as context for the remaining error details
     *
     * @param text the context to prepend to the error details
     *
     * @return the operator, as explained
     */
    public static UnaryOperator<ValidationError> context(String text) {

        return (ValidationError error) -> {
            var modifiedMessage = format("%s - %s", text, error.details());
            return error.butDetails(modifiedMessage);
        };
    }

    /**
     * Returns an operator that prepends the name of the given class to the error details of a validation error.
     * The class name serves as context for the remaining error details. The class name is converted to lower case
     * with camel case replaced by spaces.
     * <p>
     * Normally, this operator is used for domain classes whose names have direct meaning to clients.
     * Don't use this operators for classes whose names are not understood by the target audience of
     * the error messages.
     * </p>
     *
     * @param contextClass the class whose formatted name to prepend to the error details
     *
     * @return the operator, as explained
     */
    public static UnaryOperator<ValidationError> context(Class<?> contextClass) {

        String context = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, contextClass.getSimpleName())
                .replace("_", " ");

        return ValidationErrorModifier.context(context);
    }

    /**
     * Returns an operator that replaced the error details with the text 'must be specified'.
     * This is a recurring error message stereotype. It serves to ensure consistent use of this error message.
     *
     * @return the operator, as explained.
     */
    public static UnaryOperator<ValidationError> mustBeSpecifiedMsg() {

        return msg("must be specified");
    }

    /**
     * Returns an operator that replaced the error details with the text 'cannot be undefined'.
     * This is a recurring error message stereotype. It serves to ensure consistent use of this error message.
     *
     * @return the operator, as explained.
     */
    public static UnaryOperator<ValidationError> cannotBeUndefinedMsg() {

        return msg("cannot be undefined");
    }

    /**
     * Returns an operator that replaces the error details with the specified text.
     *
     * @param text the error details that should be used.
     *
     * @return the operator, as explained.
     */
    public static UnaryOperator<ValidationError> msg(String text, Object... params) {
        if (params == null || params.length == 0) {
            return (ValidationError error) -> error.butDetails(text);
        }
        return (ValidationError error) -> error.butDetails(format(text, params));
    }

    /**
     * Returns an operator that replaces the value origin with the specified text.
     *
     * @param text the value origin that should be used.
     *
     * @return the operator, as explained.
     */
    public static UnaryOperator<ValidationError> origin(String text) {

        return (ValidationError error) -> error.butOrigin(text);
    }

    /**
     * Returns an operator that replaces the value origin with the specified text
     * if the origin was unknown, so far. Does nothing if the origin is
     * already known.
     *
     * @param text the value origin that should be used.
     *
     * @return the operator, as explained.
     */
    public static UnaryOperator<ValidationError> defaultOrigin(String text) {

        return (ValidationError error) -> error.butDefaultOrigin(text);
    }

    /**
     * Returns an operator that prepends the specified index to the error details if the
     * error type is {@link ValidationError}. Has no effect for other error types.
     * <p>
     * Use this operator to supply index information if the modified validation validates
     * elements of a collection.
     * </p>
     *
     * @param idx the index to prepend
     * @param <E> the error type
     *
     * @return the operator, as explained
     */
    @SuppressWarnings("unchecked")
    public static <E> UnaryOperator<E> withIndex(Integer idx) {

        return (E error) -> {

            if (!(error instanceof ValidationError)) return error;

            String indexLabel = format("[%s]", idx);

            return (E) context(indexLabel).apply((ValidationError) error);
        };
    }

    /**
     * Returns an operator that applies all the specified operators to the error.
     * The operators are applied in the order in which they are supplied to this function.
     *
     * @param operators the operators to apply
     * @param <E>       the error type
     *
     * @return the operator, as explained
     */
    @SafeVarargs
    public static <E> UnaryOperator<E> chained(UnaryOperator<E>... operators) {

        return (E error) -> reduce(List.of(operators), error, (err, op) -> op.apply(err));
    }
}
