package ch.resrc.testing.capabilities.error_handling;

import java.util.Set;

/**
 * Identifies a problem kind that was detected by the system.
 */
public interface Problem {

    /**
     * @return a short title that communicates the type of the problem in a human friendly way
     */
    String title();

    /**
     * @return a template for a detailed problem message. The template parameters need to be
     * interpolated with suitable context values to produce a presentable problem message
     */
    String detailsTemplate();

    /**
     * Tests whether this problem is the same as a given problem.
     *
     * @param tested the problem to test against
     * @return true, if this problem is the same as the supplied problem. False otherwise.
     */
    default boolean is(Problem tested) {
        return isOneOf(tested);
    }

    /**
     * Tests whether this problem is one of the supplied problems
     *
     * @param tested the problems to test against
     * @return true, if this problem is one of the supplied problems. False otherwise.
     */
    default boolean isOneOf(Problem... tested) {
        return Set.of(tested).contains(this);
    }

}
