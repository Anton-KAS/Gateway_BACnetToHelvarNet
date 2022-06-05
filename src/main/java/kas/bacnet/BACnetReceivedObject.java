package kas.bacnet;

import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.type.constructed.Address;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;

public class BACnetReceivedObject {
    private final Address from;
    private final BACnetObject obj;
    private final PropertyValue pv;

    public BACnetReceivedObject(Address from, BACnetObject obj, PropertyValue pv) {
        this.from = from;
        this.obj = obj;
        this.pv = pv;
        System.out.println(this); // TODO: DEL after test
    }

    @Override
    public String toString() {
        return String.format("BACnet changes: from: %s, %s, %s: %s", from.getMacAddress(), obj.getObjectName(), pv.getPropertyIdentifier(), pv.getValue());
    }
}
