package ch.resrc.testing.test_capabilities.testbed;

import ch.resrc.testing.test_capabilities.habits.use_cases.SortHabits;

/**
 * Provides all the functionality defined by the listed test habit interfaces.
 * Implementations just need to bind the hook methods that the test habits rely on (non-default interface methods).
 */
public interface TestBedContextHabits

        extends

        SortHabits {
}
