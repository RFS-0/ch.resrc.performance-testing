package ch.resrc.testing.capabilities.error_handling;

/**
 * Implementations have a representation as an exception.
 */
public interface HavingExceptionRepresentation {

    RuntimeException asException();
}
