package ch.resrc.testing.test_capabilities.habits.use_cases;

import ch.resrc.testing.use_cases.sort.SortUseCase;
import ch.resrc.testing.use_cases.sort.ports.inbound.Sort;

public interface SortHabits

        extends

        PortsHabits {

    default Sort newSortUseCase() {
        return SortUseCase.create();
    }
}
