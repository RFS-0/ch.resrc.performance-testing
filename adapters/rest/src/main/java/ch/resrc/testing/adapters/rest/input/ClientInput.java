package ch.resrc.testing.adapters.rest.input;

import ch.resrc.testing.capabilities.authentication.Client;

public interface ClientInput {

    default Client client() {
        return Client.anonymous();
    }

}
