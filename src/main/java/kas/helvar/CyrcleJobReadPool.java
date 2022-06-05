package kas.helvar;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

import static kas.helvar.HelvarPointsMap.HELVAR_POINTS_MAP;
import static kas.helvar.HelvarReceivedObjectList.HELVAR_RECEIVED_OBJECT_LIST;

public class CyrcleJobReadPool implements Runnable{
    private final String host;
    private Map<Integer, HelvarPoint> pointsMap;
    private boolean running;

    public CyrcleJobReadPool(String host) {
        this.host = host;
        this.pointsMap = HELVAR_POINTS_MAP.getPointsMapByHost(host);
        this.running = false;
    }

    @Override
    public void run() {

        long totalTime = 10;
        long startTime;
        long endTime;
        long poolTime = 5; // in seconds

        BufferedReader receivedObject = HELVAR_RECEIVED_OBJECT_LIST.poolFirst(host);
        while (true) {
            try {
                System.out.println("Helvar CyrcleJobReadPool running");
                running = true;
                if (receivedObject == null) {
                    if (totalTime < poolTime) {
                        try {
                            wait(poolTime - totalTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    startTime = System.nanoTime();
                    for (HelvarPoint point : pointsMap.values()) {
                        try {
                            point.sendReadSceneValue();
                            point.sendReadConsumption();
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                    }
                    endTime = System.nanoTime();
                    totalTime = (endTime - startTime) / 1000000000;
                } else {
                    try {
                        String receiverMessage = receivedObject.readLine();
                        ReceivedObjectProcessor.processing(host, receiverMessage, HELVAR_POINTS_MAP);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                System.out.println("Helvar CyrcleJobReadPool stopped");
                running = false;
            }
        }
    }
}
