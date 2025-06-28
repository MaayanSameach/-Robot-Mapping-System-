package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Pose;

public class PoseEvent implements Event<Pose> {
    private Pose p;

    public PoseEvent(Pose p) {
        this.p = p;
    }

    public Pose getPose(){
        return p;
    }
}
