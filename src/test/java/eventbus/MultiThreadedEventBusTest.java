package eventbus;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import static org.awaitility.Awaitility.await;


class MultiThreadedEventBusTest {

    @Test
    void givenNoHandlerWhenPublishEventShouldThrowException() {
        // given
        EventBus eventBus = new MultiThreadedEventBus(10);

        // expect
        thenThrownBy(() -> eventBus.publishEvent(new BigDecimal(22)))
                .isInstanceOf(NoHandlerException.class)
                .hasMessage("No handler found for event class java.math.BigDecimal");
    }

    @Test
    void givenAHandlerWhenPublishEventShouldInvokeIt() {
        // given
        EventBus eventBus = new MultiThreadedEventBus(10);
        StringBuilder aStringBuilder = new StringBuilder();
        EventHandler<String> appendStringHandler = aStringBuilder::append;
        eventBus.addSubscriber(String.class, appendStringHandler);

        // when
        eventBus.publishEvent("hello");

        // then
        await().atMost(ofSeconds(2))
                .untilAsserted(() -> assertThat(aStringBuilder).hasToString("hello"));
    }


    @Test
    void givenMultipleHandlersOfSameTypeWhenPublishEventShouldInvokeAll() {
        // given
        EventBus eventBus = new MultiThreadedEventBus(10);
        StringBuilder aStringBuilder = new StringBuilder();
        eventBus.addSubscriber(String.class, s -> aStringBuilder.append("1"));
        eventBus.addSubscriber(String.class, s -> aStringBuilder.append("2"));
        eventBus.addSubscriber(String.class, s -> aStringBuilder.append("3"));

        // when
        eventBus.publishEvent("hello subscribers");

        // then
        await().atMost(ofSeconds(2))
                .untilAsserted(() -> assertThat(aStringBuilder.toString()).contains("1", "2", "3"));
    }

    @Test
    void givenMultipleHandlersOfDifferentTypesWhenPublishEventShouldInvokeTheCorrectOnes() {
        // given
        EventBus eventBus = new MultiThreadedEventBus(10);
        StringBuilder aStringBuilder = new StringBuilder();
        eventBus.addSubscriber(String.class, s -> aStringBuilder.append("1"));
        eventBus.addSubscriber(Integer.class, s -> aStringBuilder.append("2"));
        eventBus.addSubscriber(Double.class, s -> aStringBuilder.append("3"));

        // when
        eventBus.publishEvent("hello subscribers");
        eventBus.publishEvent("hello again");
        eventBus.publishEvent(1);
        eventBus.publishEvent(1.0);

        // then
        await().atMost(ofSeconds(2))
                .untilAsserted(() -> assertThat(aStringBuilder.toString()).contains("1", "1", "2", "3"));

    }

    @Test
    void givenFilteredHandlersWhenPublishEventShouldApplyFiletering() {
        // given
        EventBus eventBus = new MultiThreadedEventBus(10);
        StringBuilder aStringBuilder = new StringBuilder();
        eventBus.addSubscriber(String.class, s -> aStringBuilder.append("1"));
        eventBus.addSubscriberForFilteredEvents(String.class, s -> aStringBuilder.append("2"), e -> e.contains("filtered"));


        // when
        eventBus.publishEvent("hello subscribers");
        eventBus.publishEvent("hello again");
        eventBus.publishEvent("filtered message");

        // then
        await().atMost(ofSeconds(2))
                .untilAsserted(() -> assertThat(aStringBuilder.toString()).contains("1", "1", "1", "2"));

    }
}