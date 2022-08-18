package kas.bacnet;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.npdu.ip.IpNetworkBuilder;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.type.enumerated.EngineeringUnits;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.enumerated.Segmentation;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static kas.bacnet.BacnetReceivedObjectList.BACNET_RECEIVED_OBJECT_LIST;

public class BacnetLocalDevice implements Runnable {
    private final Logger logger;

    private final String APPLICATION_SOFTWARE_VERSION = "0.1.1";
    private final String MODEL_NAME = "Helvar Gateway";
    private final String VENDOR_NAME = "KAS";
    private final int VENDOR_ID = 13;

    private final int deviceId;
    private final String objectName;
    private final String location;
    private final String description;

    private final String broadcastIp;
    private final String localIp;
    private final int networkLength;
    private final int localPort;

    private final DefaultTransport transport;
    private final LocalDevice localDevice;

    private final Map<String, Map<Integer, Point>> pointMap = new HashMap<>();
    private final BacnetReceivedObjectList bacnetReceivedObjectList;

    private final IpNetwork ipNetwork;

    final List<RemoteDevice> remoteDevices;

    private final boolean bbmdEnable;
    private String bbmdIp = "0.0.0.0";
    private int bbmdPort = 0;

    public BacnetLocalDevice(Properties BacnetConfig) {
        this.logger = Logger.getLogger(BacnetLocalDevice.class);

        this.deviceId = Integer.parseInt(BacnetConfig.getProperty("device.id"));
        this.location = BacnetConfig.getProperty("location");
        this.broadcastIp = BacnetConfig.getProperty("ip.broadcast");
        this.localIp = BacnetConfig.getProperty("ip.local");
        this.networkLength = Integer.parseInt(BacnetConfig.getProperty("network.length"));
        this.localPort = Integer.parseInt(BacnetConfig.getProperty("network.port"));

        this.objectName = MODEL_NAME + deviceId;
        this.description = VENDOR_ID + " " + VENDOR_NAME + " Gateway Helvar.net to BACnet TCP/IP " + APPLICATION_SOFTWARE_VERSION;

        this.ipNetwork = getNetwork();

        this.bbmdEnable = Boolean.parseBoolean(BacnetConfig.getProperty("bbmd.enable"));
        if (bbmdEnable) {
            ipNetwork.enableBBMD();
            this.bbmdIp = BacnetConfig.getProperty("bbmd.remoteIp");
            this.bbmdPort = Integer.parseInt(BacnetConfig.getProperty("bbmd.remotePort"));
        }

        this.transport = new DefaultTransport(ipNetwork);


        this.localDevice = bacnetLocalDevice();

        this.bacnetReceivedObjectList = BACNET_RECEIVED_OBJECT_LIST;


        this.remoteDevices = new ArrayList<>();

        this.localDevice.getEventHandler().addListener(new Listener(localDevice, remoteDevices, this));
    }

    public BacnetReceivedObjectList getBacnetReceivedObjectList() {
        return bacnetReceivedObjectList;
    }

    private IpNetwork getNetwork() {
        return new IpNetworkBuilder()
                .withBroadcast(broadcastIp, networkLength)
                .withLocalBindAddress(localIp)
                .withPort(localPort)
                .withReuseAddress(true)
                .build();
    }

    private LocalDevice bacnetLocalDevice() {
        return new LocalDevice(deviceId, transport)
                .writePropertyInternal(PropertyIdentifier.vendorIdentifier, new UnsignedInteger(VENDOR_ID))
                .writePropertyInternal(PropertyIdentifier.vendorName, new CharacterString(VENDOR_NAME))
                .writePropertyInternal(PropertyIdentifier.modelName, new CharacterString(MODEL_NAME))
                .writePropertyInternal(PropertyIdentifier.objectName, new CharacterString(objectName))
                .writePropertyInternal(PropertyIdentifier.location, new CharacterString(location))
                .writePropertyInternal(PropertyIdentifier.description, new CharacterString(description))
                .writePropertyInternal(PropertyIdentifier.maxApduLengthAccepted, new UnsignedInteger(1476))
                .writePropertyInternal(PropertyIdentifier.segmentationSupported, Segmentation.segmentedBoth)
                .writePropertyInternal(PropertyIdentifier.applicationSoftwareVersion, new CharacterString(APPLICATION_SOFTWARE_VERSION));
    }

    public boolean addPointsFromJson(JSONObject json) {
        if (json == null) return false;
        for (Object o : json.keySet()) {
            String key = (String) o;
            JSONObject controller = (JSONObject) json.get(key);

            int controllerReg = (int)(long) controller.get("CONTROLLER_REGISTER");
            addBinaryInput(controllerReg, "offline", "Helvar Controller Status " + controller.get("IP_CONTROLLER") + " | " + controller.get("LIGHT_PANEL"));

            JSONObject points = (JSONObject) controller.get("Points");
            for (Object p : points.keySet()) {
                String pKey = (String) p;
                JSONObject point = (JSONObject) points.get(pKey);
                int instanceNumber;

                try {
                    instanceNumber = (int) point.get("HELVAR_GROUP");
                } catch (ClassCastException cce) {
                    long longInstanceNumber = (long) point.get("HELVAR_GROUP");
                    instanceNumber = (int) longInstanceNumber;
                }

                String description = String.format("%s / %s / %s / %s",
                        controller.get("LIGHT_PANEL"),
                        point.get("ROOM"),
                        point.get("ELECTRIC_GROUP"),
                        instanceNumber
                );

                addAnalogValue(instanceNumber, 0F, description);
                if ((boolean) point.get("DIMMING")) {
                    addAnalogInput(instanceNumber, 0F, description);
                    addAnalogOutput(instanceNumber, 0F, description);
                }
            }
        }
        return true;
    }

    public void addAnalogInput(int instanceNumber, float presentValue, String description) {
        String name = String.format("Group %s consumption", instanceNumber);
        AnalogInput ai = new AnalogInput(localDevice, instanceNumber, name, false, EngineeringUnits.noUnits, description, presentValue);
        putNewPointInMap("ai", instanceNumber, ai);
    }

    public void addAnalogValue(int instanceNumber, float presentValue, String description) {
        String name = String.format("Group %s scene", instanceNumber);
        AnalogValue av = new AnalogValue(localDevice, instanceNumber, name, false, EngineeringUnits.noUnits, description, presentValue);
        putNewPointInMap("av", instanceNumber, av);
    }

    public void addAnalogOutput(int instanceNumber, float presentValue, String description) {
        String name = String.format("Group %s direct level", instanceNumber);
        AnalogOutput ao = new AnalogOutput(localDevice, instanceNumber, name, false, EngineeringUnits.noUnits, description, presentValue, presentValue);
        putNewPointInMap("ao", instanceNumber, ao);
    }

    public void addBinaryInput(int instanceNumber, String presentValue, String description) {
        String name = String.format("Controller status %s", instanceNumber);
        BinaryInput bi = new BinaryInput(localDevice, instanceNumber, name, false, EngineeringUnits.noUnits, description, presentValue);
        putNewPointInMap("bi", instanceNumber, bi);
    }

    private void putNewPointInMap(String typeValue, int instanceNumber, Point point) {
        Map<Integer, Point> pointMapByType = pointMap.get(typeValue);
        if (pointMapByType == null) pointMapByType = new HashMap<>();
        pointMapByType.put(instanceNumber, point.create());
        pointMap.put(typeValue, pointMapByType);
    }

    public Map<String, Map<Integer, Point>> getPointMap() {
        return pointMap;
    }

    public boolean registerForeignDevice(String ip, int port) {
        if (bbmdEnable & (port != localPort | !ip.equals(localIp))) {
            try {
                ipNetwork.registerAsForeignDevice(new java.net.InetSocketAddress(ip, port), 100);
                logger.info("BBMD Remote device registered: " + ip + ":" + port);
                return true;
            } catch (Exception e) {
                logger.error(ip + ":" + port + " - " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    @Override
    public void run() {
        try {
            int sec = 10;
            logger.info("BACnet localDevice wait, starts in " + sec + " sec");
            Thread.sleep(TimeUnit.SECONDS.toMillis(sec));
        } catch (InterruptedException e) {
            logger.error(e.toString());
        }
        try {
            logger.info("BACnet localDevice " + localIp + ":" + localPort + " running");
            localDevice.initialize();
            while (true) {
                boolean result = registerForeignDevice(bbmdIp, bbmdPort);
                if (result) {
                    break;
                } else {
                    //noinspection BusyWait
                    Thread.sleep(1000);
                }
            }

        } catch (Exception e) {
            logger.info("BACnet localDevice stopped");
            logger.error("run() " + e.getMessage());
            logger.error("run() " + Arrays.toString(e.getStackTrace()));
            localDevice.terminate();
        }
    }
}
