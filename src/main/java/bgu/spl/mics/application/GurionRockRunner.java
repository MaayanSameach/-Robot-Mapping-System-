package bgu.spl.mics.application;
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.CameraService;
import bgu.spl.mics.application.services.FusionSlamService;
import bgu.spl.mics.application.services.PoseService;
import bgu.spl.mics.application.services.TimeService;
import com.google.gson.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.CountDownLatch;
import bgu.spl.mics.application.services.LiDarService;

/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 * <p>
 * This class initializes the system and starts the simulation by setting up
 * services, objects, and configurations.
 * </p>
 */
public class GurionRockRunner {
    /**
     * The main method of the simulation.
     * This method sets up the necessary components, parses configuration files,
     * initializes services, and starts the simulation.
     *
     * @param args Command-line arguments. The first argument is expected to be the path to the configuration file.
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Please provide the configuration file path as the first argument.");
            return;
        }
        String configPath = args[0];
        String basePath = Paths.get(configPath).getParent().toAbsolutePath().toString() + File.separator;
        List<Thread> serviceThreads = new ArrayList<>();
        try {
            // Parse configuration file
            Gson gson = new Gson();
            JsonObject config = gson.fromJson(new FileReader(configPath), JsonObject.class);

            // Extract simulation parameters
            int tickTime =  config.get("TickTime").getAsInt();
            int duration = config.get("Duration").getAsInt();
            String poseJsonPath = basePath + config.get("poseJsonFile").getAsString();
            // Initialize shared resources
            MessageBus messageBus = MessageBusImpl.getInstance();

            // Create services list
            List<MicroService> services = new ArrayList<>();
            // Initialize TimeService (should be started last)
            TimeService timeService = new TimeService(tickTime, duration);

            // Initialize PoseService
            PoseService poseService = new PoseService(new GPSIMU(poseJsonPath));
            services.add(poseService);

            // Initialize FusionSLAM service (should be initialized before sensors)
            FusionSlam.getInstance().setBasePath(basePath);
            FusionSlamService fusionSlam = new FusionSlamService(FusionSlam.getInstance());
            services.add(fusionSlam);

            // Initialize Cameras
            JsonObject camerasConfig = config.getAsJsonObject("Cameras");
            JsonArray cameraConfigurations = camerasConfig.getAsJsonArray("CamerasConfigurations");
            String cameraDataPath = basePath + camerasConfig.get("camera_datas_path").getAsString();

            for (JsonElement element : cameraConfigurations) {
                JsonObject cameraConfig = element.getAsJsonObject();
                int id = cameraConfig.get("id").getAsInt();
                int frequency = cameraConfig.get("frequency").getAsInt();
                String cameraKey = cameraConfig.get("camera_key").getAsString();

                CameraService camera = new CameraService(new Camera(id, frequency, STATUS.UP, cameraDataPath));
                services.add(camera);
            }

            /// Initialize LiDAR workers
            JsonObject lidarConfig = config.getAsJsonObject("LiDarWorkers");
            JsonArray lidarConfigurations = lidarConfig.getAsJsonArray("LidarConfigurations");
            String lidarDataPath = basePath + lidarConfig.get("lidars_data_path").getAsString();
            //Initialize LiDAR database
            LiDarDataBase ds = LiDarDataBase.getInstance(lidarDataPath);

            for (JsonElement element : lidarConfigurations) {
                JsonObject workerConfig = element.getAsJsonObject();
                int id = workerConfig.get("id").getAsInt();
                int frequency = workerConfig.get("frequency").getAsInt();

                LiDarWorkerTracker tracker = new LiDarWorkerTracker(id, frequency, STATUS.UP, new ArrayList<>());
                LiDarService lidarWorker = new LiDarService("LidarService: "+id, tracker, frequency);
                services.add(lidarWorker);
            }

            // Create CountDownLatch for synchronizing service startup
            CountDownLatch initLatch = new CountDownLatch(services.size());

            // Start all services
            for (MicroService service : services) {
                Thread serviceThread = new Thread(() -> {
                    try {
                        initLatch.countDown();
                        initLatch.await(); // Wait for all services to be ready
                        service.run();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
                serviceThreads.add(serviceThread);
                serviceThread.start();
            }
            //init timeService last
            Thread serviceThread = new Thread(() -> {
                timeService.run();
            });
            serviceThreads.add(serviceThread);
            serviceThread.start();

            // Wait for all threads to complete
            for (Thread thread : serviceThreads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

        } catch (IOException e) {
        }
    }
}