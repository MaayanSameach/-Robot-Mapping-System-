package bgu.spl.mics.application.objects;

import java.util.LinkedList;
import java.util.Queue;

public class PendingQueue {
    private final Queue<StampedDetectedObjects> pendingEvents;

    public PendingQueue() {
        pendingEvents = new LinkedList<>();
    }

    private static class PendingQueueHolder {
        private static final PendingQueue INSTANCE = new PendingQueue();
    }

    public static PendingQueue getInstance() {
        return PendingQueue.PendingQueueHolder.INSTANCE;
    }

    public Queue<StampedDetectedObjects> getPendingEvents() {
        return pendingEvents;
    }

    public void addEvent(StampedDetectedObjects event) {
        pendingEvents.add(event);
    }

    public StampedDetectedObjects getEvent(){
        return pendingEvents.poll();
    }

    public boolean isEmpty(){
        return pendingEvents.isEmpty();
    }

    public StampedDetectedObjects peek(){
        return pendingEvents.peek();
    }


}
