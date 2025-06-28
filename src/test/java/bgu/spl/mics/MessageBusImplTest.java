package bgu.spl.mics;

import bgu.spl.mics.application.services.TimeService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageBusImplTest {
    private MessageBusImpl messageBus;
    private MicroService mockMicroService;

    //Initializes an object to test
    @BeforeEach
    void setUp() {
        messageBus = MessageBusImpl.getInstance();
        mockMicroService = new MicroService("MockService") {
            @Override
            protected void initialize() {}
        };
    }

    /**
     * @param: A class that implements Event (TestEvent) and a registered MicroService (mockMicroService).
     * @pre: The MicroService is registered with the MessageBus via register(MicroService m).
     * @post: if (@pre: eventSubscribers.containsKey(TestEvent)):
     *               @post: eventSubscribers.get(TestEvent).contains(mockMicroService) & the Future for TestEvent is non-null.
     *        else (@pre: !eventSubscribers.containsKey(TestEvent)):
     *               @post: eventSubscribers contains TestEvent mapped to a queue that includes mockMicroService, and the Future is non-null.
     */
    @Test
    void testSubscribeEvent() {
        class TestEvent implements Event<String> {}

        messageBus.register(mockMicroService);
        messageBus.subscribeEvent(TestEvent.class, mockMicroService);

        TestEvent testEvent = new TestEvent();
        Future<String> future = messageBus.sendEvent(testEvent);

        assertNotNull(future, "Future should not be null for a subscribed event type");
    }

    /**
     * @param: A class that implements Event (TestEvent) and a registered, subscribed MicroService (mockMicroService).
     * @pre: The MicroService is registered with the MessageBus using register(MicroService m) and subscribed to the event type (TestEvent.class) via subscribeEvent.
     * @post: if (@pre: eventSubscribers.containsKey(TestEvent) && eventSubscribers.get(TestEvent).contains(mockMicroService)):
     *               @post: The TestEvent is added to the MicroService's queue & a new Future is created and mapped to the event.
     *        else (@pre: eventSubscribers does not include TestEvent or mockMicroService is not subscribed):
     *               @post: Future is null, and the event is not processed.
     */
    @Test
    void testSendEvent() throws InterruptedException {
        class TestEvent implements Event<String> {}

        messageBus.register(mockMicroService);
        messageBus.subscribeEvent(TestEvent.class, mockMicroService);

        TestEvent testEvent = new TestEvent();
        Future<String> future = messageBus.sendEvent(testEvent);

        assertNotNull(future, "Future should not be null for a valid event");
        Message receivedMessage = messageBus.awaitMessage(mockMicroService);
        assertEquals(testEvent, receivedMessage, "MicroService should receive the correct event");
    }

    /**
     * @param: A class that implements Event (TestEvent) and a MicroService (mockMicroService) that has not been registered.
     * @pre: The MicroService is not registered with the MessageBus (register(MicroService m) has not been called for mockMicroService).
     * @post: The method awaitMessage(mockMicroService) throws an IllegalStateException with the message "MicroService is not registered!".
     */
    @Test
    void testAwaitMessageThrowsExceptionIfNotRegistered() {
        class TestEvent implements Event<String> {}

        Exception exception = assertThrows(
                IllegalStateException.class,
                () -> messageBus.awaitMessage(mockMicroService),
                "Should throw IllegalStateException for unregistered MicroService"
        );

        assertEquals("MicroService is not registered!", exception.getMessage());
    }
}