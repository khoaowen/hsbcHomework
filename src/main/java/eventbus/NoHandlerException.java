package eventbus;

public class NoHandlerException extends RuntimeException {

    public <T> NoHandlerException(T e) {
        super("No handler found for event " + e.getClass());
    }
}
