package ch.resrc.testing.capabilities.validation;

import ch.resrc.testing.capabilities.authentication.Client;
import ch.resrc.testing.capabilities.result.Result;

public interface Validatable<T> {

    Client client();

    Result<T, ValidationError> validated();

}