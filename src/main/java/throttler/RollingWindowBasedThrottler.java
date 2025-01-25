package throttler;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class RollingWindowBasedThrottler implements Throttler {
    private final int maxRequests;
    private final long timeWindowMillis;
    private final Deque<Long> timestamps = new ArrayDeque<>();
    private final Lock lock = new ReentrantLock();
    private final Deque<Consumer<ThrottleResult>> subscriberQueue = new ArrayDeque<>();
    private final AtomicBoolean notifierStarted = new AtomicBoolean(false);
    private final ScheduledExecutorService notifierService = Executors.newSingleThreadScheduledExecutor();


    public RollingWindowBasedThrottler(int maxRequests, long timeWindowMillis) {
        this.maxRequests = maxRequests;
        this.timeWindowMillis = timeWindowMillis;
        startNotifierTask();
    }

    @Override
    public ThrottleResult shouldProceed() {
        long now = System.currentTimeMillis();
        lock.lock();
        try {
            cleanUpOldRequests(now);
            if (timestamps.size() < maxRequests) {
                timestamps.addLast(now);
                return ThrottleResult.PROCEED;
            } else {
                return ThrottleResult.DO_NOT_PROCEED;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void notifyWhenCanProceed(Consumer<ThrottleResult> callback) {
        subscriberQueue.addLast(callback);
        if (notifierStarted.compareAndSet(false, true)) {
            startNotifierTask();
        }
    }

    private void cleanUpOldRequests(long now) {
        while (!timestamps.isEmpty() && now - timestamps.peekFirst() > timeWindowMillis) {
            timestamps.removeFirst();
        }
    }

    private void startNotifierTask() {
        notifierService.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            lock.lock();
            try {
                cleanUpOldRequests(now);
                if (timestamps.size() < maxRequests) {
                    timestamps.addLast(now);
                    Consumer<ThrottleResult> nextSubscriber = subscriberQueue.pollFirst();
                    if (nextSubscriber != null) {
                        nextSubscriber.accept(ThrottleResult.PROCEED);
                    }
                }
            } finally {
                lock.unlock();
            }
        }, 0, 10, TimeUnit.MILLISECONDS);
    }
}
