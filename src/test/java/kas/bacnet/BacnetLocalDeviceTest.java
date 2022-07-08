package kas.bacnet;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.*;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BacnetLocalDeviceTest {
    Properties bacnetConfig = new ConfigLoader().getConfig();
    BacnetLocalDevice localDevice = new BacnetLocalDevice(bacnetConfig);
    String dirPath = "./src/test/resources/kas/excel/";

    @BeforeEach
    public void init() {
        System.out.println("Start Test");
    }

    @Test
    public void testAddPointsFromJson() {
        //arrange
        JSONObject expected = parseExpectedJson();
        assert expected != null;

        //act
        boolean result = localDevice.addPointsFromJson(expected);

        //assert
        assertTrue(result);
    }

    private JSONObject parseExpectedJson() {
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
