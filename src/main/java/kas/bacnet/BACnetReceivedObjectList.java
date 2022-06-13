package kas.bacnet;

import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.type.constructed.Address;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;

import java.util.LinkedList;

public enum BACnetReceivedObjectList {
    BACNET_RECEIVED_OBJECT_LIST;

    private final long start;
    private LinkedList<BACnetReceivedObject> objectList;

    BACnetReceivedObjectList() {
        this.start = System.currentTimeMillis();
        this.objectList = new LinkedList<>();
    }

    public void addValueInTheEnd(Address from, BACnetObject obj, PropertyValue pv) {
        System.out.println("BACNET_RECEIVED_OBJECT_LIST ++++ ADD LAST");
        objectList.addLast(new BACnetReceivedObject(from, obj, pv));
    }

    public BACnetReceivedObject poolFirst() {
        BACnetReceivedObject baCnetReceivedObject = objectList.pollFirst();
        if (baCnetReceivedObject != null) {
            System.out.println("BACNET_RECEIVED_OBJECT_LIST ---- POOL FIRST: " + baCnetReceivedObject);
        }
        return baCnetReceivedObject;

    }

    @Override
    public String toString() {
        return "Создан в " + start;
    }
}
