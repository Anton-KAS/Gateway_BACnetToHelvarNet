package kas.helvar;

import kas.bacnet.BacnetReceivedObject;
import kas.bacnet.BacnetReceivedObjectList;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//import static kas.bacnet.BacnetReceivedObjectList.BACNET_RECEIVED_OBJECT_LIST;
import static kas.helvar.HelvarPointsMap.HELVAR_POINTS_MAP;

public class ValuesFromBacnetProcessor implements Runnable {
    private final Logger logger;

    private volatile Map<String, HelvarControllerListener> listenerMap;
    private volatile BacnetReceivedObjectList bacnetReceivedObjectList;

    public ValuesFromBacnetProcessor(BacnetReceivedObjectList bacnetReceivedObjectList) {
        this.logger = Logger.getLogger(ValuesFromBacnetProcessor.class);

        this.bacnetReceivedObjectList = bacnetReceivedObjectList;
        listenerMap = new ConcurrentHashMap<>();
    }

    private @Nullable String queryProcessor(@NotNull BacnetReceivedObject bacnetReceivedObject, @NotNull HelvarPoint helvarPoint) {
        String type = bacnetReceivedObject.getType();
        int value = bacnetReceivedObject.getValue();
        String query;
        switch (type) {
            case "av":
                query = helvarPoint.getRecallSceneQuery(value);
                break;
            case "ao":
                query = helvarPoint.getDirectLevelQuery(value);
                break;
            default:
                query = null;
        }
        return query;
    }
    public void addListener(String host, HelvarControllerListener listener) {
        listenerMap.put(host, listener);
    }

    @Override
    public void run() {
        while (true) {
            try {
                //BacnetReceivedObject bacnetReceivedObject = BACNET_RECEIVED_OBJECT_LIST.getFirst();
                BacnetReceivedObject bacnetReceivedObject = bacnetReceivedObjectList.getFirst();

                int group = bacnetReceivedObject.getObjectId();
                HelvarPoint helvarPoint = HELVAR_POINTS_MAP.getPointByGroup(group);

                String query = null;
                if (helvarPoint != null) {
                    query = queryProcessor(bacnetReceivedObject, helvarPoint);
                }
                if (query != null) {
                    HelvarControllerListener listener = listenerMap.get(helvarPoint.getHost());
                    if (listener != null) {
                        listener.setBacnetSendMessage(query);
                    } else {
                        logger.error("Skip send message: " + query + "\tto: " + helvarPoint.getHost());
                    }
                }
            } catch (Exception e) {
                logger.error("run() - " + e);
                logger.error("run() - " + e.getMessage());
                logger.error("run() - " + Arrays.toString(e.getStackTrace()));
            }
        }
    }
}
