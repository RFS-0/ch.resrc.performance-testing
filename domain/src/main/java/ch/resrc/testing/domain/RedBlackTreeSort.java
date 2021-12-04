package ch.resrc.testing.domain;

import ch.resrc.testing.domain.value_objects.RedBlackTree;

import java.util.List;

public class RedBlackTreeSort<T extends Comparable<T>> implements Sort<T> {

    @Override
    public List<T> apply(List<T> unsorted) {
        final RedBlackTree<T> sorted = RedBlackTree.ofAll(T::compareTo, unsorted);
        return sorted.iterator().toList().toJavaList();
    }
}
