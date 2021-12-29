package ch.resrc.testing.capabilities.authentication;


public interface Client {

    ClientId id();

    static Client anonymous() {
        return Client.of(ClientId.of("dfa7a524-de08-4c81-87e6-ba1ed6216f14"));
    }

    static Client of(ClientId id) {
        return () -> id;
    }
}
