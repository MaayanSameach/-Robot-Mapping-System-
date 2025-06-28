package bgu.spl.mics.application.objects;

import bgu.spl.mics.Message;
import bgu.spl.mics.MicroService;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Holds statistical information about the system's operation.
 * This class aggregates metrics such as the runtime of the system,
 * the number of objects detected and tracked, and the number of landmarks identified.
 */
public class StatisticalFolder {
    private static StatisticalFolder instance;
    private AtomicInteger systemRuntime;
    private AtomicInteger numDetectedObjects;
    private AtomicInteger numTrackedObjects;
    private AtomicInteger numLandmarks;
    private Map<String, LandMark> landMarks;


    // Private constructor to prevent instantiation
    private StatisticalFolder() {
        this.systemRuntime = new AtomicInteger(0);
        this.numDetectedObjects = new AtomicInteger(0);
        this.numTrackedObjects = new AtomicInteger(0);
        this.numLandmarks = new AtomicInteger(0);
        this.landMarks = new ConcurrentHashMap<>();

    }

    // Public method to get the instance
    public static synchronized StatisticalFolder getInstance() {
        if (instance == null) {
            instance = new StatisticalFolder();
        }
        return instance;
    }

    public synchronized void incrementDetectedObjects() {
        numDetectedObjects.incrementAndGet();
    }
    public synchronized void incrementTrackedObjects() {
        numTrackedObjects.incrementAndGet();
    }
    public synchronized void incrementLandmarks() {
        numLandmarks.incrementAndGet();
    }

    public synchronized void incrementRuntime() {
        systemRuntime.incrementAndGet();
    }

    // Getters
    public int getSystemRuntime() { return systemRuntime.intValue(); }
    public int getNumDetectedObjects() { return numDetectedObjects.intValue(); }
    public int getNumTrackedObjects() { return numTrackedObjects.intValue(); }
    public int getNumLandmarks() { return numLandmarks.intValue(); }

    public void addStatisticalLandMark(String id, LandMark toAdd){
        if(landMarks.containsKey(id))
        {
            landMarks.remove(id);
            landMarks.put(id,toAdd);
        }
        else
            landMarks.put(id,toAdd);
    }

}
