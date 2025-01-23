package eventbus;

import java.util.*;
import java.util.function.Predicate;

public class SingleThreadedEventBus implements EventBus {

    private final Map<Class<?>, List<FilteredEventHandler<?>>> handlers = new HashMap<>();

    @Override
    public <T> void publishEvent(T e) {
        Objects.requireNonNull(e);
        var eventHandlers = handlers.get(e.getClass());
        if (eventHandlers == null) {
            throw new NoHandlerException(e);
        }
        eventHandlers.stream()
                .filter(handler -> ((FilteredEventHandler<T>) handler).predicate().test(e))
                .forEach(handler -> ((FilteredEventHandler<T>) handler).eventHandler().handle(e));
    }

    @Override
    public <T> void addSubscriber(Class<T> classType, EventHandler<T> eventHandler) {
        addSubscriberForFilteredEvents(classType, eventHandler, _ -> true);
    }

    @Override
    public <T> void addSubscriberForFilteredEvents(Class<T> classType, EventHandler<T> eventHandler, Predicate<T> filter) {
        handlers.computeIfAbsent(classType, _ -> new ArrayList<>()).add(new FilteredEventHandler<>(eventHandler, filter));
    }
}
