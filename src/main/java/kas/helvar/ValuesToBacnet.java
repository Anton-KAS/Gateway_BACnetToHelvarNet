package kas.helvar;

import kas.bacnet.BACnetLocalDevice;
import kas.bacnet.Point;

import java.util.Map;

public enum ValuesToBacnet {
    VALUES_TO_BACNET;

    private Map<String, Map<Integer, Point>> bacnetPointsMap;

    ValuesToBacnet() {
    }

    public void setBacnetPointList(BACnetLocalDevice localDevice) {
        this.bacnetPointsMap = localDevice.getPointMap();
    }

    public void setValue(String typeValue, int instanceNumber, float value) {
        Map<Integer, Point> bacnetPointsListByType = bacnetPointsMap.get(typeValue);
        if (bacnetPointsListByType == null) return;
        Point changePoint = bacnetPointsListByType.get(instanceNumber);
        if (changePoint == null) return;
        changePoint.setValue(value);
    }
}
