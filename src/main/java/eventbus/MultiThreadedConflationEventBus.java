package eventbus;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Predicate;

public class MultiThreadedConflationEventBus implements EventBus {

    private final ExecutorService executorService;
    private final Map<Class<?>, List<FilteredEventHandler<?>>> handlers = new ConcurrentHashMap<>();
    private final Map<Class<?>, Object> latestEvents = new ConcurrentHashMap<>();
    private final BlockingQueue<Class<?>> eventQueue = new LinkedBlockingQueue<>();
    private final Set<Class<?>> pendingEventTypes = ConcurrentHashMap.newKeySet();

    public MultiThreadedConflationEventBus(int nThreads) {
        this.executorService = Executors.newFixedThreadPool(nThreads);
        var eventExecutorService = Executors.newFixedThreadPool(nThreads);

        // Start worker threads to process events
        for (int i = 0; i < nThreads; i++) {
            eventExecutorService.submit(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Class<?> eventType = eventQueue.take();
                        processLatestEvent(eventType);
                        pendingEventTypes.remove(eventType);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }

    }

    @Override
    public <T> void publishEvent(T e) {
        Objects.requireNonNull(e);
        Class<?> eventType = e.getClass();

        var eventHandlers = handlers.get(eventType);
        if (eventHandlers == null) {
            throw new NoHandlerException(e);
        }

        // Store the latest event
        latestEvents.put(eventType, e);

        // Enqueue the event type if it's not already being processed
        if (pendingEventTypes.add(eventType)) {
            eventQueue.offer(eventType);
        }
    }

    @Override
    public <T> void addSubscriber(Class<T> classType, EventHandler<T> eventHandler) {
        addSubscriberForFilteredEvents(classType, eventHandler, _ -> true);
    }

    @Override
    public <T> void addSubscriberForFilteredEvents(Class<T> classType, EventHandler<T> eventHandler, Predicate<T> filter) {
        handlers.computeIfAbsent(classType, k -> new ArrayList<>()).add(new FilteredEventHandler<>(eventHandler, filter));
    }

    private void processLatestEvent(Class<?> eventType) {
        // Retrieve the latest event for this type
        Object event = latestEvents.get(eventType);
        if (event == null) return;

        // Notify all relevant handlers
        var eventHandlers = handlers.get(eventType);
        eventHandlers.stream()
                .filter(handler -> ((FilteredEventHandler<Object>) handler).predicate().test(event))
                .forEach(handler -> {
                    var eventHandler = ((FilteredEventHandler<Object>) handler).eventHandler();
                    executorService.submit(() -> eventHandler.handle(event));
                });
    }

}
