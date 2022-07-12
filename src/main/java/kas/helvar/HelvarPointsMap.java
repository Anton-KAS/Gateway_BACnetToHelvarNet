package kas.helvar;

import kas.excel.DefaultHeader;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static kas.helvar.ValuesToBacnet.VALUES_TO_BACNET;

public enum HelvarPointsMap implements SetValueFromHelvarNet {
    HELVAR_POINTS_MAP;

    private final Map<String, Map<Integer, HelvarPoint>> helvarPointsMap;

    HelvarPointsMap() {
        this.helvarPointsMap = new HashMap<>();
    }

    public boolean addPointsFromJson(JSONObject json) {
        if (json == null) return false;
        for (Object o : json.keySet()) {
            String keyIp = (String) o;
            JSONObject controller = (JSONObject) json.get(keyIp);
            String ipController = (String) controller.get(DefaultHeader.IP_CONTROLLER.toString());
            /*
            int portController;
            try {
                portController = (int) controller.get(DefaultHeader.PORT_CONTROLLER.toString());
            } catch (ClassCastException cce) {
                long longPortController = (long) controller.get(DefaultHeader.PORT_CONTROLLER.toString());
                portController = (int) longPortController;
            }
             */
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

                String description = String.format("Helvar point: %s / %s / %s / %s / %s",
                        keyIp,
                        controller.get("LIGHT_PANEL"),
                        point.get("ROOM"),
                        point.get("ELECTRIC_GROUP"),
                        instanceNumber
                );

                HelvarPoint helvarPoint = new HelvarPoint(ipController, instanceNumber, (boolean) point.get("DIMMING"), description);

                Map<Integer, HelvarPoint> helvarPointMapByHost = helvarPointsMap.get(ipController);
                if (helvarPointMapByHost == null) {
                    helvarPointMapByHost = new HashMap<>();
                }
                helvarPointMapByHost.put(instanceNumber, helvarPoint);

                helvarPointsMap.put(ipController, helvarPointMapByHost);
            }
        }
        return true;
    }

    public Map<Integer, HelvarPoint> getPointsMapByHost(String host) {
        return helvarPointsMap.get(host);
    }

    public HelvarPoint getPointByGroup(int group) {
        for (String host : helvarPointsMap.keySet()) {
            Map<Integer, HelvarPoint> pointsMapByHost = getPointsMapByHost(host);
            HelvarPoint helvarPoint = pointsMapByHost.get(group);
            if (helvarPoint != null) {
                return helvarPoint;
            }
        }
        return null;
    }

    @Override
    public synchronized boolean setValueFromHelvarNet(String host, int group, String type, float value) {
        if (host == null) {
            return false;
        }
        Map<Integer, HelvarPoint> helvarPointMapByHost = helvarPointsMap.get(host);
        if (helvarPointMapByHost == null) {
            return false;
        }
        HelvarPoint helvarPoint = helvarPointMapByHost.get(group);
        if (helvarPoint != null) {
            helvarPoint.updateValue(type, value, VALUES_TO_BACNET);
        }
        return true;
    }
}
