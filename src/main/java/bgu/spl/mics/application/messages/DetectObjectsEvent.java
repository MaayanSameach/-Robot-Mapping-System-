package bgu.spl.mics.application.messages;
import java.util.*;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

public class DetectObjectsEvent implements Event<DetectedObject> {
    private final StampedDetectedObjects stampedDetectedObjects;

    /**
     * Constructor to initialize a DetectObjectsEvent.
     *
     * @param detectedObjects List of objects detected by the camera.
     * @param time            The timestamp of the detection.
     */
    public DetectObjectsEvent(List<DetectedObject> detectedObjects, int time) {
        stampedDetectedObjects = new StampedDetectedObjects(time, detectedObjects);
    }
    /**
     * Retrieves the list of detected objects.
     *
     * @return A list of detected objects.
     */
    public StampedDetectedObjects getDetectedObjects() {
        return stampedDetectedObjects;
    }
    /**
     * Retrieves the time of detection.
     *
     * @return The timestamp of the detection.
     */
    public int getTime() {
        return stampedDetectedObjects.getTime();
    }

    public StampedDetectedObjects getStampedDetectedObjects() {
        return stampedDetectedObjects;
    }

    @Override
    public String toString() {
        return "DetectObjectsEvent{" +
                "stampedDetectedObjects=" + stampedDetectedObjects +
                '}';
    }
}
