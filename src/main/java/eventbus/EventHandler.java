package eventbus;

@FunctionalInterface
public interface EventHandler<T> {
    void handle(T event);
}
