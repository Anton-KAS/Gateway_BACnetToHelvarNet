package kas.helvar;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public enum HelvarReceivedObjectList {
    HELVAR_RECEIVED_OBJECT_LIST;

    private final long start;
    private Map<String, LinkedList<BufferedReader>> objectMap;

    HelvarReceivedObjectList() {
        this.start = System.currentTimeMillis();
        this.objectMap = new HashMap<>();
    }

    public void addValueInTheEnd(String host, BufferedReader in) throws IOException {
        LinkedList<BufferedReader> objectList = objectMap.get(host);
        if (objectList == null) {
            objectList = new LinkedList<>();
        }
        objectList.addLast(in);
        objectMap.put(host, objectList);
    }

    public BufferedReader poolFirst(String host) {
        LinkedList<BufferedReader> objectList = objectMap.get(host);
        if (objectList == null) {
            return null;
        }
        return objectList.pollFirst();
    }

    @Override
    public String toString() {
        return "Создан в " + start;
    }
}

