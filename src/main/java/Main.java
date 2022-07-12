import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.exception.BACnetErrorException;
import kas.bacnet.BacnetLocalDevice;
import kas.bacnet.ConfigLoader;
import kas.excel.ExcelParser;
import kas.helvar.*;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static kas.helvar.HelvarPointsMap.HELVAR_POINTS_MAP;
import static kas.helvar.ValuesToBacnet.VALUES_TO_BACNET;

public class Main {
    static final Logger logger = Logger.getLogger(Main.class);
    static Map<Runnable, Thread> threadList;

    public static void main(String[] args) {
        logger.info("Main: Start program");

        ExcelParser excelParser = new ExcelParser();
        JSONObject jsonConfigData = excelParser.parseXlsxToJson();

        HELVAR_POINTS_MAP.addPointsFromJson(jsonConfigData);

        BacnetLocalDevice bacnetDevice = new BacnetLocalDevice(new ConfigLoader().getConfig());
        logger.info("Created BACnet bacnetDevice");

        bacnetDevice.addPointsFromJson(jsonConfigData);
        logger.info("BACnet addPointsFromJson");

        VALUES_TO_BACNET.setBacnetPointList(bacnetDevice);
        logger.info("BACnet setBacnetPointList");

        threadList = new ConcurrentHashMap<>();

        Thread bacnetDeviceThread = new Thread(bacnetDevice);
        bacnetDeviceThread.setName("BACnet service");
        bacnetDeviceThread.setPriority(Thread.MAX_PRIORITY);
        threadList.put(bacnetDevice, bacnetDeviceThread);

        ValuesFromBacnetProcessor valuesFromBacnetProcessor = new ValuesFromBacnetProcessor(bacnetDevice.getBacnetReceivedObjectList());
        Thread valuesFromBacnetProcessorThread = new Thread(valuesFromBacnetProcessor);
        valuesFromBacnetProcessorThread.setPriority(Thread.MAX_PRIORITY);
        valuesFromBacnetProcessorThread.setName("valuesFromBacnetProcessor");
        threadList.put(valuesFromBacnetProcessor, valuesFromBacnetProcessorThread);


        Set<Object> objects = jsonConfigData.keySet();
        objects.parallelStream().forEach((o) -> {
            //ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

            logger.info("startCyrcleJobs loop by jsonConfigData: " + o.toString());
            String key = (String) o;
            JSONObject controller = (JSONObject) jsonConfigData.get(key);

            String host = (String) controller.get("IP_CONTROLLER");
            int port = (int) controller.get("PORT_CONTROLLER");
            int controllerReg = (int) controller.get("CONTROLLER_REGISTER");

            try {
                Socket socket = new Socket(host, port);
                logger.info("new Socket " + host + ":" + port);

                logger.info("new Helvar Controller Listener " + host + ":" + port);
                HelvarControllerListener helvarControllerlistener = new HelvarControllerListener(host, port, socket, controllerReg);
                Thread helvarControllerListenerThread = new Thread(helvarControllerlistener);
                helvarControllerListenerThread.setName("helvarControllerListenerThread " + host);
                threadList.put(helvarControllerlistener, helvarControllerListenerThread);
                helvarControllerListenerThread.start();

                logger.info("new valuesToBacnetProcessor " + host);
                ValuesToBacnetProcessor valuesToBacnetProcessor = new ValuesToBacnetProcessor(host);
                Thread valuesToBacnetProcessorThread = new Thread(valuesToBacnetProcessor);
                valuesToBacnetProcessorThread.setName("valuesToBacnetProcessor " + host);
                threadList.put(valuesToBacnetProcessor, valuesToBacnetProcessorThread);
                valuesToBacnetProcessorThread.start();

                logger.info("new cyrcleJobReadPool " + host);
                CyrcleJobReadPool cyrcleJobReadPool = new CyrcleJobReadPool(host, helvarControllerlistener);
                Thread cyrcleJobReadPoolThread = new Thread(cyrcleJobReadPool);
                cyrcleJobReadPoolThread.setName("cyrcleJobReadPool " + host);
                cyrcleJobReadPoolThread.start();
                //scheduledExecutorService.scheduleWithFixedDelay(cyrcleJobReadPool, 0, 5, TimeUnit.SECONDS);

                valuesFromBacnetProcessor.addListener(host, helvarControllerlistener);

            } catch (IOException e) {
                logger.error("Main: " + e);
            }
        });

        //Thread.sleep(5000);
        valuesFromBacnetProcessorThread.start();
        bacnetDeviceThread.start();

        /*
        while (true) {
            if (bacnetDevice.isRunning()) {
                try {
                    Thread.sleep(500);
                    bacnetDevice.sendWhoIsRequestMessage();
                    Thread.sleep(1000);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    logger.error(e.getStackTrace());
                }
            } else {
                Thread.sleep(1000);
            }
        }

         */
    }
}
