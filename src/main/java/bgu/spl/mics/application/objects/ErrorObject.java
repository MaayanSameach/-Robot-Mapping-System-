package bgu.spl.mics.application.objects;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ErrorObject<T> {
    private static class SingletonHolder {
        private static final ErrorObject instance = new ErrorObject();
    }

    private String error;
    private String faultySensor;
    private ConcurrentHashMap<String, StampedDetectedObjects> lastCamerasFrame;
    private ConcurrentHashMap<String, List<TrackedObject>> lastLidarFrames;
    private List<Pose> poses;
    private StatisticalFolder statistics;
    private List<LandMark> landMarks;


    private ErrorObject() {
        error = "";
        faultySensor = "";
        lastCamerasFrame = new ConcurrentHashMap<>();
        lastLidarFrames = new ConcurrentHashMap<>();
        poses = new ArrayList<>();
        statistics = StatisticalFolder.getInstance();
        landMarks = new ArrayList<>();
    }

    public static ErrorObject getInstance() {
        return ErrorObject.SingletonHolder.instance;
    }

    public String getErrorString() {
        return error;
    }

    public void setErrorString(String errorString) {
        this.error = errorString;
    }

    public String getFaultySensor() {
        return faultySensor;
    }

    public void setFaultySensor(String faultySensor) {
        this.faultySensor = faultySensor;
    }

    public ConcurrentHashMap<String, StampedDetectedObjects> getLastCamerasFrame() {
        return lastCamerasFrame;
    }

    public void updateLastCamerasFrame(String s, StampedDetectedObjects stampedDetectedObjects) {
        if(!lastCamerasFrame.containsKey(s)) {
            lastCamerasFrame.put(s, stampedDetectedObjects);
        }
        else{
            lastCamerasFrame.remove(s);
            lastCamerasFrame.put(s, stampedDetectedObjects);
        }
    }

    public void updateLastLiDarWorkerTrackersFrame(String s, List<TrackedObject> list) {
        if(!lastLidarFrames.containsKey(s)) {
            lastLidarFrames.put(s, list);
        }
        else{
            lastLidarFrames.remove(s);
            lastLidarFrames.put(s, list);
        }
    }

    public void addPose(Pose pose) {
        poses.add(pose);
    }

    public List<LandMark> getLandMarks() {
        return landMarks;
    }

    public void setLandMarks(List<LandMark> landMarks) {
        this.landMarks = landMarks;
    }
}