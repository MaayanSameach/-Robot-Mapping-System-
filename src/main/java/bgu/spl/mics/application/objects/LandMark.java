package bgu.spl.mics.application.objects;
import java.util.*;
/**
 * Represents a landmark in the environment map.
 * Landmarks are identified and updated by the FusionSlam service.
 */
public class LandMark {
    private final String id;
    private final String description;
    private List<CloudPoint> coordinates;

    public LandMark(String id, String description, List<CloudPoint> coordinates) {
        this.id = id;
        this.description = description;
        this.coordinates = coordinates;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }
    public void updateCoordinates(List<CloudPoint> newCoordinates) {
        coordinates= newCoordinates;
    }
    public List<CloudPoint> getCoordinates() {
        return coordinates;
    }
}
