package ch.resrc.testing.capabilities.events;

/**
 * Trait interface that adds the capability to forward events of other
 * {@link Eventful} objects to the implementor's subscribers.
 */
public interface EventForwarding extends Notifiable {

    void publish(Object anEvent);

    default void on(Object notification) { publish(notification);}
}
