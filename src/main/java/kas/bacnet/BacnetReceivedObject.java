package kas.bacnet;

import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.Address;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;

public class BacnetReceivedObject {
    private final Address from;
    private final BACnetObject obj;
    private final PropertyValue pv;

    public BacnetReceivedObject(Address from, BACnetObject obj, PropertyValue pv) {
        this.from = from;
        this.obj = obj;
        this.pv = pv;
        //System.out.println("NEW BRO: " + from + " | " + obj + " | " + pv);
    }

    public int getObjectId() {
        return obj.getInstanceId();
    }

    public String getType() {
        if (obj.getObjectName().contains("scene")) {
            return "av";
        } else if (obj.getObjectName().contains("direct level")) {
            return "ao";
        }
        return null;
    }

    public int getValue() {
        Encodable realValue = pv.getValue();
        String stringValue = realValue.toString();
        float floatValue = Float.parseFloat(stringValue);
        return Math.round(floatValue);
    }

    @Override
    public String toString() {
        return String.format("BacnetReceivedObject: from: %s, %s, %s: %s", from.getMacAddress(), obj.getObjectName(), pv.getPropertyIdentifier(), pv.getValue());
    }
}
