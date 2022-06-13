package kas.bacnet;

public interface SettableValueToBacnet {
    void setValue(String typeValue, int instanceNumber, float value);
}
