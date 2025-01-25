package throttler;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static throttler.Throttler.ThrottleResult.DO_NOT_PROCEED;
import static throttler.Throttler.ThrottleResult.PROCEED;

class RollingWindowBasedThrottlerTest {

    @Test
    void givenEnoughRequestsWithinRange_WhenPoll_ThenAllAreAllowed() {
        // given
        var rollingWindowBasedThrottler = new RollingWindowBasedThrottler(3, 1000);

        // expect
        assertThat(rollingWindowBasedThrottler.shouldProceed()).isEqualTo(PROCEED);
        assertThat(rollingWindowBasedThrottler.shouldProceed()).isEqualTo(PROCEED);
        assertThat(rollingWindowBasedThrottler.shouldProceed()).isEqualTo(PROCEED);

    }

    @Test
    void givenAnExtraRequest_WhenPoll_ThenShouldNotProceed() {
        // given
        var rollingWindowBasedThrottler = new RollingWindowBasedThrottler(1, 1000);

        // expect
        assertThat(rollingWindowBasedThrottler.shouldProceed()).isEqualTo(PROCEED);
        assertThat(rollingWindowBasedThrottler.shouldProceed()).isEqualTo(DO_NOT_PROCEED);

    }

    @Test
    void givenEnoughRequests_WhenPush_ThenShouldProceed() {
        // given
        var rollingWindowBasedThrottler = new RollingWindowBasedThrottler(1, 1000);
        AtomicInteger counter = new AtomicInteger();
        // when
        rollingWindowBasedThrottler.notifyWhenCanProceed(_ -> counter.getAndIncrement());
        // then
        await().until(() -> counter.get() == 1);
    }

    @Test
    void givenExtraRequests_WhenPush_ThenShouldDelay() {
        // given
        var rollingWindowBasedThrottler = new RollingWindowBasedThrottler(1, 1000);
        AtomicInteger counter = new AtomicInteger();
        // when
        rollingWindowBasedThrottler.notifyWhenCanProceed(_ -> counter.getAndIncrement());
        rollingWindowBasedThrottler.notifyWhenCanProceed(_ ->
                counter.getAndIncrement());
        // then
        await().atMost(Duration.ofSeconds(2)).until(() -> counter.get() == 1);
        await()
                .pollDelay(Duration.ofSeconds(1)) // Ensure at least 1 second passes
                .atMost(Duration.ofSeconds(3))   // Allow up to 3 seconds for the event
                .until(() -> counter.get() == 2);

    }


}