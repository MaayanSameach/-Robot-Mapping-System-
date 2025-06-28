package bgu.spl.mics.application.objects;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */
public class GPSIMU {
    private int currentTick;
    private STATUS status;
    private List<Pose> poseList;

    public GPSIMU(String filePath) {
        this.currentTick = 0;
        this.status = STATUS.DOWN;
        this.poseList = new ArrayList<>();
        loadData(filePath);
    }

    //auxillary function to read all poses from json
    private void loadData(String filePath) {
        Gson gson = new Gson();
        try(FileReader reader = new FileReader(filePath)){
            Type poseListType = new TypeToken<List<Pose>>(){}.getType();
            poseList = gson.fromJson(reader, poseListType);
        } catch (IOException e) {
        }

    }

    public int getCurrentTick() {
        return currentTick;
    }

    public void setCurrentTick(int currentTick) {
        this.currentTick = currentTick;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public List<Pose> getPoseList() {
        return poseList;
    }

    public Pose getPose(int time) {
        for(Pose p:poseList){
            if(p.getTime()==time)
                return p;
        }
        return null;
    }

    public void addPose(Pose pose) {
        poseList.add(pose);
    }
}
