package ch.resrc.testing.test_capabilities.habits.use_cases;

import ch.resrc.testing.capabilities.error_handling.faults.Defect;
import ch.resrc.testing.test_capabilities.testbed.Ports;

public interface PortsHabits {

    default Ports ports() {
        throw Defect.of("This test habit needs " + Ports.class.getSimpleName() + " to run.");
    }
}
