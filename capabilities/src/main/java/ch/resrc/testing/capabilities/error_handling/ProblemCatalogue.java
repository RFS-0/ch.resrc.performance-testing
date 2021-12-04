package ch.resrc.testing.capabilities.error_handling;

/**
 * Yields a {@link ProblemCode} for a given {@link Problem}. The problem code should be reported to clients if
 * the problem occurs. A concrete implementation defines the codes for a particular application.
 * <p>
 * Problem codes must be unique and should not be reused for different problems once they have been published to clients.
 * We therefore don't define the codes in the {@code Problem} objects. Because that would make it very difficult
 * to maintain the codes and to test their uniqueness. Instead, all codes are defined in a single
 * {@code ProblemCatalogue}. The problem catalogue provides a quick overview of all codes that exist and how they
 * are mapped to problems.
 * </p>
 */
public interface ProblemCatalogue {

    /**
     * Yields the problem code for the given problem.
     *
     * @param problem the problem whose code is requested
     * @return the problem code for the problem
     */
    ProblemCode codeFor(Problem problem);


    /**
     * Use instances of this object as placeholders in a problem catalogue map for {@code Problem}s that were
     * removed from the system. This is normally necessary, because problem codes that have already been published
     * should not be reused for new kinds or problems. Using this object, you can map retired codes to a
     * "problem of the past".
     */
    class ProblemOfThePast implements Problem {

        private final String pastProblem;

        private ProblemOfThePast(String pastProblem)
        {
            this.pastProblem = pastProblem;
        }

        public static ProblemOfThePast usedToBe(String problem)
        {
            return new ProblemOfThePast(problem);
        }

        @Override
        public String title()
        {
            throw unsupportedOperation();
        }

        @Override
        public String detailsTemplate()
        {
            throw unsupportedOperation();
        }

        private RuntimeException unsupportedOperation() {
            return new UnsupportedOperationException(String.format("You cannot use this object as a real problem representation. "
                    + "This object serves as a placeholder in a %s for a problem of the past that used to go by the name %s.",
                    ProblemCatalogue.class.getSimpleName(),
                    pastProblem));
        }
    }
}
