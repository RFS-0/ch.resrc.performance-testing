package ch.resrc.testing.products.spring_boot.app_config.use_cases;

import ch.resrc.testing.domain.*;
import ch.resrc.testing.use_cases.sort.SortUseCase;
import ch.resrc.testing.use_cases.sort.ports.inbound.Sort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.*;

@Configuration
public class SortPortConfig {

    @Bean
    @Qualifier("red-black-tree-sort")
    public Sort redBlackTreeSort() {
        return SortUseCase.create(new RedBlackTreeSorter());
    }

    @Bean
    @Qualifier("basic-bubble-sort")
    public Sort basicBubbleSort() {
        return SortUseCase.create(new BasicBubbleSorter());
    }

    @Bean
    @Qualifier("optimized-bubble-sort")
    public Sort optimizedBubbleSort() {
        return SortUseCase.create(new OptimizedBubbleSorter());
    }
}
