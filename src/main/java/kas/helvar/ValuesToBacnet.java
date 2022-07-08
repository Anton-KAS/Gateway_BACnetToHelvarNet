package kas.helvar;

import kas.bacnet.BacnetLocalDevice;
import kas.bacnet.Point;
import kas.bacnet.SettableValueToBacnet;

import java.util.Map;

public enum ValuesToBacnet implements SettableValueToBacnet {
    VALUES_TO_BACNET;

    private Map<String, Map<Integer, Point>> bacnetPointsMap;

    ValuesToBacnet() {
    }

    public synchronized void setBacnetPointList(BacnetLocalDevice localDevice) {
        this.bacnetPointsMap = localDevice.getPointMap();
    }

    @Override
    public synchronized void setValue(String typeValue, int instanceNumber, float value) {
        Map<Integer, Point> bacnetPointsListByType = bacnetPointsMap.get(typeValue);
        if (bacnetPointsListByType == null) return;
        Point changePoint = bacnetPointsListByType.get(instanceNumber);
        if (changePoint == null) return;
        changePoint.setValue(value);
    }
}
