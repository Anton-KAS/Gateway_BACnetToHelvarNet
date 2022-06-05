package kas.exel;

import kas.excel.ExcelParser;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExcelParserTest {
    ExcelParser excel;
    String dirPath = "./src/test/resources/kas/excel/";
    String fileName = "TestConfigPoints.xlsx";

    @BeforeAll
    public static void started() {
        System.out.println("tests started");
    }

    @BeforeEach
    public void init() {
        System.out.println("test started");
        excel = new ExcelParser(dirPath, fileName);
    }

    @AfterEach
    public void finished() {
        System.out.println("test completed");
    }

    @AfterAll
    public static void finishedAll() {
        System.out.println("tests completed");
    }

    @Test
    public void testRealParseXlsxToJson() {
        //arrange
        String expected = parseExpectedJson();

        //act
        ExcelParser excelReal = new ExcelParser(dirPath, fileName);
        String result = excelReal.parseXlsxToJson().toString();
        System.out.println(result);

        //assert
        assertEquals(expected, result);
    }


    @Test
    public void testParseXlsxToJson() {
        //arrange
        String expected = parseExpectedJson();

        //act
        String result = excel.parseXlsxToJson().toString();
        System.out.println(result);

        //assert
        assertEquals(expected, result);
    }

    private String parseExpectedJson() {
        String testFileName = "ExelParserTestExpectedData.json";
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(dirPath + testFileName, StandardCharsets.UTF_8));
            JSONObject jsonObject = (JSONObject) obj;
            return jsonObject.toString().trim();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    @ParameterizedTest
    @MethodSource("sourceCheckIP")
    public void testCheckIP(String ip, Boolean expected) {
        //act
        boolean result = ExcelParser.checkIP(ip);

        //assert
        assertEquals(expected, result);
    }

    private static Stream<Arguments> sourceCheckIP() {
        return Stream.of(
                Arguments.of(null, false),
                Arguments.of("Any string text", false),
                Arguments.of("", false),
                Arguments.of("0.0.0.0", false),
                Arguments.of("1.2.3.4", true),
                Arguments.of("01.02.03.04", true),
                Arguments.of("001.002.003.004", true),
                Arguments.of("0001.0002.0003.0004", false),
                Arguments.of("0011.0022.0033.0044", false),
                Arguments.of("0111.0222.0333.0444", false),
                Arguments.of("1111.2222.3333.4444", false),
                Arguments.of("11.22.33.44", true),
                Arguments.of("254.254.254.254", true),
                Arguments.of("1234.2.3.4", false),
                Arguments.of("1.1234.3.4", false),
                Arguments.of("1.2.1234.4", false),
                Arguments.of("1.2.3.1234", false),
                Arguments.of("1.2.3.", false),
                Arguments.of("1.2.3.4.5", false),
                Arguments.of("1.2.3..4", false),
                Arguments.of("255.2.3.4", false),
                Arguments.of("1.255.3.4", false),
                Arguments.of("1.2.255.4", false),
                Arguments.of("1.2.3.255", false),
                Arguments.of("255.255.255.255", false)
        );
    }
}
