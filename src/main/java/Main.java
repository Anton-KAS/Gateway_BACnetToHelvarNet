import com.serotonin.bacnet4j.exception.BACnetServiceException;
import kas.bacnet.BACnetLocalDevice;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) throws InterruptedException, BACnetServiceException {
        BACnetLocalDevice localDevice = new BACnetLocalDevice();
        JSONObject json = parseExpectedJson();
        localDevice.addPointsFromJson(json);
        localDevice.run();
    }

    private static JSONObject parseExpectedJson() {
        String dirPath = "./src/test/resources/kas/excel/";
        String testFileName = "ExelParserTestExpectedData.json";
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(dirPath + testFileName, StandardCharsets.UTF_8));
            return (JSONObject) obj;
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
