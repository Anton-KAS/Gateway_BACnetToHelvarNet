package kas.bacnet;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.transport.Transport;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

public class LocalDeviceBuilder extends LocalDevice {
    public LocalDeviceBuilder(int deviceNumber, Transport transport) {
        super(deviceNumber, transport);
    }
    public LocalDeviceBuilder setVendorId(int vendorId) {
        writePropertyInternal(PropertyIdentifier.vendorIdentifier, new UnsignedInteger(vendorId));
        return this;
    }
    public LocalDeviceBuilder setVendorName(String vendorName) {
        writePropertyInternal(PropertyIdentifier.vendorName, new CharacterString(vendorName));
        return this;
    }
    public LocalDeviceBuilder setModelName(String modelName) {
        writePropertyInternal(PropertyIdentifier.modelName, new CharacterString(modelName));
        return this;
    }
    public LocalDeviceBuilder setObjectName(String objectName) {
        writePropertyInternal(PropertyIdentifier.objectName, new CharacterString(objectName));
        return this;
    }
    public LocalDeviceBuilder setLocation(String location) {
        writePropertyInternal(PropertyIdentifier.location, new CharacterString(location));
        return this;
    }
    public LocalDeviceBuilder setDescription(String description) {
        writePropertyInternal(PropertyIdentifier.description, new CharacterString(description));
        return this;
    }
}
