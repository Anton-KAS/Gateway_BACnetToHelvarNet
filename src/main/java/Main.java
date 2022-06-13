import kas.bacnet.BACnetLocalDevice;
import kas.excel.ExcelParser;
import kas.helvar.*;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static kas.helvar.HelvarPointsMap.HELVAR_POINTS_MAP;
import static kas.helvar.ValuesToBacnet.VALUES_TO_BACNET;

public class Main {
    static final Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        logger.info("Start program");

        ExcelParser excelParser = new ExcelParser();
        JSONObject jsonData = excelParser.parseXlsxToJson();

        HELVAR_POINTS_MAP.addPointsFromJson(jsonData);

        ScheduledExecutorService scheduledExecutorService;
        scheduledExecutorService = Executors.newScheduledThreadPool(1);

        ScheduledExecutorService scheduledExecutorService2;
        scheduledExecutorService2 = Executors.newScheduledThreadPool(1);

        ValuesFromBacnetProcessor valuesFromBacnetProcessor = new ValuesFromBacnetProcessor();

        for (Object o : jsonData.keySet()) {
            logger.info("startCyrcleJobs loop by jsonData: " + o.toString());
            String key = (String) o;
            JSONObject controller = (JSONObject) jsonData.get(key);

            String host = (String) controller.get("IP_CONTROLLER");
            int port = (int) controller.get("PORT_CONTROLLER");

            TempControl tempControl = new TempControl();
            (new Thread(tempControl)).start();

            Socket socket;
            try {
                socket = new Socket(host, port);
                logger.info("new Socket " + host + ":" + port);

                Listener listener = new Listener(host, port, socket);
                logger.info("new Listener " + host + ":" + port);
                (new Thread(listener)).start();

                logger.info("new tempProcessing " + host);
                TempProcessing tempProcessing = new TempProcessing(host);
                //(new Thread(tempProcessing)).start();
                scheduledExecutorService2.scheduleWithFixedDelay(tempProcessing, 0, 50, TimeUnit.MILLISECONDS);

                logger.info("new cyrcleJobReadPool " + host);
                CyrcleJobReadPool cyrcleJobReadPool = new CyrcleJobReadPool(host, listener);
                scheduledExecutorService.scheduleWithFixedDelay(cyrcleJobReadPool, 0, 5, TimeUnit.SECONDS);

                valuesFromBacnetProcessor.addListener(host, listener);

            } catch (IOException e) {
                logger.error(e.toString());
            }
        }

        BACnetLocalDevice localDevice = new BACnetLocalDevice();
        logger.info("Created BACnet localDevice");

        localDevice.addPointsFromJson(jsonData);
        logger.info("BACnet addPointsFromJson");

        VALUES_TO_BACNET.setBacnetPointList(localDevice);
        logger.info("BACnet setBacnetPointList");


        scheduledExecutorService2.scheduleWithFixedDelay(valuesFromBacnetProcessor, 0, 50, TimeUnit.MILLISECONDS);
        //(new Thread(valuesFromBacnetProcessor)).start();

        (new Thread(localDevice)).start();
    }
}
