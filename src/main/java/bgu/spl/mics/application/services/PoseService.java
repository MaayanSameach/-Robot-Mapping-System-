package bgu.spl.mics.application.services;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.ErrorObject;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.MicroService;

/**
 * PoseService is responsible for maintaining the robot's current pose (position and orientation)
 * and broadcasting PoseEvents at every tick.
 */
public class PoseService extends MicroService {
    private GPSIMU gpsimu;
    /**
     * Constructor for PoseService.
     *
     * @param gpsimu The GPSIMU object that provides the robot's pose data.
     */
    public PoseService(GPSIMU gpsimu) {
        super("PoseService");
        this.gpsimu = gpsimu;
    }
    /**
     * Initializes the PoseService.
     * Subscribes to TickBroadcast and sends PoseEvents at every tick based on the current pose.
     */
    @Override
    protected synchronized void initialize() {
        // Subscribe to TickBroadcast
        subscribeBroadcast(TickBroadcast.class, tick -> {
            // Fetch the current pose
            Pose currentPose = gpsimu.getPose(tick.getCurrentTick());
            if (currentPose != null) {
                // Send a PoseEvent with the current pose
                ErrorObject.getInstance().addPose(currentPose);
                sendEvent(new PoseEvent(currentPose));
            } else {
                sendBroadcast(new TerminatedBroadcast());
                terminate();
            }
        });
        // Handle termination
        subscribeBroadcast(TerminatedBroadcast.class, this::handleTermination);
        subscribeBroadcast(CrashedBroadcast.class, crashed -> terminate());
    }
    private void handleTermination(TerminatedBroadcast broadcast) {
        terminate(); // Call terminate to exit the run loop
    }
}
