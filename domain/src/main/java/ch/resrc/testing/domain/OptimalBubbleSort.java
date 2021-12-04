package ch.resrc.testing.domain;

import java.util.*;

public class OptimalBubbleSort<T extends Comparable<T>> implements Sort<T> {

    @Override
    public List<T> apply(List<T> unsorted) {
        int unsortedItems = unsorted.size();
        List<T> sorted = new ArrayList<>(unsorted);
        int swapCount = 0;
        int comparisonCount = 0;
        while (unsortedItems > 0) {
            int sortedUpTo = 0;
            for (int index = 1; index < unsortedItems; index++) {
                int lowerIndex = index - 1;
                int higherIndex = index;
                if (sorted.get(lowerIndex).compareTo(sorted.get(higherIndex)) > 0) {
                    swap(sorted, lowerIndex, higherIndex);
                    swapCount++;
                    sortedUpTo = higherIndex;
                }
                comparisonCount++;
            }
            unsortedItems = sortedUpTo;
        }
        System.out.printf("%d comparisons and %d swaps were necessary to sort the provided list %n", comparisonCount, swapCount);
        return sorted;
    }

    private void swap(List<T> toBeSwapped, int indexOfElement, int indexOfOtherElement) {
        T element = toBeSwapped.get(indexOfElement);
        T otherElement = toBeSwapped.get(indexOfOtherElement);
        toBeSwapped.remove(indexOfElement);
        toBeSwapped.add(indexOfElement, otherElement);
        toBeSwapped.remove(indexOfOtherElement);
        toBeSwapped.add(indexOfOtherElement, element);
    }
}
