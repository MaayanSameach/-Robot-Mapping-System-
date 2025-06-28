package bgu.spl.mics.application.objects;
import java.util.*;
/**
 * Represents a group of cloud points corresponding to a specific timestamp.
 * Used by the LiDAR system to store and process point cloud data for tracked objects.
 */
public class StampedCloudPoints {
    private final String id;
    private final int time;
    private final List<List<Double>> cloudPoints;

    public StampedCloudPoints(String id, int time) {
        this.id = id;
        this.time = time;
        this.cloudPoints = new ArrayList<>();
    }

    public String getId() { return id; }
    public int getTime() { return time; }
    public List<List<Double>> getCloudPoints() { return cloudPoints; }

    public void addPoint(List<Double> point) {
        cloudPoints.add(point);
    }
}
