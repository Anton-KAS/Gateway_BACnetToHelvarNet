import kas.bacnet.BACnetLocalDevice;
import kas.excel.ExcelParser;
import kas.helvar.HelvarServicesStarter;
import org.apache.logging.log4j.LogManager;
import org.json.simple.JSONObject;

import java.util.Scanner;
import java.util.logging.Logger;

import static kas.helvar.ValuesToBacnet.VALUES_TO_BACNET;

public class Main {
    public static void main(String[] args) {


        Scanner in = new Scanner(System.in);
//        System.out.println("Let's start? (y/n)");
//        String s = in.nextLine();
//        if (!s.equals("y")) {
//            return;
//        }

        System.out.println("Working Directory = " + System.getProperty("user.dir"));
//        JSONObject json = parseExpectedJson();
        ExcelParser excelParser = new ExcelParser();
        System.out.println("Create  excelParser");

        JSONObject json = excelParser.parseXlsxToJson();
        System.out.println("Get json");
        System.out.println(json);

        BACnetLocalDevice localDevice = new BACnetLocalDevice();
        System.out.println("Create localDevice");

        localDevice.addPointsFromJson(json);
        System.out.println("addPointsFromJson");

        VALUES_TO_BACNET.setBacnetPointList(localDevice);
        System.out.println("setBacnetPointList");

        //localDevice.run();
        (new Thread(localDevice)).start();;

        HelvarServicesStarter helvarServicesStarter = new HelvarServicesStarter(json);
        System.out.println("Create helvarServicesStarter");

        (new Thread(helvarServicesStarter)).start();;
        //helvarServicesStarter.run();
        System.out.println("startHelvarServices");


    }

//    private static JSONObject parseExpectedJson() {
//        String dirPath = "./src/test/resources/kas/excel/";
//        String testFileName = "ExelParserTestExpectedData.json";
//        JSONParser parser = new JSONParser();
//        try {
//            Object obj = parser.parse(new FileReader(dirPath + testFileName, StandardCharsets.UTF_8));
//            return (JSONObject) obj;
//        } catch (IOException | ParseException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
}
