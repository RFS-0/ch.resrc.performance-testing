package ch.resrc.testing.capabilities.events;

public class ErrorOccurred extends Event {

    private final RuntimeException error;

    public ErrorOccurred(RuntimeException error) {
        this.error = error;
    }

    public RuntimeException asException() {
        return error;
    }
}
