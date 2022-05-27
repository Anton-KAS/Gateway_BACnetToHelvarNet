package kas.bacnet;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.exception.BACnetServiceException;
import com.serotonin.bacnet4j.obj.AnalogValueObject;
import com.serotonin.bacnet4j.type.enumerated.EngineeringUnits;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.Real;

public class AnalogValue extends Point {
    private final float presentValue;
    protected AnalogValueObject obj;

    public AnalogValue(LocalDevice localDevice, int instanceNumber, String name, boolean outOfService,
                       EngineeringUnits units, String description,
                       float presentValue) {
        super(localDevice, instanceNumber, name, outOfService, units, description);
        this.presentValue = presentValue;
    }

    @Override
    public Object getValue() throws BACnetServiceException {
        return obj.readProperty(PropertyIdentifier.presentValue);
    }

    @Override
    public void setValue(Object value) {
        obj.writePropertyInternal(PropertyIdentifier.presentValue, new Real((Float) value));
    }

    @Override
    public AnalogValue create() {
        try {
            obj = new AnalogValueObject(localDevice, instanceNumber, name, presentValue, units, outOfService);
            obj.writePropertyInternal(PropertyIdentifier.description, new CharacterString(description));
            obj.supportWritable();
        } catch (BACnetServiceException e) {
            e.printStackTrace();
        }
        return this;
    }
}
