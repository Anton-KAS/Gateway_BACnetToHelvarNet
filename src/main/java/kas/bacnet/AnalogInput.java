package kas.bacnet;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.exception.BACnetServiceException;
import com.serotonin.bacnet4j.obj.AnalogInputObject;
import com.serotonin.bacnet4j.type.enumerated.EngineeringUnits;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.Real;

public class AnalogInput extends Point {
    private final float presentValue;
    private AnalogInputObject obj;

    public AnalogInput(LocalDevice localDevice, int instanceNumber, String name, boolean outOfService,
                       EngineeringUnits units, String description, float presentValue) {
        super(localDevice, instanceNumber, name, outOfService, units, description);

        if (Float.isNaN(presentValue)) {
            throw new IllegalArgumentException("Present value cannot be NaN");
        }

        this.presentValue = presentValue;
    }

    @Override
    public synchronized Object getValue() throws BACnetServiceException {
        validateObjectInitialized();
        return obj.readProperty(PropertyIdentifier.presentValue);
    }

    @Override
    public synchronized void setValue(Object value) {
        validateObjectInitialized();

        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }

        if (!(value instanceof Number)) {
            throw new IllegalArgumentException("Value must be a Number, got: " + value.getClass().getName());
        }

        float floatValue = ((Number) value).floatValue();
        obj.writePropertyInternal(PropertyIdentifier.presentValue, new Real(floatValue));
    }

    @Override
    public AnalogInput create() {
        try {
            obj = new AnalogInputObject(localDevice, instanceNumber, name, presentValue, units, outOfService);
            obj.writePropertyInternal(PropertyIdentifier.description, new CharacterString(description));
        } catch (BACnetServiceException e) {
            logger.error("Failed to create AnalogInputObject: " + e.getMessage(), e);
        }
        return this;
    }

    private void validateObjectInitialized() {
        if (obj == null) {
            throw new IllegalStateException("AnalogInputObject not initialized. Call create() first.");
        }
    }
}
