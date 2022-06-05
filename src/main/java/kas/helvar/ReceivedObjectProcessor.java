package kas.helvar;

import java.util.HashMap;
import java.util.Map;

public class ReceivedObjectProcessor {
    private static final String TERMINATOR = "#";
    private static final String DELIMITER = ",";
    private static final String PARAMETER_DELIMITER = ":";
    private static final String ANSWER = "=";
    private static final String COMMAND = ">";
    private static final String REPLY = "?";
    private static final String C_LSIB = "103";
    private static final String C_CONSUMPTION = "161";
    private static final String C_RECALL_SCENE = "11";
    private static final String C_DIRECT_LEVEL = "13";


    public static void processing(String host, String receiveMessage, SetValueFromHelvarNet helvarPointsMap) {
        if (receiveMessage == null) {
            return;
        }
        String[] receiveMessages = receiveMessage.split(TERMINATOR);
        for (String message : receiveMessages) {
            Map<String, String> parametersMap = new HashMap<>();

            String[] units = message.split(DELIMITER);
            parametersMap.put("type", units[0].substring(0, 1));
            for (String unit : units) {
                if (unit.contains(ANSWER)) {
                    String[] answer = unit.split(ANSWER);
                    parametersMap.put("answer", answer[1]);
                    unit = unit.substring(0, unit.indexOf(ANSWER));
                }
                String[] parameter = unit.split(PARAMETER_DELIMITER);
                if (parameter.length >= 2) {
                    parametersMap.put(parameter[0], parameter[1]);
                }
            }
            String type = parametersMap.get("type");
            String value = null;
            String valueType = null;
            switch (type) {
                case REPLY:
                    switch (parametersMap.get("C")) {
                        case C_LSIB:
                            if (parametersMap.get("B") == null & !parametersMap.get("B").equals("1")) {
                                continue;
                            }
                            valueType = "av";
                            break;
                        case C_CONSUMPTION:
                            valueType = "ai";
                            break;
                    }
                    value = parametersMap.get("answer");
                    break;

                case COMMAND:
                    switch (parametersMap.get("C")) {
                        case C_RECALL_SCENE:
                            if (parametersMap.get("B") == null & !parametersMap.get("B").equals("1")) {
                                continue;
                            }
                            valueType = "av";
                            value = parametersMap.get("S");
                            break;
                        case C_DIRECT_LEVEL:
                            valueType = "ao";
                            value = parametersMap.get("L");
                            break;
                    }
                    break;
            }
            if (valueType != null & parametersMap.get("G") != null & value != null) {
                helvarPointsMap.setValueFromHelvarNet(host, Integer.parseInt(parametersMap.get("G")), valueType, Float.parseFloat(value));
            }
        }
    }
}
