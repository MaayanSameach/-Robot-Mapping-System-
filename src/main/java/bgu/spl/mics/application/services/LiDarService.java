package bgu.spl.mics.application.services;
import bgu.spl.mics.application.objects.*;

import java.util.*;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.PendingQueue;


/**
 * LiDarService is responsible for processing data from the LiDAR sensor and
 * sending TrackedObjectsEvents to the FusionSLAM service.
 *
 * This service interacts with the LiDarWorkerTracker object to retrieve and process
 * cloud point data and updates the system's StatisticalFolder upon sending its
 * observations.
 */
public class LiDarService extends MicroService {
    private final LiDarWorkerTracker lwt; // Assumes lwt is responsible for object tracking
    private final int lidarFrequency; // Frequency in ticks
//    private final Queue<StampedDetectedObjects> pendingEvents = new LinkedList<>();
    private int currentTick = 0; // Tracks the current tick
    private List<List<TrackedObject>> trackedObjectsList;

    public LiDarService(String name, LiDarWorkerTracker lwt, int lidarFrequency) {
        super("LiDarTrackerWorker" + lwt.getId());
        this.lwt = lwt;
        this.lidarFrequency = lidarFrequency;
        this.trackedObjectsList = new ArrayList<List<TrackedObject>>();
    }

    @Override
    protected synchronized void initialize() {
        subscribeEvent(DetectObjectsEvent.class, (DetectObjectsEvent e) -> {
            List<TrackedObject> trackedObjects = lwt.createTrackedObjects(e.getDetectedObjects());
            if (trackedObjects != null) {
                trackedObjectsList.add(trackedObjects);
                //ErrorObject.getInstance().updateLastLiDarWorkerTrackersFrame("LiDar"+lwt.getId(), trackedObjects);
            }
            else{
                lwt.setStatus(STATUS.ERROR);
                ErrorObject.getInstance().setFaultySensor("LiDar" + lwt.getId());
                ErrorObject.getInstance().setErrorString("Connection to LiDAR lost");
                sendBroadcast(new CrashedBroadcast("LiDar"+lwt.getId(), "The LiDar sensor disconnected"));
                terminate();
            }
        });

        subscribeBroadcast(TickBroadcast.class, (TickBroadcast c) -> {
            currentTick = c.getCurrentTick();
            //If there are active cameras or tracked objects left to send:
            if((SensorsCounter.getInstance().getNumOfCameraSensors() != 0) || !trackedObjectsList.isEmpty()){
                Iterator<List<TrackedObject>> iterator = trackedObjectsList.iterator();
                while (iterator.hasNext()) {
                    List<TrackedObject> list = iterator.next();
                    if (!list.isEmpty() && currentTick >= (lwt.getFrequency() + list.get(0).getTime())) {
                        for (int i = 0; i < list.size(); i++)
                            StatisticalFolder.getInstance().incrementTrackedObjects();
                        lwt.setLastTrackedObjects(list); //double check if needed (no usage for get)!!!!!!!!!!!!
                        ErrorObject.getInstance().updateLastLiDarWorkerTrackersFrame(getName(), lwt.getLastTrackedObjects());
                        sendEvent(new TrackedObjectEvent(list));
                        iterator.remove();
                    }
                }

                // If we sent the last tracked object in the current tick.
                if(SensorsCounter.getInstance().getNumOfCameraSensors() == 0 && trackedObjectsList.isEmpty()){
                    sendBroadcast(new TerminatedBroadcast());
                    SensorsCounter.getInstance().decrementLIDarSensors();
                    this.lwt.setStatus(STATUS.DOWN);
                    terminate();
                }
            }

            //The lidar finished its work (there are no more cameras and no more tracked objects to send)
            else{
                sendBroadcast(new TerminatedBroadcast());
                SensorsCounter.getInstance().decrementLIDarSensors();
                this.lwt.setStatus(STATUS.DOWN);
                terminate();
            }

        });
        // Handle termination
        subscribeBroadcast(TerminatedBroadcast.class, terminated -> terminate());
        subscribeBroadcast(CrashedBroadcast.class, crashed -> terminate());
    }

    private synchronized void processPendingEvents() {
        StatisticalFolder sf=StatisticalFolder.getInstance();
        while (!PendingQueue.getInstance().isEmpty()) {
            StampedDetectedObjects pending = PendingQueue.getInstance().peek();
            if (currentTick >= pending.getTime() + lidarFrequency) {
                List<TrackedObject> trackedObjects = lwt.processDetectedObjects(PendingQueue.getInstance().getEvent().getDetectedObjects(), currentTick);
                if(!trackedObjects.isEmpty()) {
                    boolean error = false;
                    List<TrackedObject> lastFrame = new LinkedList<>();
                    for (TrackedObject trackedObject : trackedObjects) {
                        if (trackedObject.getId().equals("ERROR")){
                            error = true;
                            lwt.setStatus(STATUS.ERROR);
                            ErrorObject.getInstance().setErrorString(trackedObject.getDescription());
                            ErrorObject.getInstance().setFaultySensor("LiDarWorkerTracker" + lwt.getId());
                            sendBroadcast(new CrashedBroadcast("LiDarWorkerTracker"+lwt.getId(), trackedObject.getDescription()));
                            break;
                        }
                        else {
                            lastFrame.add(trackedObject);
                            sf.incrementTrackedObjects();
                        }
                    }
                    if (!error){
                        if (!lastFrame.isEmpty())
                            ErrorObject.getInstance().updateLastLiDarWorkerTrackersFrame("LiDarWorkerTracker" + lwt.getId(),lastFrame);
                        sendEvent(new TrackedObjectEvent(trackedObjects));                    }
                }

            } else {
                // Stop processing if conditions aren't met
                break;
            }
            if(PendingQueue.getInstance().isEmpty() && SensorsCounter.getInstance().getNumOfCameraSensors() < SensorsCounter.getInstance().getNumOfLIDarSensors()){
                sendBroadcast(new TerminatedBroadcast());
                SensorsCounter.getInstance().decrementLIDarSensors();
                lwt.setStatus(STATUS.DOWN);
                terminate();
            }
        }
    }

    private void handleTermination(TerminatedBroadcast broadcast) {
        terminate(); // Call terminate to exit the run loop
    }
}
