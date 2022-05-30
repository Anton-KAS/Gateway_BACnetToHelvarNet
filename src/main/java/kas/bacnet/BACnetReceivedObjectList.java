package kas.bacnet;

import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.type.constructed.Address;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;

import java.util.LinkedList;

public enum BACnetReceivedObjectList {
    RECEIVED_OBJECT_LIST;

    private final long start;
    private LinkedList<BACnetReceivedObject> ObjectList;

    BACnetReceivedObjectList() {
        this.start = System.currentTimeMillis();
        this.ObjectList = new LinkedList<>();
    }

    public void addValueInTheEnd(Address from, BACnetObject obj, PropertyValue pv) {
        ObjectList.addLast(new BACnetReceivedObject(from, obj, pv));
    }

    public BACnetReceivedObject poolFirst() {
        return ObjectList.pollFirst();
    }

    @Override
    public String toString() {
        return "Создан в " + start;
    }
}
