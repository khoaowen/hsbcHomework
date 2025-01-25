package throttler;

import java.util.function.Consumer;

public interface Throttler {

    // check if we can proceed (poll)
    ThrottleResult shouldProceed();

    // subscribe to be told when we can proceed (Push)
    void notifyWhenCanProceed(Consumer<ThrottleResult> callback);

    enum ThrottleResult {
        PROCEED, // publish, aggregate etc
        DO_NOT_PROCEED //
    }
}