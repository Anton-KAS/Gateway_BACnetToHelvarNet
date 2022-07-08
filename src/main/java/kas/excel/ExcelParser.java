package kas.excel;

import java.io.*;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONObject;

public class ExcelParser {
    private final Logger logger;
    private String dirPath;
    private String fileName = "configPoints.xlsx";

    public ExcelParser() {
        this.logger = Logger.getLogger(ExcelParser.class);
    }

    public ExcelParser(String dirPath, String fileName) {
        this.logger = Logger.getLogger(ExcelParser.class);
        this.dirPath = dirPath;
        this.fileName = fileName;
    }

    public JSONObject parseXlsxToJson() {
        try {
            logger.info("Start parseXlsxToJson");

            String filePath = dirPath + fileName;
            File file = new File(filePath);
            String filePath2 = System.getProperty("user.dir") + "\\" + fileName;
            File file2 = new File(filePath2);

            XSSFWorkbook wb;
            logger.info("Excel file " + filePath + " exist: " + file.exists());
            logger.info("Excel file " + filePath + " exist: " + file2.exists());
            if (file.exists()) {
                logger.info("Start parse excel file " + filePath);
                FileInputStream inputStream = new FileInputStream(file);
                wb = new XSSFWorkbook(inputStream);
            } else if (file2.exists()) {
                logger.info("Start parse excel file " + filePath2);
                FileInputStream inputStream2 = new FileInputStream(file2);
                wb = new XSSFWorkbook(inputStream2);
            } else {
                logger.info("Searching file in resources: " + fileName + " : " + fileName);
                ClassLoader classLoader = getClass().getClassLoader();
                InputStream inputStream3 = classLoader.getResourceAsStream(fileName);
                wb = new XSSFWorkbook(inputStream3);
            }

            JSONObject jsonData = new JSONObject();

            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                XSSFSheet sheet = wb.getSheetAt(i);
                //System.out.println("1 - " + sheet.getSheetName());
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
                    //System.out.println("2 Row num - " + row.getRowNum());
                    JSONObject jsonPoint = new JSONObject();

                    Iterator<Cell> cellIterator = row.cellIterator();
                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();
                        int cellColumnIndex = cell.getColumnIndex();
                        int cellRowIndex = cell.getRowIndex();
                        //System.out.println("3 Cell num - " + cellColumnIndex + "|" + cellRowIndex);
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
                                                jsonController.put(DefaultHeader.PORT_CONTROLLER.toString(), (int) doubleValue);
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

                    if (headRowIndex != -1 && row.getRowNum() > headRowIndex && jsonPoint.get(DefaultHeader.HELVAR_GROUP.toString()) != null) {
                        jsonPoints.put(jsonPoint.get(DefaultHeader.HELVAR_GROUP.toString()).toString(), jsonPoint);
                    }
                }
                jsonController.put("Points", jsonPoints);
                jsonData.put(jsonController.get(DefaultHeader.IP_CONTROLLER.toString()).toString(), jsonController);
            }
            logger.info("END parseXlsxToJson");
            return jsonData;

        } catch (IOException e) {
            logger.error(e.toString());
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
