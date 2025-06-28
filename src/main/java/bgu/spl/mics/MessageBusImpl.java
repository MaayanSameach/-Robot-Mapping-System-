package bgu.spl.mics;

import java.util.*;
import java.util.concurrent.*;
/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only one public method (in addition to getters which can be public solely for unit testing) may be added to this class
 * All other methods and members you add the class must be private.
 */
public class MessageBusImpl implements MessageBus {
	private final Map<MicroService, BlockingQueue<Message>> microServiceQueues;
	private final Map<Class<? extends Message>, Queue<MicroService>> eventSubscribers;
	private final Map<Class<? extends Broadcast>, List<MicroService>> broadcastSubscribers;
	private final ConcurrentHashMap<Event<?>, Future<?>> eventFutureMap;

	private static class SingletonHolder{
		private static final MessageBusImpl INSTANCE = new MessageBusImpl();
	}

	private MessageBusImpl() {
		microServiceQueues = new ConcurrentHashMap<>();
		eventSubscribers = new ConcurrentHashMap<>();
		broadcastSubscribers = new ConcurrentHashMap<>();
		eventFutureMap = new ConcurrentHashMap<>();
	}

	public static MessageBusImpl getInstance() {
		return SingletonHolder.INSTANCE;
	}

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		eventSubscribers.computeIfAbsent(type, k -> new LinkedBlockingQueue<>()).add(m);
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		broadcastSubscribers.computeIfAbsent(type, k -> new CopyOnWriteArrayList <>()).add(m);
	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		Future<T> future = (Future<T>) eventFutureMap.remove(e);
		if (future != null) {
			future.resolve(result);
		}
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		// Log the broadcast being sent
		// Retrieve the list of services subscribed to this broadcast type
		List<MicroService> msList = broadcastSubscribers.get(b.getClass());
		// If there are no services subscribed, return early (nothing to send)
		if (msList == null || msList.isEmpty()) {
			return;
		}
		// Add the broadcast to the queues of all subscribed services
		for (MicroService ms : msList) {
			BlockingQueue<Message> queue = microServiceQueues.get(ms);
			if (queue != null) {
				queue.add(b); // Add the broadcast to the queue
			}
		}
	}

	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		MicroService ms =null;
		synchronized (eventSubscribers) {
			Queue<MicroService> queue = eventSubscribers.get(e.getClass());
			if (queue == null || queue.isEmpty())
				return null;
			ms = queue.poll(); // Get the next MicroService
			queue.add(ms); // Add it back to the end of the queue for round-robin
		}
		// Make sure the queue for the selected service exists before sending the event
		BlockingQueue<Message> queue = microServiceQueues.get(ms);
		if (queue != null) {
			queue.add(e);
		} else {
			return null;
		}

		Future<T> future = new Future<>();
		eventFutureMap.put(e, future);
		return future;
	}

	@Override
	public void register(MicroService m) {
		microServiceQueues.putIfAbsent(m, new LinkedBlockingQueue<Message>());
	}

	@Override
	public synchronized void unregister(MicroService m) {
		// Remove the MicroService's message queue
		BlockingQueue<Message> queue = microServiceQueues.remove(m);
		if (queue != null) {
			queue.clear();
		}

		// Remove subscriptions
		eventSubscribers.values().forEach(q -> q.remove(m));
		broadcastSubscribers.values().forEach(q -> q.remove(m));

		// Clean up unresolved futures
		eventFutureMap.keySet().removeIf(e -> {
			Queue<MicroService> subscribers = eventSubscribers.get(e.getClass());
			return subscribers != null && !subscribers.contains(m);
		});
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		if(!microServiceQueues.containsKey(m)){
			throw new IllegalStateException ("MicroService is not registered!");
		}
		return microServiceQueues.get(m).take();
	}

}
