package kas.bacnet;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.exception.BACnetServiceException;
import com.serotonin.bacnet4j.type.enumerated.EngineeringUnits;

import java.util.Objects;

public abstract class Point {
    protected LocalDevice localDevice;
    protected int instanceNumber;
    protected String name;
    protected boolean outOfService;
    protected EngineeringUnits units;
    protected String description;

    public Point(LocalDevice localDevice, int instanceNumber, String name, boolean outOfService,
                 EngineeringUnits units, String description) {
        this.localDevice = localDevice;
        this.instanceNumber = instanceNumber;
        this.name = name;
        this.outOfService = outOfService;
        this.units = units;
        this.description = description;
    }

    public abstract Object getValue() throws BACnetServiceException;

    public abstract void setValue(Object value);

    public abstract Point create();

    @Override
    public String toString() {
        String currentValue = "---";
        try {
            currentValue = getValue().toString();
        } catch (BACnetServiceException e) {
            e.printStackTrace();
        } finally {
            return String.format("%s, %s, %s = %s, %s", instanceNumber, name, description, currentValue, units);
        }

    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || !obj.getClass().equals(this.getClass())) return false;

        Point altPoint = (Point) obj;
        return name.equals(altPoint.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
