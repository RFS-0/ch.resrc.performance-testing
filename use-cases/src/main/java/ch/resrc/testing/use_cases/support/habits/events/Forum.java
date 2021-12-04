package ch.resrc.testing.use_cases.support.habits.events;

import ch.resrc.testing.capabilities.events.*;

/**
 * A lightweight event bus for loose coupling of use case role objects.
 * <p>
 * Use case roles publish events to the {@code Forum}. Other roles
 * that need to react to these events subscribe to the {@code Forum}.
 * They are notified immediately when an event gets published.
 * <p>
 * Each use case invocation creates its own {@code Forum} object
 * and subscribes its role instances to the {@code Forum}.
 * <p>
 * Example: A use case {@code Workflow} role publishes a completion event to the {@code Forum}.
 * The {@code UserInterface} role subscribes to the {@code Forum}. When it receives the completion event,
 * it presents the relevant result data to the user.
 */
public class Forum extends Eventful implements EventForwarding {
}
