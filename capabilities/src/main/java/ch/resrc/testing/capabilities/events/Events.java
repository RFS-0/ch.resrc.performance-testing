package ch.resrc.testing.capabilities.events;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Events {

    private final List<Event> events = new ArrayList<>();


    public static Events none() {
        return new Events();
    }

    public static Events of() {
        return new Events();
    }

    public static Events of(Event event) {
        return none().added(event);
    }

    public static Events of(Collection<Event> events) {
        return Events.none().added(events);
    }

    private Events() {/* Use static factory methods */}

    private Events(Events other) {
        this.events.addAll(other.events);
    }


    public List<Event> events() {
        return new ArrayList<>(events);
    }

    public Stream<Event> stream() {
        return events.stream();
    }


    public Events added(Event toBeEmitted) {
        return added(List.of(toBeEmitted));
    }

    public Events added(Collection<? extends Event> toBeEmitted) {
        Events result = new Events(this);
        result.events.addAll(toBeEmitted);
        return result;
    }

    public Events added(Events other) {
        return this.added(other.events);
    }

    public <E extends Event> Events on(Class<E> eventType, Consumer<? super E> action) {
        findEventsOf(eventType).forEach(action);
        return this;
    }

    public <E extends Event> Events on(Class<E> eventType, Runnable action) {
        findEventsOf(eventType).forEach(event -> action.run());
        return this;
    }

    public Events onAny(Consumer<Event> action) {
        events.forEach(action);
        return this;
    }

    public boolean isEmpty() { return events.isEmpty(); }

    private <E> Stream<E> findEventsOf(Class<E> eventType) {
        return this.events().stream()
                .filter(event -> eventType.isAssignableFrom(event.getClass()))
                .map(eventType::cast);
    }

}
