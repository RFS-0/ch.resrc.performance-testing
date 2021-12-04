package ch.resrc.testing.test_capabilities.testbed;

import java.util.function.Consumer;

/**
 * Modifies the configuration of the test bed that gest passed to the accept method.
 * <p>
 * Using functions of this type, we can change the test bed configuration
 * in tests in a declarative way.
 * </p>
 */
public interface TestBedModifier extends Consumer<TestBed> {
}
