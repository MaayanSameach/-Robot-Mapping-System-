package bgu.spl.mics.application.objects;
import java.util.*;
import bgu.spl.mics.application.objects.LandMark;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {
    // Fields
    private final List<LandMark> landmarks; // Represents the map of the environment
    private final List<Pose> poses;     // Represents previous Poses needed for calculations
    private String basePath;
    private List<TrackedObject> trackedObjects;

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getBasePath() {
        return basePath;
    }

    // Private constructor to enforce Singleton pattern
    private FusionSlam() {
        // Initialize fields
        this.landmarks = new ArrayList<LandMark>(); // Start with an empty map
        this.poses = new ArrayList<>();   // Initialize an empty list of poses
        this.trackedObjects = new ArrayList<>();
    }
    // Singleton instance holder
    private static class FusionSlamHolder {
        private static final FusionSlam INSTANCE = new FusionSlam();
    }

    // Public method to access the Singleton instance
    public static FusionSlam getInstance() {
        return FusionSlamHolder.INSTANCE;
    }

    // Method to add a landmark to the map
    public void addLandmark(LandMark landmark) {
       landmarks.add(landmark);
    }

    // Method to get all landmarks
    public List<LandMark> getLandmarks() {
        return landmarks;
    }

    // Method to add a pose to the list
    public void addPose(Pose pose) {
        poses.add(pose);
    }

    public Pose getPoseAtTime(int time) {
        for (Pose pose : poses) {
            if (time == pose.getTime()) {
                return pose;
            }
        }
        return null;
    }
    // Method to get all poses
    public List<Pose> getPoses() {
        return poses;
    }

    // Example method to clear the map (reset landmarks and poses)
    public void resetMap() {
        this.landmarks.clear();
        this.poses.clear();
    }

    public LandMark findLandmarkById(String id) {
        for (LandMark landmark : landmarks) {
            if (landmark.getId().equals(id)) {
                return landmark;
            }
        }
        return null;
    }

    public void processLandmarks() {
        List<TrackedObject> waitList = new ArrayList<>();
        for (TrackedObject obj : trackedObjects) {
            Pose currentPose = getPoseAtTime(obj.getTime());
            if(currentPose == null) {
                waitList.add(obj);
                continue;
            }
            List<CloudPoint> globalCoordinates = new ArrayList<>();
            // Transform local coordinates to global coordinates
            for (CloudPoint localPoint : obj.getCoordinates()) {
                CloudPoint globalPoint = transformToGlobal(localPoint, currentPose);
                globalCoordinates.add(globalPoint);
            }
            // Update or create a landmark
            LandMark existingLandmark = findLandmarkById(obj.getId());

            if (existingLandmark == null) {
                // New landmark
                LandMark temp = new LandMark(obj.getId(), obj.getDescription(), globalCoordinates);
                addLandmark(temp);
                StatisticalFolder.getInstance().incrementLandmarks();
                StatisticalFolder.getInstance().addStatisticalLandMark(temp.getId(),temp);
            } else {
                // Update existing landmark
                List<CloudPoint> updatedCoordinates = mergeCoordinates(existingLandmark.getCoordinates(), globalCoordinates);
                existingLandmark.updateCoordinates(updatedCoordinates);
                StatisticalFolder.getInstance().addStatisticalLandMark(existingLandmark.getId(),existingLandmark);
            }
        }
        trackedObjects = waitList;
    }

    public synchronized CloudPoint transformToGlobal(CloudPoint localPoint, Pose currentPose) {
        double theta = Math.toRadians(currentPose.getYaw());
        double cosTheta = Math.cos(theta);
        double sinTheta = Math.sin(theta);
        double globalX = (cosTheta * localPoint.getX()) - (sinTheta * localPoint.getY()) + (currentPose.getX());
        double globalY = (sinTheta * localPoint.getX()) + (cosTheta * localPoint.getY()) + (currentPose.getY());
        return new CloudPoint(globalX, globalY);
    }

    public synchronized List<CloudPoint> mergeCoordinates(List<CloudPoint> oldCoords, List<CloudPoint> newCoords) {
        // Simple averaging for refinement
        List<CloudPoint> merged = new ArrayList<>();
        for (int i = 0; i < Math.min(oldCoords.size(), newCoords.size()); i++) {
            double avgX = (oldCoords.get(i).getX() + newCoords.get(i).getX()) / 2;
            double avgY = (oldCoords.get(i).getY() + newCoords.get(i).getY()) / 2;
            merged.add(new CloudPoint(avgX, avgY));
        }
        return merged;
    }

    public void addToTrackedObjects(List<TrackedObject> trackedObject) {
        trackedObjects.addAll(trackedObject);
    }
}
