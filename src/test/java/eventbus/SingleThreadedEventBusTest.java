package eventbus;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;


class SingleThreadedEventBusTest {

    @Test
    void givenNoHandlerWhenPublishEventShouldThrowException() {
        // given
        EventBus eventBus = new SingleThreadedEventBus();

        // expect
        thenThrownBy(() -> eventBus.publishEvent(new BigDecimal(22)))
                .isInstanceOf(NoHandlerException.class)
                .hasMessage("No handler found for event class java.math.BigDecimal");
    }

    @Test
    void givenAHandlerWhenPublishEventShouldInvokeIt() {
        // given
        EventBus eventBus = new SingleThreadedEventBus();
        StringBuilder aStringBuilder = new StringBuilder();
        EventHandler<String> appendStringHandler = aStringBuilder::append;
        eventBus.addSubscriber(String.class, appendStringHandler);

        // when
        eventBus.publishEvent("hello");

        // then
        assertThat(aStringBuilder).hasToString("hello");
    }


    @Test
    void givenMultipleHandlersOfSameTypeWhenPublishEventShouldInvokeAll() {
        // given
        EventBus eventBus = new SingleThreadedEventBus();
        StringBuilder aStringBuilder = new StringBuilder();
        eventBus.addSubscriber(String.class, s -> aStringBuilder.append("1"));
        eventBus.addSubscriber(String.class, s -> aStringBuilder.append("2"));
        eventBus.addSubscriber(String.class, s -> aStringBuilder.append("3"));

        // when
        eventBus.publishEvent("hello subscribers");

        // then
        assertThat(aStringBuilder).hasToString("123");
    }

    @Test
    void givenMultipleHandlersOfDifferentTypesWhenPublishEventShouldInvokeTheCorrectOnes() {
        // given
        EventBus eventBus = new SingleThreadedEventBus();
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
        assertThat(aStringBuilder).hasToString("1123");
    }


    @Test
    void givenFilteredHandlersWhenPublishEventShouldApplyFiletering() {
        // given
        EventBus eventBus = new SingleThreadedEventBus();
        StringBuilder aStringBuilder = new StringBuilder();
        eventBus.addSubscriber(String.class, s -> aStringBuilder.append("1"));
        eventBus.addSubscriberForFilteredEvents(String.class, s -> aStringBuilder.append("2"), e -> e.contains("filtered"));


        // when
        eventBus.publishEvent("hello subscribers");
        eventBus.publishEvent("hello again");
        eventBus.publishEvent("filtered message");

        // then
        assertThat(aStringBuilder).hasToString("1112");
    }
}