package ch.resrc.testing.adapters.rest.output;

/**
 * Marks objects that are able to present something on behalf of a use case.
 */
public interface HavingPresentation {

    /**
     * @return true if the object has nothing to present. False otherwise.
     */
    boolean isPresentationMissing();

}
