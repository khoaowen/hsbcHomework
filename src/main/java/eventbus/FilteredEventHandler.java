package eventbus;

import java.util.function.Predicate;

public record FilteredEventHandler<T>(EventHandler<T> eventHandler, Predicate<T> predicate) {
    
}
