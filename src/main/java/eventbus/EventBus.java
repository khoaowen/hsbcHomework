package eventbus;

import java.util.function.Predicate;

public interface EventBus {
    <T> void publishEvent(T e);

    <T> void addSubscriber(Class<T> classType, EventHandler<T> eventHandler);

    <T> void addSubscriberForFilteredEvents(Class<T> classType, EventHandler<T> eventHandler, Predicate<T> filter);

}