package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

public class OutputFile {

    private int systemRuntime;
    private int numDetectedObjects;
    private int numTrackedObjects;
    private int numLandmarks;
    private List<LandMark> landmarks;

    public OutputFile(int sysRunTime, int numDetected, int numTracked,int lndmrks, List<LandMark> lndmrksList) {
        this.systemRuntime = sysRunTime;
        this.numDetectedObjects = numDetected;
        this.numTrackedObjects = numTracked;
        this.numLandmarks = lndmrks;
        this.landmarks=lndmrksList;
    }
}
