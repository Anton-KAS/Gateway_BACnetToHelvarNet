package kas.bacnet;

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

public class Listener implements DeviceEventListener {
    @Override
    public void listenerException(Throwable e) {
        System.out.println("Listener:\tlistenerException\t" + e);
    }

    @Override
    public void iAmReceived(RemoteDevice d) {
        //System.out.println("Listener: iAmReceived");
    }

    @Override
    public boolean allowPropertyWrite(Address from, BACnetObject obj, PropertyValue pv) {
        return true;
    }

    @Override
    public void propertyWritten(Address from, BACnetObject obj, PropertyValue pv) {
        BACnetReceivedObjectList.BACNET_RECEIVED_OBJECT_LIST.addValueInTheEnd(from, obj, pv);
    }

    @Override
    public void iHaveReceived(RemoteDevice d, RemoteObject o) {
        //System.out.println("Listener: iHaveReceived");
    }

    @Override
    public void covNotificationReceived(UnsignedInteger subscriberProcessIdentifier, ObjectIdentifier initiatingDeviceIdentifier, ObjectIdentifier monitoredObjectIdentifier, UnsignedInteger timeRemaining, SequenceOf<PropertyValue> listOfValues) {
        System.out.println("Listener: covNotificationReceived");
    }

    @Override
    public void eventNotificationReceived(UnsignedInteger processIdentifier, ObjectIdentifier initiatingDeviceIdentifier, ObjectIdentifier eventObjectIdentifier, TimeStamp timeStamp, UnsignedInteger notificationClass, UnsignedInteger priority, EventType eventType, CharacterString messageText, NotifyType notifyType, Boolean ackRequired, EventState fromState, EventState toState, NotificationParameters eventValues) {
        System.out.println("Listener: eventNotificationReceived / eventType " + eventType + " / messageText " + messageText);
    }

    @Override
    public void textMessageReceived(ObjectIdentifier textMessageSourceDevice, Choice messageClass, MessagePriority messagePriority, CharacterString message) {
        System.out.println("Listener: textMessageReceived");
    }

    @Override
    public void synchronizeTime(Address from, DateTime dateTime, boolean utc) {
        System.out.println("Listener: synchronizeTime");
    }

    @Override
    public void requestReceived(Address from, Service service) {
    }
}
