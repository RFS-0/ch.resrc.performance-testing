package ch.resrc.testing.use_cases.support.outbound_ports.authentication;

import ch.resrc.testing.domain.value_objects.ClientId;

public interface Client {

    ClientId id();

    static Client anonymous() {
        return Client.of(ClientId.of("dfa7a524-de08-4c81-87e6-ba1ed6216f14"));
    }

    static Client of(ClientId id) {
        return () -> id;
    }
}
