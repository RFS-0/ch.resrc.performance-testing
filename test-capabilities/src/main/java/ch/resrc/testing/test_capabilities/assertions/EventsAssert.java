package ch.resrc.testing.test_capabilities.assertions;

import ch.resrc.testing.capabilities.error_handling.faults.Defect;
import ch.resrc.testing.capabilities.events.*;
import org.assertj.core.api.AbstractAssert;

import java.util.*;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class EventsAssert extends AbstractAssert<EventsAssert, Events> {

    private EventsAssert(Events actual) {

        super(actual, EventsAssert.class);
    }

    public static EventsAssert assertThatEvents(Events actual) {

        return new EventsAssert(actual);
    }


    public EventsAssert hasNothingEmitted() {

        Collection<Event> events = actual.events();

        if (!events.isEmpty()) {
            failWithMessage("No events should have been emitted, but there are <%s> events: %s", events.size(), events);
        }

        return this;
    }

    public <E> EventsAssert hasEmitted(int expectedNumber, Class<E> expectedType) {

        long numEventsOfExpectedType = actual.events().stream()
                                             .filter(anEvent -> anEvent.getClass().isAssignableFrom(expectedType))
                                             .count();

        if (numEventsOfExpectedType != expectedNumber) {
            failWithMessage("There were <%s> events of type <%s> emitted. Expected: <%s>.",
                            numEventsOfExpectedType, expectedType.getName(), expectedNumber);
        }

        return this;
    }

    @SafeVarargs
    public final <E extends Event> EventsAssert hasEmittedExactly(Class<? extends E>... expected) {

        List<Class<? extends Event>> actualEvents = actual.events().stream()
                                                          .map(Event::getClass)
                                                          .collect(toList());

        assertThat(actualEvents).containsExactly(expected);

        return this;
    }

    public <E extends Event> EventsAssert hasEmittedExactlyOne(Class<E> expectedType) {

        return hasEmitted(1, expectedType);
    }

    public <E extends Event> EventsAssert hasEmittedExactlyOneSatisfying(Class<E> expectedType, Consumer<E> requirements) {

        hasEmittedExactlyOne(expectedType);

        E event = actual.events().stream()
                        .filter(anEvent -> anEvent.getClass().isAssignableFrom(expectedType))
                        .map(expectedType::cast)
                        .reduce((e1, e2) -> {
                            throw Defect.of("There should be exactly one event of type " + expectedType.getName());
                        })
                        .orElseThrow();

        assertThat(event).satisfies(requirements);

        return this;
    }


}
