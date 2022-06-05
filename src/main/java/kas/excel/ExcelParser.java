package kas.excel;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.json.JSONObject;
import org.json.simple.JSONObject;

public class ExcelParser {
    private String dirPath = "./src/main/resources/kas/excel/";
    private String fileName = "configPoints.xlsx";

    public ExcelParser() {
    }

    public ExcelParser(String dirPath, String fileName) {
        this.dirPath = dirPath;
        this.fileName = fileName;
    }

    public JSONObject parseXlsxToJson() {
        try {
            //String currentDir = System.getProperty("user.dir");
            File file = new File(dirPath + fileName);
            System.out.println("FILE PATH " + file);
            FileInputStream fis = new FileInputStream(file);
            XSSFWorkbook wb = new XSSFWorkbook(fis);

            JSONObject jsonData = new JSONObject();

            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                XSSFSheet sheet = wb.getSheetAt(i);
                if (sheet.getSheetName().equals(DefaultSheets.DESCRIPTION.toString()) |
                        sheet.getSheetName().equals(DefaultSheets.PATTERN.toString()) |
                        sheet.getSheetName().equals(DefaultSheets.SETTINGS.toString())) { // TODO: Добавить обработчик параметров
                    continue;
                }

                JSONObject jsonController = new JSONObject();
                JSONObject jsonPoints = new JSONObject();

                String[] mainHeaders = DefaultHeader.getMainHeaders();
                int headRowIndex = -1;
                int[] headColIndexes = new int[mainHeaders.length];
                for (Row row : sheet) {
                    JSONObject jsonPoint = new JSONObject();

                    Iterator<Cell> cellIterator = row.cellIterator();
                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();
                        int cellColumnIndex = cell.getColumnIndex();
                        int cellRowIndex = cell.getRowIndex();
                        switch (cell.getCellType()) {

                            case STRING:
                                String cellValue = cell.getStringCellValue().trim();

                                if (cellIterator.hasNext() && headRowIndex == -1) {
                                    // Поиск IP Контроллера
                                    if (cellValue.equals(DefaultHeader.IP_CONTROLLER.getHeader())) {
                                        cell = cellIterator.next();

                                        String nextCellValue = cell.getStringCellValue();
                                        if (checkIP(nextCellValue)) {
                                            jsonController.put(DefaultHeader.IP_CONTROLLER.toString(), nextCellValue);

                                            // Добавим поле со статусом контроллера (поумолчанию "Не в сети")
                                            jsonController.put("STATUS", false);
                                            break;
                                        }
                                    }

                                    // Поиск номера порта
                                    if (cellValue.equals(DefaultHeader.PORT_CONTROLLER.getHeader())) {
                                        cell = cellIterator.next();
                                        switch (cell.getCellType()) {
                                            case STRING:
                                                String nextCellValue = cell.getStringCellValue();
                                                if (nextCellValue != null && nextCellValue.length() > 0) {
                                                    jsonController.put(DefaultHeader.PORT_CONTROLLER.toString(), Integer.parseInt(nextCellValue));
                                                    break;
                                                }
                                            case NUMERIC:
                                                double doubleValue = cell.getNumericCellValue();
                                                jsonController.put(DefaultHeader.PORT_CONTROLLER.toString(),(int) doubleValue);
                                                break;
                                            default:
                                        }
                                    }

                                    // Поиск название щита освещения
                                    if (cellValue.equals(DefaultHeader.LIGHT_PANEL.getHeader())) {
                                        cell = cellIterator.next();
                                        String nextCellValue = cell.getStringCellValue();
                                        if (nextCellValue != null && nextCellValue.length() > 0) {
                                            jsonController.put(DefaultHeader.LIGHT_PANEL.toString(), nextCellValue);
                                            break;
                                        }
                                    }
                                }

                                // Поиск номера столбца с Helvar группой
                                if (cellValue.equals(DefaultHeader.HELVAR_GROUP.getHeader())) {
                                    headColIndexes[0] = cellColumnIndex;
                                    if (headRowIndex == -1) headRowIndex = cellRowIndex;
                                }
                                // Поиск номера столбца с номером помещения
                                if (cellValue.equals(DefaultHeader.ROOM.getHeader())) {
                                    headColIndexes[1] = cellColumnIndex;
                                    if (headRowIndex == -1) headRowIndex = cellRowIndex;
                                }
                                // Поиск номера столбца с электрической группой
                                if (cellValue.equals(DefaultHeader.ELECTRIC_GROUP.getHeader())) {
                                    headColIndexes[2] = cellColumnIndex;
                                    if (headRowIndex == -1) headRowIndex = cellRowIndex;
                                }
                                // Поиск номера столбца с диммированием
                                if (cellValue.equals(DefaultHeader.DIMMING.getHeader())) {
                                    headColIndexes[3] = cellColumnIndex;
                                    if (headRowIndex == -1) headRowIndex = cellRowIndex;
                                }

                                // Поисх данных по точкам
                                if (headRowIndex != -1 && cellRowIndex > headRowIndex) {
                                    if (headColIndexes[0] == cellColumnIndex) {
                                        int intValue = Integer.parseInt(cellValue);
                                        jsonPoint.put(DefaultHeader.HELVAR_GROUP.toString(), intValue);
                                    }
                                    if (headColIndexes[1] == cellColumnIndex) {
                                        jsonPoint.put(DefaultHeader.ROOM.toString(), cellValue);
                                    }
                                    if (headColIndexes[2] == cellColumnIndex) {
                                        jsonPoint.put(DefaultHeader.ELECTRIC_GROUP.toString(), cellValue);
                                    }
                                    if (headColIndexes[3] == cellColumnIndex) {
                                        Boolean boolValue = Boolean.parseBoolean(cellValue);
                                        jsonPoint.put(DefaultHeader.DIMMING.toString(), boolValue);
                                    }
                                }

                                break;
                            case NUMERIC:
                                // Поисх данных по точкам
                                if (headRowIndex != -1 && cell.getRowIndex() > headRowIndex) {
                                    double doubleValue = cell.getNumericCellValue();
                                    String stringValue = Double.toString(doubleValue);
                                    if (headColIndexes[0] == cell.getColumnIndex()) {
                                        int intValue = (int) doubleValue;
                                        jsonPoint.put(DefaultHeader.HELVAR_GROUP.toString(), intValue);
                                    }
                                    if (headColIndexes[1] == cellColumnIndex) {
                                        jsonPoint.put(DefaultHeader.ROOM.toString(), stringValue);
                                    }
                                    if (headColIndexes[2] == cellColumnIndex) {
                                        jsonPoint.put(DefaultHeader.ELECTRIC_GROUP.toString(), stringValue);
                                    }
                                    if (headColIndexes[3] == cellColumnIndex) {
                                        Boolean boolValue = Boolean.parseBoolean(stringValue);
                                        jsonPoint.put(DefaultHeader.DIMMING.toString(), boolValue);
                                    }
                                }
                                break;
                            default:
                        }
                    }

                    if (headRowIndex != -1 && row.getRowNum() > headRowIndex) {
                        jsonPoints.put(jsonPoint.get(DefaultHeader.HELVAR_GROUP.toString()).toString(), jsonPoint);
                    }
                }
                jsonController.put("Points", jsonPoints);
                jsonData.put(jsonController.get(DefaultHeader.IP_CONTROLLER.toString()).toString(), jsonController);
            }
            return jsonData;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // TODO: Подумать, может выделить в отдельный класс
    public static boolean checkIP(String ip) {
        if (ip == null) return false;

        List<String> ipParts = List.of(ip.trim().split("\\."));
        if (ipParts.size() != 4) return false;

        String[] stringPatterns = {"0{0,2}[1-9]", "0?[1-9][0-9]", "1[0-9]{1,2}", "2[0-4][0-9]", "25[0-4]"};

        Pattern[] patterns = new Pattern[stringPatterns.length];
        for (int n = 0; n < stringPatterns.length; n++) {
            patterns[n] = Pattern.compile(stringPatterns[n]);
        }
        int n = 0;
        for (String part : ipParts) {
            for (Pattern pattern : patterns) {
                if (pattern.matcher(part).matches()) {
                    n++;
                    break;
                }
            }
        }
        return n == 4;
    }
}
