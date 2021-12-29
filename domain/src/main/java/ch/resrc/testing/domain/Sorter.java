package ch.resrc.testing.domain;

import java.util.List;

@FunctionalInterface
public interface Sorter {

    <T extends Comparable<T>> List<T> apply(List<T> unsorted);

}
