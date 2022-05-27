package kas.bacnet;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.exception.BACnetServiceException;
import com.serotonin.bacnet4j.obj.AnalogOutputObject;
import com.serotonin.bacnet4j.type.enumerated.EngineeringUnits;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.Real;

public class AnalogOutput extends Point {
    private final float presentValue;
    private AnalogOutputObject obj;
    private final float relinquishDefault;

    public AnalogOutput(LocalDevice localDevice, int instanceNumber, String name, boolean outOfService,
                        EngineeringUnits units, String description,
                        float presentValue, float relinquishDefault) {
        super(localDevice, instanceNumber, name, outOfService, units, description);
        this.presentValue = presentValue;
        this.relinquishDefault = relinquishDefault;
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
    public AnalogOutput create() {
        try {
            obj = new AnalogOutputObject(localDevice, instanceNumber, name, presentValue, units, outOfService, relinquishDefault);
            obj.writePropertyInternal(PropertyIdentifier.description, new CharacterString(description));
        } catch (BACnetServiceException e) {
            e.printStackTrace();
        }
        return this;
    }
}
