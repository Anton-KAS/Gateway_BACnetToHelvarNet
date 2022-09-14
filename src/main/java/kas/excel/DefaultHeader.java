package kas.excel;

import java.util.HashMap;
import java.util.Map;

public enum DefaultHeader {
    IP_CONTROLLER("IP Helvar controller:", -1),
    PORT_CONTROLLER("IP port Helvar controller:", -1),
    LIGHT_PANEL("Light panel:", -1),
    CONTROLLER_REGISTER("Controller Register:", -1),
    HELVAR_GROUP("Helvar group number", 0),
    ROOM("Room number", 1),
    ELECTRIC_GROUP("Electric group", 2),
    DIMMING("Dimming", 3),
    FADE_TIME("Fade Time", 4);

    private final String header;
    private final int numCol;

    DefaultHeader(String header, int numCol) {
        this.header = header;
        this.numCol = numCol;
    }

    public String getHeader() {
        return this.header;
    }

    public int getNumCol() {
        return this.numCol;
    }

    public static String[] getMainHeaders() {
        return new String[]{HELVAR_GROUP.getHeader(), ROOM.getHeader(), ELECTRIC_GROUP.getHeader(),
                DIMMING.getHeader(), FADE_TIME.getHeader()};
    }

    public static Map<String, Integer> getMainHeadersMap() {
        Map<String, Integer> mainHeadersMap = new HashMap<>();
        mainHeadersMap.put(HELVAR_GROUP.getHeader(), HELVAR_GROUP.getNumCol());
        mainHeadersMap.put(ROOM.getHeader(), ROOM.getNumCol());
        mainHeadersMap.put(ELECTRIC_GROUP.getHeader(), ELECTRIC_GROUP.getNumCol());
        mainHeadersMap.put(DIMMING.getHeader(), DIMMING.getNumCol());
        mainHeadersMap.put(FADE_TIME.getHeader(), FADE_TIME.getNumCol());
        return mainHeadersMap;
    }

    public static Map<Integer, String> getDefColNameMap() {
        Map<Integer, String> mainHeadersMap = new HashMap<>();
        mainHeadersMap.put(HELVAR_GROUP.getNumCol(), String.valueOf(HELVAR_GROUP));
        mainHeadersMap.put(ROOM.getNumCol(), String.valueOf(ROOM));
        mainHeadersMap.put(ELECTRIC_GROUP.getNumCol(), String.valueOf(ELECTRIC_GROUP));
        mainHeadersMap.put(DIMMING.getNumCol(), String.valueOf(DIMMING));
        mainHeadersMap.put(FADE_TIME.getNumCol(), String.valueOf(FADE_TIME));
        return mainHeadersMap;
    }
}
