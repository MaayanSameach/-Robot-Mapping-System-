package bgu.spl.mics.application.services;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.messages.TrackedObjectEvent;
import bgu.spl.mics.application.objects.LandMark;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javax.print.attribute.DocAttributeSet;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

/**
 * FusionSlamService integrates data from multiple sensors to build and update
 * the robot's global map.
 *
 * This service receives TrackedObjectsEvents from LiDAR workers and PoseEvents from the PoseService,
 * transforming and updating the map with new landmarks.
 */
public class FusionSlamService extends MicroService {
    private FusionSlam fs;
    private int proccessedObj=0;
    /**
     * Constructor for FusionSlamService.
     *
     * @param fusionSlam The FusionSLAM object responsible for managing the global map.
     */
    public FusionSlamService(FusionSlam fusionSlam) {
        super("FusionSlamService");
        fs = fusionSlam;
    }
    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and TickBroadcasts,
     * and sets up callbacks for updating the global map.
     */
    @Override
    protected synchronized void initialize() {
        StatisticalFolder sf=StatisticalFolder.getInstance();
        // Subscribe to TrackedObjectsEvent
        subscribeEvent(TrackedObjectEvent.class, trackedObjectsList -> {
            List<TrackedObject> trackedObjects = trackedObjectsList.getTrackedObjects();
            fs.addToTrackedObjects(trackedObjects);
            fs.processLandmarks();
        });

        // Subscribe to PoseEvent
        subscribeEvent(PoseEvent.class, event -> {
            fs.addPose(event.getPose());
            fs.processLandmarks();
        });

        // Subscribe to TickBroadcast
        subscribeBroadcast(TickBroadcast.class, broadcast -> {
            if(SensorsCounter.getInstance().getNumOfCameraSensors() < SensorsCounter.getInstance().getNumOfLIDarSensors() && StatisticalFolder.getInstance().getNumTrackedObjects()==StatisticalFolder.getInstance().getNumDetectedObjects())
                SensorsCounter.getInstance().decrementLIDarSensors();
        });

        // Subscribe to TerminatedBroadcast
        subscribeBroadcast(TerminatedBroadcast.class, broadcast -> {
            StatisticalFolder sfoutput = StatisticalFolder.getInstance();
            //OutputFile outputf = new OutputFile(sfoutput);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try(FileWriter writer = new FileWriter(fs.getBasePath() + "output_file.json")){
                gson.toJson(StatisticalFolder.getInstance(),writer);
            }
            catch (IOException e){
            }
            sendBroadcast(new TerminatedBroadcast());
            terminate();
        });

        // Subscribe to CrashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class, broadcast -> {
            ErrorObject.getInstance().setLandMarks(fs.getLandmarks());
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try(FileWriter writer = new FileWriter(fs.getBasePath() + "OutputError.json")){
                gson.toJson(ErrorObject.getInstance(),writer);
            }
            catch (IOException e){
                e.printStackTrace();
            }
            terminate();
            // Additional handling for crashes if needed
        });
    }
}
