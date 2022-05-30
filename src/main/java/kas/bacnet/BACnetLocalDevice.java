package kas.bacnet;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.event.DeviceEventListener;
import com.serotonin.bacnet4j.npdu.Network;
import com.serotonin.bacnet4j.npdu.ip.IpNetworkBuilder;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.type.enumerated.EngineeringUnits;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class BACnetLocalDevice implements Runnable {
    private final String APPLICATION_SOFTWARE_VERSION = "0.0.1";
    private final int DEVICE_ID = 777;
    private final int VENDOR_ID = 13;
    private final String VENDOR_NAME = "KAS";

    private final String MODEL_NAME = "Helvar Gateway";
    private final String OBJECT_NAME = "Helvar Gateway " + DEVICE_ID;
    private final String LOCATION = "BMS server";
    private final String DESCRIPTION = "Gateway Helvar.net to BACnet TCP/IP";

    private final String BROADCAST_IP = "192.168.1.255";
    private final String SUBNET_IP = "192.168.1.0";
    private final String LOCAL_IP = "192.168.1.7";
    private final int NETWORK_LENGTH = 24;

    private Network network;
    private DefaultTransport transport;
    private LocalDevice localDevice;
    private DeviceEventListener deviceEventListener;
    private Listener listener;

    private Map<Integer, Point> pointMap = new HashMap<>();

    public BACnetLocalDevice() {
        network = getNetwork();
        transport = new DefaultTransport(network);
        localDevice = getLocalDevice();
        listener = new Listener();
        localDevice.getEventHandler().addListener(listener);
    }

    private Network getNetwork() {
        return new IpNetworkBuilder()
                .withBroadcast(BROADCAST_IP, NETWORK_LENGTH)
                .withSubnet(SUBNET_IP, NETWORK_LENGTH)
                .withLocalBindAddress(LOCAL_IP)
                .build();
    }

    private LocalDevice getLocalDevice() {
        return new LocalDevice(DEVICE_ID, transport)
                .writePropertyInternal(PropertyIdentifier.vendorIdentifier, new UnsignedInteger(VENDOR_ID))
                .writePropertyInternal(PropertyIdentifier.vendorName, new CharacterString(VENDOR_NAME))
                .writePropertyInternal(PropertyIdentifier.modelName, new CharacterString(MODEL_NAME))
                .writePropertyInternal(PropertyIdentifier.objectName, new CharacterString(OBJECT_NAME))
                .writePropertyInternal(PropertyIdentifier.location, new CharacterString(LOCATION))
                .writePropertyInternal(PropertyIdentifier.description, new CharacterString(DESCRIPTION))
                .writePropertyInternal(PropertyIdentifier.applicationSoftwareVersion, new CharacterString(APPLICATION_SOFTWARE_VERSION));
    }

    public boolean addPointsFromJson(JSONObject json) {
        if (json == null) return false;
        for (Object o : json.keySet()) {
            String key = (String) o;
            JSONObject controller = (JSONObject) json.get(key);
            JSONObject points = (JSONObject) controller.get("Points");
            for (Object p : points.keySet()) {
                String pKey = (String) p;
                JSONObject point = (JSONObject) points.get(pKey);
                long longInstanceNumber = (long) point.get("HELVAR_GROUP");
                int instanceNumber = (int) longInstanceNumber;

                String description = String.format("%s / %s / %s / %s",
                        controller.get("LIGHT_PANEL"),
                        point.get("ROOM"),
                        point.get("ELECTRIC_GROUP"),
                        instanceNumber
                );

                float presentValue = 0F; // TODO: Перед началом, добавить в JSON стартовые значения

                addAnalogValue(instanceNumber, presentValue, description);
                if ((boolean) point.get("DIMMING")) {
                    addAnalogInput(instanceNumber, presentValue, description);
                    addAnalogOutput(instanceNumber, presentValue, description);
                }
            }
        }
        return true;
    }

    public void addAnalogInput(int instanceNumber, float presentValue, String description) {
        String name = String.format("Group %s consumption", instanceNumber);
        boolean outOfService = false;
        EngineeringUnits units = EngineeringUnits.noUnits;
        AnalogInput ai = new AnalogInput(localDevice, instanceNumber, name, outOfService, units, description, presentValue);
        pointMap.put(instanceNumber, ai.create());
    }

    public void addAnalogValue(int instanceNumber, float presentValue, String description) {
        String name = String.format("Group %s scene", instanceNumber);
        boolean outOfService = false;
        EngineeringUnits units = EngineeringUnits.noUnits;
        AnalogValue av = new AnalogValue(localDevice, instanceNumber, name, outOfService, units, description, presentValue);
        pointMap.put(instanceNumber, av.create());
    }

    public void addAnalogOutput(int instanceNumber, float presentValue, String description) {
        String name = String.format("Group %s direct level", instanceNumber);
        boolean outOfService = false;
        EngineeringUnits units = EngineeringUnits.noUnits;
        AnalogOutput ao = new AnalogOutput(localDevice, instanceNumber, name, outOfService, units, description, presentValue, presentValue);
        pointMap.put(instanceNumber, ao.create());
    }

    @Override
    public void run() {
        try {
            localDevice.initialize();
        } catch (Exception e) {
            e.printStackTrace();
            localDevice.terminate();
        }
    }
}
