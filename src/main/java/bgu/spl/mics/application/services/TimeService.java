package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.SensorsCounter;
import bgu.spl.mics.application.objects.StatisticalFolder;
import  bgu.spl.mics.application.messages.CrashedBroadcast;
import  bgu.spl.mics.application.messages.TerminatedBroadcast;


/**
 * TimeService acts as the global timer for the system, broadcasting TickBroadcast messages
 * at regular intervals and controlling the simulation's duration.
 */
public class TimeService extends MicroService {

    private int tickTime; // Duration of each tick in milliseconds
    private int duration; // Total number of ticks
    private int currentTick; // Tracks the current tick

    /**
     * Constructor for TimeService.
     *
     * @param TickTime  The duration of each tick in milliseconds.
     * @param Duration  The total number of ticks before the service terminates.
     */
    public TimeService(int TickTime, int Duration) {
        super("TimeService");
        this.tickTime = TickTime;
        this.duration = Duration;
        this.currentTick = 0;
    }
    /**
     * Initializes the TimeService.
     * Starts broadcasting TickBroadcast messages and terminates after the specified duration.
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, (TickBroadcast c) -> {
            try{
                Thread.sleep(tickTime*1000);
                if (duration > currentTick) {
                    currentTick++;
                    StatisticalFolder.getInstance().incrementRuntime();
                    sendBroadcast(new TickBroadcast(currentTick));
                } else {
                    sendBroadcast(new TerminatedBroadcast());
                    terminate();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                sendBroadcast(new TerminatedBroadcast());
                terminate();
            }

        });

        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast b) -> {
                terminate();
        });

        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast b) -> {
            terminate();
        });


        // The first tick.
        currentTick++;
        sendBroadcast(new TickBroadcast(currentTick));
        StatisticalFolder.getInstance().incrementRuntime();
    }
}
