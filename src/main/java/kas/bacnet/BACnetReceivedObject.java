package kas.bacnet;

import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.Address;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.primitive.Real;
import jflex.Main;
import kas.excel.ExcelParser;
import org.apache.log4j.Logger;

public class BACnetReceivedObject {
    private final Logger logger;

    private final Address from;
    private final BACnetObject obj;
    private final PropertyValue pv;

    public BACnetReceivedObject(Address from, BACnetObject obj, PropertyValue pv) {
        this.logger = Logger.getLogger(ExcelParser.class);

        this.from = from;
        this.obj = obj;
        this.pv = pv;

        logger.info(this.toString());
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
        return String.format("BACnet changes: from: %s, %s, %s: %s", from.getMacAddress(), obj.getObjectName(), pv.getPropertyIdentifier(), pv.getValue());
    }
}
