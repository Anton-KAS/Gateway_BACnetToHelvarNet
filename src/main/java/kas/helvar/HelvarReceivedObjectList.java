package kas.helvar;

import kas.excel.ExcelParser;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static kas.helvar.HelvarPointsMap.HELVAR_POINTS_MAP;

public enum HelvarReceivedObjectList {
    HELVAR_RECEIVED_OBJECT_LIST;

    private final Logger logger;

    private final long start;
    private final Map<String, LinkedList<String>> objectMap;
    private int currentSize;

    HelvarReceivedObjectList() {
        this.logger = Logger.getLogger(ExcelParser.class);
        this.start = System.currentTimeMillis();
        this.objectMap = new HashMap<>();
    }

    public void addValueInTheEnd(String host, String in) throws IOException {
        //logger.info("HELVAR_RECEIVED_OBJECT_LIST addValueInTheEnd: " + host + " | DATA: " + in);
        LinkedList<String> objectList = objectMap.get(host);
        if (objectList == null) {
            objectList = new LinkedList<>();
        }
        objectList.addLast(in);
        objectMap.put(host, objectList);
    }

    public String poolFirst(String host) {
        LinkedList<String> objectList = objectMap.get(host);
        if (objectList == null) {
            return null;
        }
        String message = objectList.pollFirst();
//        if (message != null) {
//            //logger.info("HELVAR_RECEIVED_OBJECT_LIST poolFirst " + host);
//        }
        return message;
    }

    public void sizeChanged() {
        int newSize = objectMap.size();
        if (newSize != currentSize) {
            //logger.info("HELVAR_RECEIVED_OBJECT_LIST: size changed from " + currentSize + " to " + newSize);
            currentSize = newSize;
        }
    }

    public void processing(String host) {
        String receivedObject = HELVAR_RECEIVED_OBJECT_LIST.poolFirst(host);
        if (receivedObject != null) {
            //logger.info("HELVAR_RECEIVED_OBJECT_LIST: processing() get message from Helvar " + host);
            ReceivedObjectProcessor.processing(host, receivedObject, HELVAR_POINTS_MAP);
        }
    }

    @Override
    public String toString() {
        return "Создан в " + start;
    }
}

