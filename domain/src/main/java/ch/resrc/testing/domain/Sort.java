package ch.resrc.testing.domain;

import java.util.List;

@FunctionalInterface
public interface Sort<T extends Comparable<T>> {

    List<T> apply(List<T> unsorted);

}
