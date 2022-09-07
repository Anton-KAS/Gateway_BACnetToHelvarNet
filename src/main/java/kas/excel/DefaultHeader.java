package kas.excel;

public enum DefaultHeader {
    IP_CONTROLLER("IP Helvar controller:"),
    PORT_CONTROLLER("IP port Helvar controller:"),
    LIGHT_PANEL("Light panel:"),
    CONTROLLER_REGISTER("Controller Register:"),
    HELVAR_GROUP("Helvar group number"),
    ROOM("Room number"),
    ELECTRIC_GROUP("Electric group"),
    DIMMING("Dimming"),
    FADE_TIME("Fade Time");

    private final String header;

    DefaultHeader(String header) {
        this.header = header;
    }

    public String getHeader() {
        return this.header;
    }

    public static String[] getMainHeaders() {
        return new String[]{HELVAR_GROUP.getHeader(), ROOM.getHeader(), ELECTRIC_GROUP.getHeader(),
                DIMMING.getHeader(), FADE_TIME.getHeader()};
    }
}
