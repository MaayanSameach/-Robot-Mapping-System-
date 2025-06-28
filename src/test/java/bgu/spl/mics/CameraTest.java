package bgu.spl.mics;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.STATUS;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
class CameraTest {

    private Camera camera;

    @BeforeEach
    void setUp() throws IOException {
        camera = new Camera(1,0, STATUS.UP,"./src/test/resources/camera_data.json");
    }

    /**
     * @param: int currentTick - the current simulation tick for which to retrieve detected objects.
     * @pre:
     *       - The Camera is initialized with a non-empty list of detected objects.
     *       - detectedObjectsList.stream().anyMatch(stamped -> stamped.getTime() + frequency == currentTick) == true.
     * @post:
     *       - Returns a list of DetectedObject where stamped.getTime() + frequency == currentTick.
     *       - The size of the returned list matches the number of objects that meet the condition.
     *       - Each DetectedObject in the list matches the expected id and description.
     */
    @Test
    void testGetDetectedObjects() {
        List<DetectedObject> result = camera.getDetectedObjects(4); // 4 + frequency == 15
        assertEquals(2, result.size());
        assertEquals("Chair_Base_1", result.get(0).getId());
        assertEquals("Chair Base", result.get(0).getDescription());
        assertEquals("Circular_Base_1", result.get(1).getId());
        assertEquals("Circular Base", result.get(1).getDescription());
    }

    /**
     * @param: int currentTick - the current simulation tick for which to retrieve detected objects.
     * @pre:
     *       - The Camera is initialized with a non-empty list of detected objects.
     *       - detectedObjectsList.stream().anyMatch(stamped -> stamped.getTime() + frequency == currentTick) == false.
     * @post:
     *       - Returns an empty list when no DetectedObject matches the condition stamped.getTime() + frequency == currentTick.
     */
    @Test
    void testGetDetectedObjectsNoMatch() {
        List<DetectedObject> result = camera.getDetectedObjects(21); // No match for tick 20
        assertTrue(result.isEmpty());
    }
}
