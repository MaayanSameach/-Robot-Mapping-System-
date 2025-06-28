package bgu.spl.mics.application.messages;
import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.TrackedObject;
import java.util.List;
public class TrackedObjectEvent implements Event<TrackedObject> {
    private List<TrackedObject> trackedObjects;

    public TrackedObjectEvent(List<TrackedObject> trackedObjects) {
        this.trackedObjects = trackedObjects;
    }

    public List<TrackedObject> getTrackedObjects() {
        return trackedObjects;
    }
}
