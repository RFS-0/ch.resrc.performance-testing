package ch.resrc.testing.capabilities.validation;

import ch.resrc.testing.capabilities.functional.Reduce;
import ch.resrc.testing.capabilities.result.*;
import io.vavr.collection.*;
import org.apache.commons.lang3.StringUtils;

import java.net.*;
import java.util.*;
import java.util.List;
import java.util.function.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static ch.resrc.testing.capabilities.validation.ValidationError.Claim.UNINTERESTING_VALUE;
import static ch.resrc.testing.capabilities.validation.ValidationErrorModifier.*;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class Validations {

    public static <T, E> Result<T, E> validated(T toBeValidated, Validation<T, E> validation) {

        return validation.applyTo(toBeValidated);
    }

    public static <T> Result<T, ValidationError> validatedInput(Input<T> toBeValidated, Validation<T, ValidationError> validation) {

        return validation.applyTo(toBeValidated.value()).mapErrors(origin(toBeValidated.origin()));
    }

    public static <T, E> List<T> toValues(Collection<Result<T, E>> validated) {

        return validated.stream().map(Result::value).collect(toList());
    }

    /**
     * Produces a {@link Validation} that applies the given sequence of validations to the validated object.
     * The object is valid if and only if all validations are successful. Applies the validations in the order in which
     * they are supplied to this function. If a validation fails in the sequence, all further validations are skipped.
     * <p>
     * The short circuiting behaviour is useful if an upstream validation is supposed to protect the validation
     * operation
     * of a downstream validation. Or, if a downstream validation can only be done if the upstream validation succeeds.
     * For example, an upstream validation might validate the size of a string so that the parser of a
     * downstream validation is protected from maliciously large input that intends a denial of service attack.
     *
     * @param validations the validations to apply
     * @param <T>         the type of the object to be validated.
     * @param <E>         the error type of the validations
     *
     * @return the {@code Validation}, as explained
     */
    @SafeVarargs
    public static <T, E> Validation<T, E> chained(Validation<T, E>... validations) {

        return (T toBeValidated) ->
                Reduce.reduce(Stream.of(validations), Result.success(toBeValidated), Result::flatMap);
    }

    /**
     * Modifies the given validation such that errors are mapped with the specified error mappings.
     * The mappings are applied in the order they supplied to this function.
     *
     * @param validation     the validation to modify
     * @param errorModifiers the error mappings to be applied after validation
     * @param <T>            the type of the validated object
     * @param <E>            the error type
     *
     * @return the modified validation, as explained.
     */
    @SafeVarargs
    public static <T, E> Validation<T, E> modified(Validation<T, E> validation,
                                                   Function<E, E>... errorModifiers) {

        return (T toBeValidated) -> {
            Result<T, E> validated = validation.applyTo(toBeValidated);
            return Reduce.reduce(Stream.of(errorModifiers), validated, Result::mapErrors);
        };
    }

    /**
     * Produces a {@link Validation} that applies all of the given validations to the validated object.
     * The validation is successful if and only if the object passes all of the validations.
     * All validations are applied, even if some of them fail. Contrast this with {@link #chained} which stops
     * validating
     * as soon as one of the validations fails.
     *
     * @param validations the validations to apply
     * @param <T>         the type of the object to be validated
     * @param <E>         the error type of the validations
     *
     * @return the {@code Validation}, as explained
     */
    @SafeVarargs
    public static <T, E> Validation<T, E> allOf(Validation<T, E>... validations) {

        return (T toBeValidated) -> {

            List<Result<?, E>> allValidated = Stream.of(validations)
                    .map(validation -> validation.applyTo(toBeValidated))
                    .collect(toList());

            return Precondition.of(allValidated).thenValueOf(() -> toBeValidated);
        };
    }

    /**
     * Produces a {@link Validation} that validates each element of a collection. The validation is successful
     * if and only if each element of the collection passes the given validation.
     *
     * @param elementValidation is applied to each element of the collection
     * @param errorModifiers    modifiers to be applied to all validation errors raised by this validation
     * @param <T>               the type of the collection elements
     * @param <E>               the error type of the validation
     *
     * @return the {@code Validation}, as explained
     */
    @SafeVarargs
    public static <T, E> Validation<Collection<T>, E> forEach(Validation<T, E> elementValidation,
                                                              UnaryOperator<E>... errorModifiers) {

        return modified(new ForEach<>(elementValidation), errorModifiers);
    }

    /**
     * Produces a {@link Validation} that validates each element of a collection. The validation is successful
     * if and only if each element of the collection passes the given validation. The element validation
     * is determined by the supplied function. This way, the element validation can depend on the element
     * being validated
     *
     * @param getValidationForElement function that supplies the validation to be applied to an element of the
     *                                collection
     * @param errorModifiers          modifiers to be applied to all validation errors raised by this validation
     * @param <T>                     the type of the collection elements
     * @param <E>                     the error type of the validation
     *
     * @return the {@code Validation}, as explained
     */
    @SafeVarargs
    public static <T, E> Validation<Collection<T>, E> forEach(Function<T, Validation<T, E>> getValidationForElement,
                                                              UnaryOperator<E>... errorModifiers) {

        return modified(new ForEach<>(getValidationForElement), errorModifiers);
    }

    /**
     * Produces a {@link Validation} that validates an attribute of the validated object.
     * The object is valid if and only if the attribute is valid according to the supplied attribute validator.
     *
     * @param attributeLense      yields the attribute value to be validated. Receives the validated object as its
     *                            argument.
     * @param attributeValidation validates the attribute value
     * @param <T>                 the type of the object whose attribute gets validated
     * @param <U>                 the type of the validated attribute
     * @param <E>                 the error type of the attribute validation
     *
     * @return the {@code Validation}, as explained
     */
    public static <T, U, E> Validation<T, E> attribute(Function<T, U> attributeLense, Validation<U, E> attributeValidation) {

        return (T toBeValidated) ->
                attributeValidation.applyTo(attributeLense.apply(toBeValidated))
                        .map(__ -> toBeValidated);
    }

    /**
     * Produces a {@link Validation} that succeeds if and only if the validated object satisfies the given
     * condition.
     *
     * @param condition      the validation condition
     * @param errorModifiers all errors are modified with these
     * @param <T>            the type of the validated object
     *
     * @return the {@code Validation}, as explained
     */
    @SafeVarargs
    public static <T> Validation<T, ValidationError> isTrue(Predicate<T> condition,
                                                            UnaryOperator<ValidationError>... errorModifiers) {

        return (T toBeValidated) -> {

            if (condition.test(toBeValidated)) {
                return Result.success(toBeValidated);
            } else {
                ValidationError error = ValidationError.of("must be true", toBeValidated);
                return Result.<T, ValidationError>failure(error)
                        .mapErrors(ValidationErrorModifier.chained(errorModifiers));
            }
        };
    }

    @SafeVarargs
    public static <T> Validation<T, ValidationError> isFalse(Predicate<T> condition,
                                                             UnaryOperator<ValidationError>... errorModifiers) {

        return (T toBeValidated) -> {

            if (!condition.test(toBeValidated)) {
                return Result.success(toBeValidated);
            } else {
                ValidationError error = ValidationError.of("must be false", toBeValidated);
                return Result.<T, ValidationError>failure(error)
                        .mapErrors(ValidationErrorModifier.chained(errorModifiers));
            }
        };
    }

    @SafeVarargs
    public static <T> Validation<T, ValidationError> notNull(UnaryOperator<ValidationError>... errorModifiers) {

        Validation<T, ValidationError> validation = isTrue(
                Objects::nonNull,
                msg("must not be null"),
                claimed(UNINTERESTING_VALUE)
        );

        return modified(validation, errorModifiers);
    }

    @SafeVarargs
    public static <T> Validation<Traversable<T>, ValidationError> noneNull(UnaryOperator<ValidationError>... errorModifiers) {

        Validation<Traversable<T>, ValidationError> validation = isFalse(
                (Traversable<T> values) -> values.exists(Objects::isNull),
                msg("must not contain null"),
                claimed(UNINTERESTING_VALUE)
        );

        return modified(validation, errorModifiers);
    }

    @SafeVarargs
    public static Validation<String, ValidationError> matches(String regexp,
                                                              UnaryOperator<ValidationError>... errorModifiers) {

        String errorMessage = format("must match <%s>", regexp);
        Pattern pattern = Pattern.compile(regexp);

        return modified(isTrue(x -> pattern.matcher(x).matches(), msg(errorMessage)), errorModifiers);
    }

    @SafeVarargs
    public static Validation<String, ValidationError> matches(String regexp,
                                                              int flags,
                                                              UnaryOperator<ValidationError>... errorModifiers) {

        String errorMessage = format("must match <%s>", regexp);
        Pattern pattern = Pattern.compile(regexp, flags);

        return modified(isTrue(x -> pattern.matcher(x).matches(), msg(errorMessage)), errorModifiers);
    }

    @SafeVarargs
    public static Validation<String, ValidationError> maxSize(int maxSize,
                                                              UnaryOperator<ValidationError>... errorModifiers) {

        var validation = isTrue(
                (String x) -> x.length() <= maxSize,
                msg(format("is limited to <%s> characters", maxSize))
        );

        return modified(validation, errorModifiers);
    }

    @SafeVarargs
    public static Validation<Number, ValidationError> equalTo(Number number, UnaryOperator<ValidationError>... errorModifiers) {
        var validation = isTrue(
                (Number x) -> x.doubleValue() == number.doubleValue(),
                msg(format("must be equal to <%s>", number))
        );

        return modified(validation, errorModifiers);
    }

    @SafeVarargs
    public static Validation<Seq<Integer>, ValidationError> allDivisibleBy(int divisor, UnaryOperator<ValidationError>... errorModifiers) {
        var validation = isFalse(
                (Seq<Integer> values) -> values.exists(value -> value % divisor != 0),
                msg(format("all values must be divisible by <%s>", divisor))
        );

        return modified(validation, errorModifiers);
    }

    @SafeVarargs
    public static Validation<Seq<Integer>, ValidationError> allMin(int minimum, UnaryOperator<ValidationError>... errorModifiers) {
        var validation = isFalse(
                (Seq<Integer> values) -> values.exists(value -> value < minimum),
                msg(format("all values must be greater or equal to <%s>", minimum))
        );

        return modified(validation, errorModifiers);
    }

    @SafeVarargs
    public static Validation<Seq<Integer>, ValidationError> allMax(int maximum, UnaryOperator<ValidationError>... errorModifiers) {
        var validation = isFalse(
                (Seq<Integer> values) -> values.exists(value -> value > maximum),
                msg(format("all values must be smaller or equal to <%s>", maximum))
        );

        return modified(validation, errorModifiers);
    }

    @SafeVarargs
    public static Validation<Integer, ValidationError> min(int minimum,
                                                           UnaryOperator<ValidationError>... errorModifiers) {

        Validation<Integer, ValidationError> validation = isTrue(
                x -> x >= minimum,
                msg("must greater or equal to <%s>", minimum)
        );

        return modified(validation, errorModifiers);
    }

    @SafeVarargs
    public static Validation<String, ValidationError> notBlank(UnaryOperator<ValidationError>... errorModifiers) {

        Validation<String, ValidationError> validation = isTrue(
                StringUtils::isNotBlank,
                msg("must not be blank"),
                claimed(UNINTERESTING_VALUE)
        );

        return modified(validation, errorModifiers);
    }

    @SafeVarargs
    public static Validation<String, ValidationError> isUuid(UnaryOperator<ValidationError>... errorModifiers) {

        Validation<String, ValidationError> validation =
                (String value) -> {
                    try {
                        var __ = UUID.fromString(value);
                        return Result.success(value);
                    } catch (IllegalArgumentException bad) {
                        return Result.failure(ValidationError.of("must be a UUID", value));
                    }
                };

        return modified(validation, errorModifiers);
    }

    @SafeVarargs
    public static Validation<String, ValidationError> isInteger(UnaryOperator<ValidationError>... errorModifiers) {

        Validation<String, ValidationError> validation =
                (String value) -> {
                    try {
                        var __ = Integer.parseInt(value);
                        return Result.success(value);
                    } catch (NumberFormatException bad) {
                        return Result.failure(ValidationError.of("must be an integer", value));
                    }
                };

        return modified(validation, errorModifiers);
    }

    @SafeVarargs
    public static Validation<String, ValidationError> isUrl(UnaryOperator<ValidationError>... errorModifiers) {
        Validation<String, ValidationError> validation = (String value) -> {
            try {
                var __ = new URL(value);
                __.toURI();
                return Result.success(value);
            } catch (URISyntaxException | MalformedURLException bad) {
                return Result.failure(ValidationError.of("must be an URL", value));
            }
        };

        return modified(validation, errorModifiers);
    }

    @SafeVarargs
    public static <T> Validation<Seq<T>, ValidationError> distinct(UnaryOperator<ValidationError>... errorModifiers) {
        Validation<Seq<T>, ValidationError> validation = isTrue(
                (Seq<T> values) -> values.length() == values.distinct().length(),
                msg("all values must be distinct"),
                claimed(UNINTERESTING_VALUE)
        );

        return modified(validation, errorModifiers);
    }
}
