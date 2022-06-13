package kas.helvar;

import kas.bacnet.BACnetReceivedObject;
import kas.excel.ExcelParser;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static kas.bacnet.BACnetReceivedObjectList.BACNET_RECEIVED_OBJECT_LIST;
import static kas.helvar.HelvarPointsMap.HELVAR_POINTS_MAP;

public class ValuesFromBacnetProcessor implements Runnable {
    private final Logger logger;

    private boolean running;
    private Map<String, Listener> listenerMap;

    public ValuesFromBacnetProcessor() {
        this.logger = Logger.getLogger(ExcelParser.class);

        this.running = false;
        listenerMap = new HashMap<>();
    }

    public void addListener(String host, Listener listener) {
        listenerMap.put(host, listener);
    }

    @Override
    public void run() {
        //logger.info("ValuesFromBacnetProcessor run()");
        running = true;
        //while (running) {
        try {
            BACnetReceivedObject bacnetReceivedObject = BACNET_RECEIVED_OBJECT_LIST.poolFirst();
            if (bacnetReceivedObject == null) {
                return;
                //continue;
            }
            System.out.println("ValuesFromBacnetProcessor get bacnetReceivedObject");
            int group = bacnetReceivedObject.getObjectId();
            System.out.println("ValuesFromBacnetProcessor get group: " + group);
            HelvarPoint helvarPoint = HELVAR_POINTS_MAP.getPointByGroup(group);
            System.out.println("ValuesFromBacnetProcessor get helvarPoint: " + helvarPoint);
            if (helvarPoint == null) {
                return;
                //continue;
            }
            ;
            String type = bacnetReceivedObject.getType();
            System.out.println("ValuesFromBacnetProcessor get type: " + type);
            int value = bacnetReceivedObject.getValue();
            System.out.println("ValuesFromBacnetProcessor get value: " + value);
            String query = null;
            switch (type) {
                case "av":
                    System.out.println("ValuesFromBacnetProcessor switch av");
                    query = helvarPoint.getRecallSceneQuery(value);
                    break;
                case "ao":
                    System.out.println("ValuesFromBacnetProcessor switch ao");
                    query = helvarPoint.getDirectLevelQuery(value);
                    break;
            }
            System.out.println("ValuesFromBacnetProcessor get query: " + query);
            if (query != null) {
                Listener listener = listenerMap.get(helvarPoint.getHost());
                System.out.println("ValuesFromBacnetProcessor get listener");
                listener.setBacnetSendMessage(query);
                System.out.println("ValuesFromBacnetProcessor listener setBacnetSendMessage");
            }
        } catch (Exception e) {
            logger.error("ValuesFromBacnetProcessor run() - " + e.toString());
        }
        //}
    }

    public void stop() {
        running = false;
        logger.info("ValuesFromBacnetProcessor stop()");
    }
}
