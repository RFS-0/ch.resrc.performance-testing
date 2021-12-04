package ch.resrc.testing.domain.habits;

import ch.resrc.testing.domain.RedBlackTreeSort;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class RedBlackTreeSortTest {

    @Test
    void unsortedList_apply_sortedList() {
        // given:
        List<Integer> unsorted = List.of(5, 4, 3, 2, 1);
        RedBlackTreeSort<Integer> sort = new RedBlackTreeSort<>();

        // when:
        List<Integer> sorted = sort.apply(unsorted);

        // then:
        assertThat(sorted).isEqualTo(List.of(1, 2, 3, 4, 5));
    }

    @Test
    void sortedList_apply_sortedList() {
        // given:
        List<Integer> alreadySorted = List.of(1, 2, 3, 4, 5);
        RedBlackTreeSort<Integer> sort = new RedBlackTreeSort<>();

        // when:
        List<Integer> sorted = sort.apply(alreadySorted);

        // then:
        assertThat(sorted).isEqualTo(List.of(1, 2, 3, 4, 5));
    }

}