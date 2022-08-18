package kas.helvar;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;

import static kas.helvar.HelvarPointsMap.HELVAR_POINTS_MAP;

public enum HelvarReceivedObjectList {
    HELVAR_RECEIVED_OBJECT_LIST;

    private final Map<String, LinkedBlockingDeque<String>> objectMap;

    HelvarReceivedObjectList() {
        this.objectMap = new HashMap<>();
    }

    public void addValueToTheEnd(String host, String in) {
        LinkedBlockingDeque<String> objectList = objectMap.get(host);
        if (objectList == null) {
            objectList = new LinkedBlockingDeque<>();
            objectMap.put(host, objectList);
        }
        objectList.add(in);
    }

    public String poolFirst(String host) throws InterruptedException {
        LinkedBlockingDeque<String> objectList = objectMap.get(host);
        if (objectList == null) {
            return null;
        }
        return objectList.takeFirst();
    }

    public void processing(String host) throws InterruptedException {
        String receivedObject = HELVAR_RECEIVED_OBJECT_LIST.poolFirst(host);
        if (receivedObject != null) {
            ReceivedObjectProcessor.processing(host, receivedObject, HELVAR_POINTS_MAP);
        }
    }
}
