package bgu.spl.mics.application.objects;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.StandardSocketOptions;
import java.util.*;
import java.util.List;

/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {
    private final int id;
    private final int frequency;
    private STATUS status;
    private final List<StampedDetectedObjects> detectedObjectsList;

    public Camera(int id, int frequency, STATUS status, String filename) throws IOException {
        this.id = id;
        this.frequency = frequency;
        this.status = status;
        this.detectedObjectsList = loadData(filename);
        SensorsCounter.getInstance().increaseCameraSensors();
    }

    private List<StampedDetectedObjects> loadData(String filePath) throws IOException {
        Gson gson = new Gson();
        List<StampedDetectedObjects> temp = new ArrayList<>();
        try (FileReader reader = new FileReader(filePath)) {
            // Parse the JSON as a generic object
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            // Get the camera-specific data by key
            JsonArray stampedArray = jsonObject.getAsJsonArray("camera"+id); // Adjust key if needed
            // Convert the array to a list of StampedDetectedObjects
            Type stampedListType = new TypeToken<List<StampedDetectedObjects>>() {}.getType();
            temp = gson.fromJson(stampedArray, stampedListType);
        } catch (IOException e) {
            throw new IOException("Failed to read the file: " + filePath, e);
        } catch (JsonSyntaxException | IllegalStateException e) {
            throw new IOException("Invalid JSON structure in file: " + filePath, e);
        }
        return temp;
    }

    public int getId() {
        return id;
    }

    public int getFrequency() {
        return frequency;
    }
    public List<StampedDetectedObjects> detectedObjectsList() {
        return detectedObjectsList;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public synchronized List<DetectedObject> getDetectedObjects(int currentTick){
        List<DetectedObject> temp =  detectedObjectsList.stream()
                .filter(stamped -> stamped.getTime() + frequency == currentTick)
                .findFirst()
                .map(StampedDetectedObjects::getDetectedObjects)
                .orElse(Collections.emptyList());

        return temp;
    }

    public boolean checkIfLeft(int tick){
        StampedDetectedObjects lastObjects = detectedObjectsList.get(detectedObjectsList.size() - 1);
        if (lastObjects.getTime() + getFrequency() < tick){
            return false;
        }
        return true;
    }
}