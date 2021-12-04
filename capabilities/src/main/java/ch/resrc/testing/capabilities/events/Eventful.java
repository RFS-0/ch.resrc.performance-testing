package ch.resrc.testing.capabilities.events;

public class Eventful {

    private final Dispatcher events = new Dispatcher();

    public Subscribable events() {

        return events;
    }

    public void publish(Object event) {

        events.dispatch(event);
    }
}
