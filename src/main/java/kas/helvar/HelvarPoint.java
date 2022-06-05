package kas.helvar;

import java.util.Objects;

import static kas.helvar.ValuesToBacnet.VALUES_TO_BACNET;

public class HelvarPoint {
    private final int group;
    private final String description;
    private final boolean dimming;
    private float sceneValue;
    private float directLevelValue;
    private float consumptionValue;
    private final Client client;

    private final String M_START = ">V:1";
    private final String M_TERMINATOR = "#";
    private final String M_BLOCK = "B:1";
    private final String M_GROUP;

    private final String M_FADE_TIME;

    public HelvarPoint(int group, boolean dimming, String ipController, int portController, String description) {
        this.group = group;
        this.description = description;
        this.dimming = dimming;
        this.client = new Client(ipController, portController);

        this.M_GROUP = String.format("G:%s", group);

        int fade_time = dimming ? 2 : 0;

        this.M_FADE_TIME = String.format("F:%s", fade_time);
        System.out.println(this);
    }

    public int getGroup() {
        return group;
    }

    public void setSceneValue(float sceneValue) {
        this.sceneValue = sceneValue;
    }

    public void setDirectLevelValue(float directLevelValue) {
        this.directLevelValue = directLevelValue;
    }

    public void setConsumptionValue(float consumptionValue) {
        this.consumptionValue = consumptionValue;
    }

    public void sendReadSceneValue() {
        // LSIB - Last Scene In Block
        String LSIB_COMMAND = "C:103";
        String query = String.format("%s,%s,%s,%s%s", M_START, LSIB_COMMAND, M_GROUP, M_BLOCK, M_TERMINATOR);
        client.sendValue(query);
    }

    public void sendReadConsumption() {
        if (!dimming) {
            return;
        }
        String CONSUMPTION_COMMAND = "C:161";
        String query = String.format("%s,%s,%s%s", M_START, CONSUMPTION_COMMAND, M_GROUP, M_TERMINATOR);
        client.sendValue(query);
    }

    public void updateValue(String type, float value) {
        switch (type) {
            case "av":
                this.sceneValue = value;
                VALUES_TO_BACNET.setValue("av", group, value);

            case "ai":
                this.consumptionValue = value;
                VALUES_TO_BACNET.setValue("ai", group, value);

            case "ao":
                this.directLevelValue = value;
                VALUES_TO_BACNET.setValue("ao", group, value);
        }
    }

    @Override
    public String toString() {
        return description + " | Dimming = " + dimming
                + " | Scene = " + sceneValue + " / Direct Level = " + directLevelValue + " / Consumption = " + consumptionValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || !obj.getClass().equals(HelvarPoint.class)) return false;

        HelvarPoint altPoint = (HelvarPoint) obj;

        return group == altPoint.group;
    }

    @Override
    public int hashCode() {
        return Objects.hash(group);
    }

}
