package bgu.spl.mics.application.objects;
import java.util.*;

import java.io.FileReader;
import java.io.IOException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {
    private final String filePath;
    private final List<StampedCloudPoints> cloudPoints;

    private LiDarDataBase(String filePath) throws IOException {
        this.filePath = filePath;
        cloudPoints = loadData(filePath);
    }

    private static class LidarDataSingletonHolder {
        private static LiDarDataBase instance;

        private static void initialize(String filePath) throws IOException {
            if (instance == null) {
                instance = new LiDarDataBase(filePath);
            }
        }
    }
    /**
     * Returns the singleton instance of LiDarDataBase.
     *
     * @param filePath The path to the LiDAR data file.
     * @return The singleton instance of LiDarDataBase.
     */
    public static LiDarDataBase getInstance(String filePath) throws IOException {
        LidarDataSingletonHolder.initialize(filePath);
        return LidarDataSingletonHolder.instance;
    }

    public static LiDarDataBase getInstance(){
        return LidarDataSingletonHolder.instance;
    }

    private List<StampedCloudPoints> loadData(String filePath) throws IOException {
        Gson gson = new Gson();
        List<StampedCloudPoints> temp = new ArrayList<>();
        try(FileReader reader = new FileReader(filePath)){
            Type stamptedListType = new TypeToken<List<StampedCloudPoints>>(){}.getType();
            temp = gson.fromJson(reader, stamptedListType);
        } catch (IOException e){
            throw new IOException("didnt read the file");
        }
        return temp;
    }

    public synchronized StampedCloudPoints getCloudPoints(String id) {
        for (StampedCloudPoints stamped : cloudPoints) {
            if (stamped.getId().equals(id)) {
                return stamped;
            }
        }
        return null;
    }
    public synchronized StampedCloudPoints getCloudPoints(String id, int time) {
        for (StampedCloudPoints stamped : cloudPoints) {
            if (stamped.getId().equals(id) && stamped.getTime() == time) {
                return stamped;
            }
        }
        return null;
    }

    /**
     * Retrieves all cloud points in the database.
     *
     * @return A list of all StampedCloudPoints.
     */
    public synchronized List<StampedCloudPoints> getCloudPoints() {
        return new ArrayList<>(cloudPoints);
    }
}
