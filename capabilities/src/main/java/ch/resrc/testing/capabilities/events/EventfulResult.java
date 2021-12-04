package ch.resrc.testing.capabilities.events;

import java.util.function.Consumer;

public class EventfulResult<T> {

    private final T value;
    private final Events events;

    private EventfulResult(T value, Events events)
    {
        this.value = value;
        this.events = events;
    }

    public static <U> EventfulResult<U> of(U value, Events events)
    {
        return new EventfulResult<>(value, events);
    }

    public T value()
    {
        return value;
    }

    public Events events()
    {
        return events;
    }

    public EventfulResult<T> applyEvents(Consumer<Events> eventConsumer) {
        eventConsumer.accept(events);
        return this;
    }

}
