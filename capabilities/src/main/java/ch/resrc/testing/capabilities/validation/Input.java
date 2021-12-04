package ch.resrc.testing.capabilities.validation;

import ch.resrc.testing.capabilities.error_handling.Try;
import org.apache.commons.lang3.builder.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static java.util.stream.Collectors.*;

/**
 * Represents an input value. Associates origin information to the input value.
 * The origin information is used in error messages if the input fails to parse
 * or validate.
 *
 * <p>A value of null is interpreted as an absent input value. An absent value is not parsed
 * and not mapped. A parsing or mapping function is not invoked for absent values</p>
 *
 * @param <T> the type of the input value
 * @implNote the behaviour of defined and absent input values is implemented by a
 * GoF state machine pattern.
 */
public abstract class Input<T> {

    private final T value;
    private final String origin;


    private Input(T value, String origin) {

        this.value = value;
        this.origin = origin;
    }

    /**
     * Creates a new instance for the given value and origin
     *
     * @param value  the input value
     * @param origin the origin of the input value. Choose it so that humans easily understand where the input value comes from.
     * @param <T>    the type of the input value
     * @return a new instance
     */
    public static <T> Input<T> of(T value, String origin) {

        if (value != null) {
            return defined(value, origin);
        } else {
            return absent(origin);
        }
    }

    /**
     * Creates a new instance for a value of unknown origin. The origin information on the input object
     * signals that the origin is unknown.
     *
     * @param valueOfUnknownOrigin the input value
     * @param <T>                  the type of the input value
     * @return a new instance, as explained
     */
    public static <T> Input<T> of(T valueOfUnknownOrigin) {

        return Input.of(valueOfUnknownOrigin, "origin N/A");
    }

    /**
     * Turns an array of raw input into a list of {@code Input} objects using the supplied
     * mapping function. If the array is null, an empty list is returned.
     *
     * @param input        the array of raw input
     * @param inputMapping maps each element of the array to a corresponding input object
     * @return a list of input objects for the array of raw input
     */
    public static <T> List<Input<T>> of(T[] input, Function<T, Input<T>> inputMapping) {

        return Optional.ofNullable(input)
                       .map(it -> Stream.of(it).map(inputMapping).collect(toList()))
                       .orElse(List.of());
    }

    /**
     * Concatenates the origins of the given inputs in the order that they appear in the list.
     *
     * @param origins the inputs whose origins should be concatenated
     * @return the concatenated origins.
     */
    public static String originsOf(Input<?>... origins) {

        return Stream.of(origins).map(Input::origin).collect(Collectors.joining("; "));
    }

    private static <T> Defined<T> defined(T value, String origin) {

        return new Defined<>(value, origin);
    }

    private static <T> Absent<T> absent(String origin) {

        return new Absent<>(origin);
    }

    /**
     * Applies the given function to this object unless the input is absent. Returns null, if the input is absent.
     *
     * @param f   the function to apply if the input is present
     * @param <U> the type of the value that the function maps to
     * @return the function's result or null, as explained
     */
    public abstract <U> U applyOrNull(Function<Input<T>, U> f);

    /**
     * Tries to parse the value of this object with the given parser.
     *
     * Returns a success whose value is an input of the parsed value if the value parses successfully.
     * The returned input has the same origin as this object.
     *
     * Returns failure with an {@code InvalidInputDetected} exception if the value fails to parse.
     *
     * Returns an empty input if this object is an empty input.
     *
     * @param parser the parser to be used
     * @param <U> the target type. The parser creates an object of this type
     * @return the parsing result as explained
     */
    public abstract <U> Try<Input<U>> tryParse(InputParser<T,U> parser);

    /**
     * Maps the input value to another value. The result is a new {@code Input} with the same
     * origin as this {@code Input} but with the mapped value as the input value.
     *
     * @param inputMapping maps this input's value to another value
     * @param <U>          the type of the mapped value
     * @return a new {@code Input} as explained
     */
    public abstract <U> Input<U> map(Function<T, U> inputMapping);

    /**
     * Maps the input value to another {@code Input}. The origin of the result is a concatenation of the origin
     * of this input with the origin of the input that the mapping function returns.
     *
     * @param inputMapping maps this input's value to another input.
     * @param <U>          the type of the mapped value
     * @return a new {@code Input} as explained
     */
    public abstract <U> Input<U> flatMap(Function<T, Input<U>> inputMapping);

    /**
     * Tells whether an input value is present in this object or not.
     *
     * @return true, if an input value is present. False otherwise.
     */
    public abstract boolean isPresent();

    private Input<T> withOrigin(String newOrigin) {

        return Input.of(this.value(), newOrigin);
    }

    /**
     * Creates a failed {@code Try} with an {@link InvalidInputDetected} exception if the input is absent.
     * Returns this input wrapped in a {@code Try} if the input is defined.
     *
     * @param errorMessage the message that an absent input should use for the {@code InvalidInputDetected} exception.
     * @return the failure result if the input is absent, a success result if the input is defined
     */
    public abstract Try<Input<T>> rejectAbsent(String errorMessage);


    /**
     * Declares the input value as invalid.
     *
     * @param details explains to humans why the input value is invalid
     * @return an {@link ValidationError} for the input value with the given details
     */
    public ValidationError invalidate(String details) {

        return ValidationError.of(details, value()).butOrigin(origin());
    }


    /**
     * @return the input value represented by this object
     */
    public T value() {

        return value;
    }

    /**
     * @return the origin of the input value
     */
    public String origin() {

        return origin;
    }


    @Override
    public String toString() {

        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("value", value)
                .append("origin", origin)
                .toString();
    }

    /**
     * Represents a defined, i.e. non-null, input value.
     */
    private static class Defined<T> extends Input<T> {

        private Defined(T value, String origin) {

            super(value, origin);
        }

        @Override
        public <U> Try<Input<U>> tryParse(InputParser<T, U> parser) {

            return Try.of(() -> parser.parse(this.value())
                                      .map(x -> Input.<U>of(x, this.origin()))
                                      .mapErrors(x -> x.butOrigin(this.origin()))
                                      .getOrThrow(InvalidInputDetected::of)
            );
        }

        @Override
        public <U> Input<U> map(Function<T, U> inputMapping) {

            return Input.of(inputMapping.apply(this.value()), this.origin());
        }

        @Override
        public <U> Input<U> flatMap(Function<T, Input<U>> inputMapping) {

            Input<U> theMappedInput = inputMapping.apply(this.value());
            return theMappedInput.withOrigin(originsOf(this, theMappedInput));
        }

        @Override
        public boolean isPresent() {

            return true;
        }

        @Override
        public <U> U applyOrNull(Function<Input<T>, U> f) {

            return f.apply(this);
        }

        @Override
        public Try<Input<T>> rejectAbsent(String errorMessage) {

            return Try.of(this);
        }
    }

    /**
     * Represents an absent input value. Null is interpreted as an absent input value.
     */
    private static class Absent<T> extends Input<T> {

        private Absent(String origin) {

            super(null, origin);
        }


        @Override
        public <U> Try<Input<U>> tryParse(InputParser<T, U> parser) {

            return Try.of(Input.absent(this.origin()));
        }

        @Override
        public <U> Input<U> map(Function<T, U> inputMapping) {

            return Input.absent(this.origin());
        }

        @Override
        public <U> Input<U> flatMap(Function<T, Input<U>> inputMapping) {

            return Input.absent(this.origin());
        }

        @Override
        public boolean isPresent() {

            return false;
        }

        @Override
        public <U> U applyOrNull(Function<Input<T>, U> f) {

            return null;
        }

        @Override
        public Try<Input<T>> rejectAbsent(String errorMessage) {

            return Try.failure(InvalidInputDetected.of(this.invalidate(errorMessage)));
        }

    }
}
