package kas.bacnet;

import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.type.constructed.Address;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;

import java.util.concurrent.LinkedBlockingDeque;

public enum BacnetReceivedObjectList {
    BACNET_RECEIVED_OBJECT_LIST;

    private final long start;
    private final LinkedBlockingDeque<BacnetReceivedObject> objectList;

    BacnetReceivedObjectList() {
        this.start = System.currentTimeMillis();
        this.objectList = new LinkedBlockingDeque<>();
    }

    public void addValueInTheEnd(Address from, BACnetObject obj, PropertyValue pv) {
        objectList.addLast(new BacnetReceivedObject(from, obj, pv));
    }

    public BacnetReceivedObject getFirst() throws InterruptedException {
        return objectList.takeFirst();
    }

    @Override
    public String toString() {
        return "Создан в " + start;
    }
}
