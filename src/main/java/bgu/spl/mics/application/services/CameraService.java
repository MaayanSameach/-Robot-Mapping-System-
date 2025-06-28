package bgu.spl.mics.application.services;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.example.services.ExampleBroadcastListenerService;

import java.util.LinkedList;
import java.util.List;
/**
 * CameraService is responsible for processing data from the camera and
 * sending DetectObjectsEvents to LiDAR workers.
 * 
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService {

    private Camera camera;
    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera) {
        super("CameraService");
        this.camera = camera;
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectObjectsEvents.
     */
    @Override
    protected synchronized void initialize() {
        // Subscribe to TickBroadcast
        StatisticalFolder sf=StatisticalFolder.getInstance();
        subscribeBroadcast(TickBroadcast.class, tick -> {
            // Check if the camera is operational and if it should send data at this tick
            if (camera.checkIfLeft(tick.getCurrentTick())) {
                List<DetectedObject> detectedObjects = camera.getDetectedObjects(tick.getCurrentTick());
                // Create and send DetectObjectsEvent
                if (!detectedObjects.isEmpty()) {
                    StampedDetectedObjects lastFrame = new StampedDetectedObjects(tick.getCurrentTick() - camera.getFrequency(), new LinkedList<DetectedObject>());
                    boolean error = false;
                    for(DetectedObject detectedObject:detectedObjects){
                        if(detectedObject.getId().equals("ERROR")) {
                            error = true;
                            camera.setStatus(STATUS.ERROR);
                            ErrorObject.getInstance().setErrorString(detectedObject.getDescription());
                            ErrorObject.getInstance().setFaultySensor("Camera" + camera.getId());
                            sendBroadcast(new CrashedBroadcast("camera"+camera.getId(), detectedObject.getDescription()));
                            break;
                        } else{
                            lastFrame.getDetectedObjects().add(detectedObject);
                            sf.incrementDetectedObjects();
                        }
                    }
                    if (!error){
                        if (!lastFrame.getDetectedObjects().isEmpty())
                            ErrorObject.getInstance().updateLastCamerasFrame("Camera" + camera.getId(),lastFrame);
                        sendEvent(new DetectObjectsEvent(detectedObjects, tick.getCurrentTick() - camera.getFrequency()));
                    }

                }
            } else {
                SensorsCounter.getInstance().decrementCameraSensors();
                this.camera.setStatus(STATUS.DOWN);
                terminate();
            }
        });
        // Handle termination
        subscribeBroadcast(TerminatedBroadcast.class, this::handleTermination);
        //handle crashed
        subscribeBroadcast(CrashedBroadcast.class, crashed -> terminate());
    }
    private void handleTermination(TerminatedBroadcast broadcast) {
        terminate(); // Call terminate to exit the run loop
    }
}
