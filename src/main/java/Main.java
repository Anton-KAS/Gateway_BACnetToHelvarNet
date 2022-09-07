import kas.bacnet.BacnetLocalDevice;
import kas.bacnet.ConfigLoader;
import kas.excel.ExcelParser;
import kas.helvar.*;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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

            logger.info("startCircleJobs loop by jsonConfigData: " + o.toString());
            String key = (String) o;
            JSONObject controller = (JSONObject) jsonConfigData.get(key);

            String host = (String) controller.get("IP_CONTROLLER");
            int port = (int) controller.get("PORT_CONTROLLER");
            int controllerReg = (int) controller.get("CONTROLLER_REGISTER");

            valuesFromBacnetProcessorThread.start();
            logger.info("valuesFromBacnetProcessorThread running");

            try {

                logger.info("new Helvar Controller Listener " + host + ":" + port);
                HelvarControllerListener helvarControllerlistener = new HelvarControllerListener(host, port, controllerReg);
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

                logger.info("new circleJobReadPool " + host);
                CircleJobReadPool circleJobReadPool = new CircleJobReadPool(host, helvarControllerlistener);
                Thread circleJobReadPoolThread = new Thread(circleJobReadPool);
                circleJobReadPoolThread.setName("circleJobReadPool " + host);
                circleJobReadPoolThread.start();

                valuesFromBacnetProcessor.addListener(host, helvarControllerlistener);

            } catch (IOException e) {
                logger.error("Main: " + e);
            }
        });

        bacnetDeviceThread.start();
    }
}
