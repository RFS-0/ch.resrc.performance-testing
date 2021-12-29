package ch.resrc.testing.capabilities.authentication;

import ch.resrc.testing.capabilities.result.Result;
import ch.resrc.testing.capabilities.validation.*;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.UUID;

import static ch.resrc.testing.capabilities.validation.ValidationErrorModifier.context;
import static ch.resrc.testing.capabilities.validation.Validations.*;

public class ClientId {

    private final UUID value;

    private ClientId(String literal) {
        value = UUID.fromString(literal);
    }

    public static Validation<String, ValidationError> validation() {

        return Validations.chained(
                notNull(),
                isUuid()
        ).mapErrors(context(ClientId.class));
    }

    public static Result<ClientId, ValidationError> resultOf(String literal) {
        return validation().applyTo(literal).map(ClientId::new);
    }

    public static ClientId of(String literal) throws InvalidInputDetected {
        return ClientId.resultOf(literal).getOrThrow(InvalidInputDetected::of);
    }

    protected String getPrimitiveValue() {
        return value.toString();
    }

    protected UUID getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientId clientId = (ClientId) o;

        return value.equals(clientId.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
