package bgu.spl.mics;
import bgu.spl.mics.application.objects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LiDarWorkerTrackerTest {

    private LiDarWorkerTracker tracker;

    @BeforeEach
    void setUp() throws IOException {
        tracker = new LiDarWorkerTracker(1, 0, STATUS.UP, new ArrayList<>());
        LiDarDataBase ds = LiDarDataBase.getInstance("./src/test/resources/lidar_data.json");

    }

    /**
     * @param:
     *       - List<DetectedObject> detectedObjects - a list of detected objects to process.
     *       - int currentTick - the current tick for processing.
     * @pre:
     *       - detectedObjects != null and contains valid DetectedObject instances with non-null IDs and descriptions.
     *       - LiDarWorkerTracker (tracker) is initialized and active.
     *       - LiDarDataBase contains matching cloud points for each detected object ID at the given tick.
     * @post:
     *       - Returns a List<TrackedObject> where each TrackedObject corresponds to a DetectedObject.
     *       - The size of the returned list matches the number of detected objects processed.
     *       - Each TrackedObject contains the same ID as its corresponding DetectedObject.
     */
    @Test
    void testProcessDetectedObjects() {
        List<DetectedObject> detectedObjects = Arrays.asList(
                new DetectedObject("Wall_3", "Wall"),
                new DetectedObject("Chair_Base_1", "Chair Base")
        );

        List<TrackedObject> result = tracker.processDetectedObjects(detectedObjects, 4);
        assertEquals(2, result.size());
        assertEquals("Wall_3", result.get(0).getId());
        assertEquals("Chair_Base_1", result.get(1).getId());
    }
}
