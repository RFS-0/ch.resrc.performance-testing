package ch.resrc.testing.capabilities.functional;

import java.util.*;
import java.util.function.Consumer;

/**
 * Convenience functions to modify collections in a functional way by producing suitable copies or
 * using truly persistent collection data structures. Encapsulates our persistent collection design choices.
 * <p>
 * All collections returned by these functions are unmodifiable.
 * All collections returned by these functions allow null elements.
 * </p>
 */
public class PersistentCollections {

    ///// Lists /////

    public static <T> List<T> copyList(List<T> toCopy, Consumer<List<T>> modification) {

        List<T> theCopy = new ArrayList<>(toCopy);
        modification.accept(theCopy);
        return Collections.unmodifiableList(theCopy);
    }

    public static <T> List<T> removedFrom(List<T> theList, T element) {

        return copyList(theList, it -> it.remove(element));
    }

    public static <T> List<T> addedTo(List<T> theList, T element) {

        return copyList(theList, it -> it.add(element));
    }

    public static <T> List<T> addedTo(List<T> theList, Collection<? extends T> elements) {

        return copyList(theList, it -> it.addAll(elements));
    }


    ///// Sets /////

    private static <T> Set<T> copySet(Set<T> toCopy, Consumer<Set<T>> modifier) {

        Set<T> theCopy = new HashSet<>(toCopy);
        modifier.accept(theCopy);
        return Collections.unmodifiableSet(theCopy);
    }

    public static <T> Set<T> addedTo(Set<T> theSet, T element) {

        return copySet(theSet, it -> it.add(element));
    }

    public static <T> Set<T> removedFrom(Set<T> theSet, T element) {

        return copySet(theSet, it -> it.remove(element));
    }


}
