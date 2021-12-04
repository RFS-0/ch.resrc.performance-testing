package ch.resrc.testing.capabilities.events;

import java.util.*;

public class Dispatcher implements Subscribable {

    private final List<Notifiable> listeners = new ArrayList<>();

    @Override
    public void subscribe(Notifiable object) {
        listeners.add(object);
    }

    public void dispatch(Object event) {
        listeners.forEach(it -> it.on(event));
    }
}
