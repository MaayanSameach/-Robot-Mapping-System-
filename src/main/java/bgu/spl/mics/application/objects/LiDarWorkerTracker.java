package bgu.spl.mics.application.objects;
import java.util.*;
/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */
public class LiDarWorkerTracker {

    private final int id;
    private final int frequency;
    private STATUS status;
    private List<TrackedObject> lastTrackedObjects;

    public LiDarWorkerTracker(int id, int frequency, STATUS status, List<TrackedObject> lastTrackedObjects) {
        this.id = id;
        this.frequency = frequency;
        this.status = status;
        this.lastTrackedObjects = lastTrackedObjects;
        SensorsCounter.getInstance().increaseLIDarSensors();
    }

    public int getId() {
        return id;
    }

    public int getFrequency() {
        return frequency;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public void setLastTrackedObjects(List<TrackedObject> lastTrackedObjects) {
        this.lastTrackedObjects = lastTrackedObjects;
    }

    public List<TrackedObject> getLastTrackedObjects() {
        return lastTrackedObjects;
    }

    public List<TrackedObject> processDetectedObjects(List<DetectedObject> detectedObjects, int currentTick) {
        // Simulate processing and return tracked objects.
        List<TrackedObject> trackedObjects = new ArrayList<>();
        LiDarDataBase ds = LiDarDataBase.getInstance();
        if(ds!=null) {
            for (DetectedObject detected : detectedObjects) {
                StampedCloudPoints stampedPoints = ds.getCloudPoints(detected.getId(), currentTick);
                // Retrieve the raw cloud points (List<List<Double>>)
                if (stampedPoints != null) {
                    List<List<Double>> rawCloudPoints = stampedPoints.getCloudPoints();
                    // Convert raw cloud points to a list of CloudPoint
                    List<CloudPoint> cloudPoints = convertToCloudPoints(rawCloudPoints);
                    // Create a new TrackedObject using the cloud points
                    TrackedObject tracked = new TrackedObject(
                            detected.getId(),
                            stampedPoints.getTime(),
                            detected.getDescription(),
                            cloudPoints
                    );
                    trackedObjects.add(tracked);
                }
            }
        }

        lastTrackedObjects = trackedObjects;
        return trackedObjects;
    }

    private List<CloudPoint> convertToCloudPoints(List<List<Double>> rawCloudPoints) {
        List<CloudPoint> cloudPoints = new ArrayList<>();
        for (List<Double> point : rawCloudPoints) {
            if (point.size() >= 2) { // Ensure there are at least x and y coordinates
                cloudPoints.add(new CloudPoint(point.get(0), point.get(1)));
            }
        }
        return cloudPoints;
    }

    public List<TrackedObject> createTrackedObjects(StampedDetectedObjects stampedDetected) {
        // For each object, we search in the database for the matching cloudPoint according to the time. if the IDs match,
        // we create a new TrackedObject, add it to result and to our main recording list but if the cloudPoint is an ERROR, we return null.
        List<TrackedObject> result = new ArrayList<>();
        for (DetectedObject o: stampedDetected.getDetectedObjects()){
            for (StampedCloudPoints cp: LiDarDataBase.getInstance().getCloudPoints()){
                if (stampedDetected.getTime() == cp.getTime()){
                    if (o.getId().equals(cp.getId())){
                        List<CloudPoint> converted = convertToCloudPoints(cp.getCloudPoints()); // THE CHANGE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                        TrackedObject temp = new TrackedObject(o.getId(), cp.getTime(), o.getDescription(), converted); //double check what time we need to send!
                        result.add(temp);
                        break;
                    }
                    else if (cp.getId().equals("ERROR")){
                        return null;
                    }
                }
            }
        }
        return result;
    }
}
