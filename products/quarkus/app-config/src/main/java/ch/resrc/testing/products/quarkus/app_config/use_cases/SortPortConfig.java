package ch.resrc.testing.products.quarkus.app_config.use_cases;

import ch.resrc.testing.adapters.rest.quarkus.config.*;
import ch.resrc.testing.domain.*;
import ch.resrc.testing.use_cases.sort.SortUseCase;
import ch.resrc.testing.use_cases.sort.ports.inbound.Sort;

import javax.enterprise.context.ApplicationScoped;

public class SortPortConfig {

    @ApplicationScoped
    @RedBlackTreeSort
    public Sort redBlackTreeSort() {
        return SortUseCase.create(new RedBlackTreeSorter());
    }

    @ApplicationScoped
    @BasicBubbleSort
    public Sort basicBubbleSort() {
        return SortUseCase.create(new BasicBubbleSorter());
    }

    @ApplicationScoped
    @OptimizedBubbleSort
    public Sort optimizedBubbleSort() {
        return SortUseCase.create(new OptimizedBubbleSorter());
    }
}
