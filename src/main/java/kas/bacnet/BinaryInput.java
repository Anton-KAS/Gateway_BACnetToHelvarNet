package kas.bacnet;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.exception.BACnetServiceException;
import com.serotonin.bacnet4j.obj.AnalogInputObject;
import com.serotonin.bacnet4j.obj.BinaryInputObject;
import com.serotonin.bacnet4j.type.enumerated.BinaryPV;
import com.serotonin.bacnet4j.type.enumerated.EngineeringUnits;
import com.serotonin.bacnet4j.type.enumerated.Polarity;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.Real;

public class BinaryInput extends Point {
    private final BinaryPV presentValue;
    private BinaryInputObject obj;

    public BinaryInput(LocalDevice localDevice, int instanceNumber, String name, boolean outOfService,
                       EngineeringUnits units, String description,
                       String presentValue) {
        super(localDevice, instanceNumber, name, outOfService, units, description);
        if (presentValue.equals("online")) {
            this.presentValue = BinaryPV.active;
        } else {
            this.presentValue = BinaryPV.inactive;
        }
    }

    @Override
    public synchronized Object getValue() throws BACnetServiceException {
        return obj.readProperty(PropertyIdentifier.presentValue);
    }

    @Override
    public synchronized void setValue(Object value) {
        BinaryPV valueToSet;
        if ((float) value == 1f) {
            valueToSet =  BinaryPV.active;
        } else {
            valueToSet =  BinaryPV.inactive;
        }
        obj.writePropertyInternal(PropertyIdentifier.presentValue, valueToSet);
    }

    @Override
    public BinaryInput create() {
        try {
            obj = new BinaryInputObject(localDevice, instanceNumber, name, presentValue, outOfService, Polarity.normal);
            obj.writePropertyInternal(PropertyIdentifier.description, new CharacterString(description));
        } catch (BACnetServiceException e) {
            logger.error(e.toString());
        }
        return this;
    }
}
