package bgu.spl.mics.application.objects;

import java.util.concurrent.atomic.AtomicInteger;

// This class is a singleton that will count the number of services active at each given time.
public class SensorsCounter {
    private AtomicInteger cameraSensors;
    private AtomicInteger liDarSensors;
    private AtomicInteger poseSensors;

    private static class SingletonHolder {
        private static final SensorsCounter instance = new SensorsCounter();
    }

    private SensorsCounter() {
        this.cameraSensors = new AtomicInteger(0);
        this.liDarSensors = new AtomicInteger(0);
        this.poseSensors = new AtomicInteger(0);
    }

    public static SensorsCounter getInstance() {
        return SingletonHolder.instance;
    }

    public void decrementCameraSensors() {
        cameraSensors.decrementAndGet();
    }

    public void decrementLIDarSensors() {
        liDarSensors.decrementAndGet();
    }

    public void decrementPoseSensors() {
        poseSensors.decrementAndGet();
    }

    public void increaseCameraSensors() {
        cameraSensors.incrementAndGet();
    }

    public void increaseLIDarSensors() {
        liDarSensors.incrementAndGet();
    }

    public void increasePoseSensors() {
        poseSensors.incrementAndGet();
    }

    public int getNumOfCameraSensors() {
        return cameraSensors.get();
    }

    public int getNumOfLIDarSensors() {
        return liDarSensors.get();
    }

    public int getNumOfPoseSensors() {
        return poseSensors.get();
    }

    public void setCameraServices(int num) {
        cameraSensors.set(num);
    }

    public void setLIDarServices(int num) {
        liDarSensors.set(num);
    }

    public void setPoseServices(int num) {
        poseSensors.set(num);
    }

    public boolean areServicesDone(){
        return (getNumOfCameraSensors() == 0 && getNumOfLIDarSensors() == 0 && getNumOfPoseSensors() == 0);
    }
}

