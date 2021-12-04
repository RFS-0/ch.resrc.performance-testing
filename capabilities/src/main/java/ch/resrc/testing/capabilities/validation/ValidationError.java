package ch.resrc.testing.capabilities.validation;

import com.google.gson.Gson;
import org.apache.commons.lang3.builder.*;

import java.util.*;
import java.util.function.Consumer;

import static ch.resrc.testing.capabilities.functional.PersistentCollections.*;
import static java.lang.String.format;

/**
 * Represents an error due to an invalid value. Holds all information that is necessary to generate
 * an error message that humans find useful. Generates such an error message.
 *
 * @implNote effectively immutable
 */
public class ValidationError {

    public static final String UNKNOWN_ORIGIN = "<origin N/A>";

    private String origin;
    private String details;
    private Set<Claim> claims;
    private final Object invalidValue;

    /**
     * States a claim about the error that influences how the error gets presented.
     * The error processing pipeline can add one or more claims about the error depending
     * on the validation context.
     */
    public enum Claim {

        /**
         * The invalid value can safely be shown in error messages. It is known to be
         * a value that cannot compromise security if shown.
         * <p>
         * Often, this is the case for constraint validations that check if a set of values is consistent. The values
         * are normally known to be syntactically valid or otherwise a cross validation would not make sense.
         * In that case, the values can safely be shown if they fail the constraint.
         * </p>
         */
        SAFE_VALUE,

        /**
         * The invalid value is not worth showing because it does not
         * add information to the information already contained in the details message.
         */
        UNINTERESTING_VALUE
    }

    private ValidationError(String origin, String details, Object invalidValue, Set<Claim> claims) {

        this.origin = origin;
        this.details = details;
        this.invalidValue = invalidValue;
        this.claims = claims;
    }

    private ValidationError(ValidationError other) {

        this.origin = other.origin;
        this.details = other.details;
        this.invalidValue = other.invalidValue;
        this.claims = Set.copyOf(other.claims);
    }

    private ValidationError copied(Consumer<ValidationError> modification) {

        var theCopy = new ValidationError(this);
        modification.accept(theCopy);
        return theCopy;
    }

    /**
     * Creates a new validation error.
     *
     * @param details      details that explain why the value is invalid
     * @param invalidValue the invalid value
     * @return a new instance
     */
    public static ValidationError of(String details, Object invalidValue) {

        return new ValidationError(UNKNOWN_ORIGIN, details, invalidValue, Set.of());
    }

    /**
     * Yields an error message that can be shown clients.
     *
     * @return the error message
     */
    public String errorMessage() {

        return format("%s: %s%s", origin(), details(), invalidValuePart());
    }

    private String invalidValuePart() {

        if (isClaimed(Claim.UNINTERESTING_VALUE)) {
            return "";
        } else {
            return format(" - Invalid value: <%s>", displayableInvalidValue());
        }
    }

    private Object displayableInvalidValue() {

        return this.claims.contains(Claim.SAFE_VALUE) ? invalidValue() : "HIDDEN FOR SECURITY REASONS";
    }

    private boolean isClaimed(Claim claim) { return claims.contains(claim); }


    public String details() { return details; }

    public String origin() { return origin; }

    public boolean hasKnownOrigin() { return !this.origin.equals(UNKNOWN_ORIGIN); }

    private Object invalidValue() {

        if (isClaimed(Claim.SAFE_VALUE)) {
            return invalidValue;
        }

        throw new IllegalStateException(
                format("The invalid value is not safe to be used. It is possibly malicious." +
                       " To access it, you must first claim it to be safe with <%s.%s>",
                       Claim.class.getSimpleName(), Claim.SAFE_VALUE.name())
        );
    }


    /**
     * Creates a copy of this object but with a different origin
     * assigned to it.
     *
     * @param origin the origin that should be assigned to the copy
     * @return the copy.
     */
    public ValidationError butOrigin(String origin) {

        return this.copied(x -> x.origin = origin);
    }

    /**
     * Creates a copy of this object but with the given origin if this object
     * has an unknown origin. Returns this object without modification if this
     * object has a known origin.
     *
     * @param origin the origin to use if this object has an unknown origin
     * @return a validation error, as explained
     */
    public ValidationError butDefaultOrigin(String origin) {

        if (this.hasKnownOrigin()) return this;

        return this.copied(x -> x.origin = origin);
    }

    public ValidationError butDetails(String details) {

        return this.copied(x -> x.details = details);
    }

    public ValidationError butClaimed(Claim toBeAdded) {

        return this.copied(x -> x.claims = addedTo(claims, toBeAdded));
    }

    public ValidationError butClaimRevoked(Claim revoked) {

        return this.copied(x -> x.claims = removedFrom(claims, revoked));
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("origin", origin)
                .append("details", details)
                .append("claims", claims)
                .append("invalidValue", displayableInvalidValue())
                .toString();
    }

    private static class Memento {

        String origin;
        String details;
        List<Claim> claims;

        Memento(String origin, String details, Set<Claim> claims) {

            this.origin = origin;
            this.details = details;
            this.claims = List.copyOf(claims);
        }

        String asJsonStr() {

            return new Gson().toJson(this);
        }

        static Memento fromJsonStr(String json) {

            return new Gson().fromJson(json, Memento.class);
        }

    }

    public String asMementoWithoutValue() {

        return new Memento(origin, details, claims).asJsonStr();
    }

    public static ValidationError fromMemento(String mementoStr, Object invalidValue) {

        Memento memento = Memento.fromJsonStr(mementoStr);

        return new ValidationError(memento.origin,
                                   memento.details,
                                   invalidValue,
                                   Set.copyOf(memento.claims));
    }
}
