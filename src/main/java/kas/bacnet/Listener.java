package kas.bacnet;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.RemoteObject;
import com.serotonin.bacnet4j.event.DeviceEventListener;
import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.service.Service;
import com.serotonin.bacnet4j.type.constructed.*;
import com.serotonin.bacnet4j.type.enumerated.*;
import com.serotonin.bacnet4j.type.notificationParameters.NotificationParameters;
import com.serotonin.bacnet4j.type.primitive.Boolean;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;
import org.apache.log4j.Logger;

import java.util.List;

import static kas.bacnet.BacnetReceivedObjectList.BACNET_RECEIVED_OBJECT_LIST;

public class Listener implements DeviceEventListener {
    private final Logger logger;
    List<RemoteDevice> remoteDevices;
    LocalDevice localDevice;
    BacnetLocalDevice bacnetLocalDevice;

    public Listener(LocalDevice localDevice, List<RemoteDevice> remoteDevices, BacnetLocalDevice bacnetLocalDevice) {
        this.logger = Logger.getLogger(BacnetLocalDevice.class);

        this.remoteDevices = remoteDevices;
        this.localDevice = localDevice;
        this.bacnetLocalDevice = bacnetLocalDevice;
    }

    @Override
    public void listenerException(Throwable e) {
        System.out.println("listenerException: " + e);
    }

    @Override
    public void iAmReceived(RemoteDevice d) {
    }

    @Override
    public boolean allowPropertyWrite(Address from, BACnetObject obj, PropertyValue pv) {
        return true;
    }

    @Override
    public void propertyWritten(Address from, BACnetObject obj, PropertyValue pv) {
        logger.info("BACnet propertyWritten from: " + from.getDescription() + " | " + from + "\tproperty: " + pv);
        BACNET_RECEIVED_OBJECT_LIST.addValueInTheEnd(from, obj, pv);
    }

    @Override
    public void iHaveReceived(RemoteDevice d, RemoteObject o) {
        System.out.println("iHaveReceived: " + d + " | " + o);
    }

    @Override
    public void covNotificationReceived(UnsignedInteger subscriberProcessIdentifier, ObjectIdentifier initiatingDeviceIdentifier, ObjectIdentifier monitoredObjectIdentifier, UnsignedInteger timeRemaining, SequenceOf<PropertyValue> listOfValues) {
    }

    @Override
    public void eventNotificationReceived(UnsignedInteger processIdentifier, ObjectIdentifier initiatingDeviceIdentifier, ObjectIdentifier eventObjectIdentifier, TimeStamp timeStamp, UnsignedInteger notificationClass, UnsignedInteger priority, EventType eventType, CharacterString messageText, NotifyType notifyType, Boolean ackRequired, EventState fromState, EventState toState, NotificationParameters eventValues) {
    }

    @Override
    public void textMessageReceived(ObjectIdentifier textMessageSourceDevice, Choice messageClass, MessagePriority messagePriority, CharacterString message) {
    }

    @Override
    public void synchronizeTime(Address from, DateTime dateTime, boolean utc) {
        System.out.println("synchronizeTime: " + from + " | " + dateTime + " | " + utc);
    }

    @Override
    public void requestReceived(Address from, Service service) {
    }
}
