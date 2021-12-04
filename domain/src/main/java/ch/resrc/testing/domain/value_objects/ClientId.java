package ch.resrc.testing.domain.value_objects;

import ch.resrc.testing.capabilities.result.Result;
import ch.resrc.testing.capabilities.validation.*;
import ch.resrc.testing.domain.IdSequence;
import ch.resrc.testing.domain.error_handling.DomainProblemDetected;

import java.util.*;

import static ch.resrc.testing.capabilities.validation.ValidationErrorModifier.context;
import static ch.resrc.testing.capabilities.validation.Validations.*;
import static ch.resrc.testing.domain.validation.DomainValidations.invariantViolated;

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

public static ClientId of(String literal) throws DomainProblemDetected {
        return ClientId.resultOf(literal).getOrThrow(invariantViolated());
    }

    protected UUID getValue() {
        return value;
    }

    public interface Sequence extends IdSequence<ClientId> {

        Sequence RANDOM = () -> ClientId.of(UUID.randomUUID().toString());

        ClientId nextId();
    }
}
