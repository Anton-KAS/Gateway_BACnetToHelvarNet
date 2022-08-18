package kas.helvar;

import kas.bacnet.SettableValueToBacnet;

import java.util.Objects;

public class HelvarPoint {
    private final String host;
    private final int group;
    private final String description;
    private final boolean dimming;
    private float sceneValue;
    private float directLevelValue;
    private float consumptionValue;

    private final String M_START = ">V:1";
    private final String M_TERMINATOR = "#";
    private final String M_BLOCK = "B:1";
    private final String M_GROUP;
    private final String M_CONSTANT_LIGHT = "K:1";

    private final String M_FADE_TIME;
    private final String M_FADE_TIME_MIN;

    public HelvarPoint(String host, int group, boolean dimming, int fadeTime, String description) {
        this.host = host;
        this.group = group;
        this.description = description;
        this.dimming = dimming;

        this.M_GROUP = String.format("G:%s", group);
        this.M_FADE_TIME = String.format("F:%s", fadeTime);
        this.M_FADE_TIME_MIN = String.format("F:%s", 1);
    }

    public String getHost() {
        return host;
    }

    public String getReadSceneQuery() {
        // LSIB - Last Scene In Block
        String LSIB_COMMAND = "C:103";
        return String.format("%s,%s,%s,%s%s", M_START, LSIB_COMMAND, M_GROUP, M_BLOCK, M_TERMINATOR);
    }

    public String getReadConsumptionQuery() {
        String CONSUMPTION_COMMAND = "C:161";

        if (!dimming) return null;

        return String.format("%s,%s,%s%s", M_START, CONSUMPTION_COMMAND, M_GROUP, M_TERMINATOR);
    }

    public String getRecallSceneQuery(int sceneNum) {
        String command = "C:11";

        sceneNum = Math.min(sceneNum, 16);
        sceneNum = Math.max(sceneNum, 1);
        String fadeTime = sceneNum == 12 ? M_FADE_TIME_MIN : M_FADE_TIME;
        String scene = String.format("S:%s", sceneNum);
        return String.format("%s,%s,%s,%s,%s,%s,%s%s", M_START, command, M_GROUP, M_CONSTANT_LIGHT, M_BLOCK, scene, fadeTime, M_TERMINATOR);
    }

    public String getDirectLevelQuery(int directLevelInt) {
        String command = "C:13";

        directLevelInt = Math.min(directLevelInt, 100);
        directLevelInt = Math.max(directLevelInt, 0);
        String direct_level = String.format("L:%s", directLevelInt);
        return String.format("%s,%s,%s,%s,%s%s", M_START, command, M_GROUP, direct_level, M_FADE_TIME, M_TERMINATOR);
    }

    public synchronized void updateValue(String type, float value, SettableValueToBacnet VALUES_TO_BACNET) {
        switch (type) {
            case "av":
                if (sceneValue != value) {
                    this.sceneValue = value;
                    VALUES_TO_BACNET.setValue("av", group, value);
                }
                break;

            case "ai":
                if (consumptionValue != value) {
                    this.consumptionValue = value;
                    VALUES_TO_BACNET.setValue("ai", group, value);
                }
                break;

            case "ao":
                if (directLevelValue != value) {
                    this.directLevelValue = value;
                    VALUES_TO_BACNET.setValue("ao", group, value);
                }
                break;
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
