package bgu.spl.mics;
import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.Pose;
import org.junit.jupiter.api.*;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
public class FusionSlamTest {

    /**
     * @param: CloudPoint localPoint - a local coordinate with (x, y).
     *         Pose currentPose - the robot's global position and orientation with (x, y, yaw).
     * @pre: localPoint != null && currentPose != null.
     * @post:
     *       - The returned CloudPoint (globalPoint) represents the localPoint transformed to global coordinates.
     *       - globalPoint.x = (cos(yaw) * localPoint.x) - (sin(yaw) * localPoint.y) + currentPose.x.
     *       - globalPoint.y = (sin(yaw) * localPoint.x) + (cos(yaw) * localPoint.y) + currentPose.y.
     */
    @Test
    void testTransformToGlobal() {
        // Create a sample Pose
        Pose currentPose = new Pose(5, 5, 90, 10); // At (5, 5) with a yaw of 90 degrees

        // Create a local CloudPoint
        CloudPoint localPoint = new CloudPoint(1.0, 2.0);

        // Get the global coordinates using transformToGlobal
        CloudPoint globalPoint = FusionSlam.getInstance().transformToGlobal(localPoint, currentPose);

        // Expected global coordinates (90-degree rotation and translation by (5,5))
        assertEquals(3.0, globalPoint.getX(), 0.001, "Global X should match the transformed value");
        assertEquals(6.0, globalPoint.getY(), 0.001, "Global Y should match the transformed value");
    }


    /**
     * @param: List<CloudPoint> oldCoords - a list of existing coordinates.
     *         List<CloudPoint> newCoords - a list of new coordinates.
     * @pre: oldCoords != null && newCoords != null && oldCoords.size() >= 0 && newCoords.size() >= 0.
     * @post:
     *       - The returned List<CloudPoint> (merged) has a size equal to the smaller of oldCoords.size() and newCoords.size().
     *       - For each index i in the merged list:
     *         merged.get(i).x = (oldCoords.get(i).x + newCoords.get(i).x) / 2.
     *         merged.get(i).y = (oldCoords.get(i).y + newCoords.get(i).y) / 2.
     */
    @Test
    void testMergeCoordinates() {
        // Create old coordinates
        List<CloudPoint> oldCoords = new ArrayList<>();
        oldCoords.add(new CloudPoint(1.0, 2.0));
        oldCoords.add(new CloudPoint(3.0, 4.0));

        // Create new coordinates
        List<CloudPoint> newCoords = new ArrayList<>();
        newCoords.add(new CloudPoint(5.0, 6.0));
        newCoords.add(new CloudPoint(7.0, 8.0));

        // Merge the coordinates
        List<CloudPoint> mergedCoords = FusionSlam.getInstance().mergeCoordinates(oldCoords, newCoords);

        // Check the merged results
        assertEquals(2, mergedCoords.size(), "Merged list size should match the smaller of the two lists");

        // Verify the merged values
        assertEquals(3.0, mergedCoords.get(0).getX(), 0.001, "Merged X value at index 0 should be averaged");
        assertEquals(4.0, mergedCoords.get(0).getY(), 0.001, "Merged Y value at index 0 should be averaged");

        assertEquals(5.0, mergedCoords.get(1).getX(), 0.001, "Merged X value at index 1 should be averaged");
        assertEquals(6.0, mergedCoords.get(1).getY(), 0.001, "Merged Y value at index 1 should be averaged");
    }


}
